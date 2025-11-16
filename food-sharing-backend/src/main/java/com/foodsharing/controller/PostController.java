package com.foodsharing.controller;

import com.foodsharing.dto.PostCreateRequest;
import com.foodsharing.entity.Category;
import com.foodsharing.entity.Post;
import com.foodsharing.entity.Tag;
import com.foodsharing.entity.User;
import com.foodsharing.repository.CategoryRepository;
import com.foodsharing.repository.PostRepository;
import com.foodsharing.repository.TagRepository;
import com.foodsharing.repository.UserRepository;
import com.foodsharing.repository.LikeRepository;
import com.foodsharing.repository.CommentRepository;
import com.foodsharing.repository.FavoriteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/posts")
public class PostController {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final FavoriteRepository favoriteRepository;

    public PostController(PostRepository postRepository,
                          UserRepository userRepository,
                          CategoryRepository categoryRepository,
                          TagRepository tagRepository,
                          LikeRepository likeRepository,
                          CommentRepository commentRepository,
                          FavoriteRepository favoriteRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.tagRepository = tagRepository;
        this.likeRepository = likeRepository;
        this.commentRepository = commentRepository;
        this.favoriteRepository = favoriteRepository;
    }

    @PostMapping
    public ResponseEntity<?> create(Authentication authentication, @RequestBody PostCreateRequest req) {
        if (authentication == null) {
            return ResponseEntity.status(401).body(Map.of("message", "未登录"));
        }
        String username = authentication.getName();
        User author = userRepository.findByUsername(username).orElse(null);
        if (author == null) return ResponseEntity.status(404).body(Map.of("message", "用户不存在"));

        Post p = new Post();
        p.setTitle(req.getTitle());
        p.setContent(req.getContent());
        p.setImageUrl(req.getImageUrl());
        p.setVideoUrl(req.getVideoUrl());
        p.setAuthor(author);
        p.setLat(req.getLat());
        p.setLng(req.getLng());
        p.setAddress(req.getAddress());
        if (req.getCategoryId() != null) {
            Category c = categoryRepository.findById(req.getCategoryId()).orElse(null);
            p.setCategory(c);
        }
        if (req.getTagIds() != null && !req.getTagIds().isEmpty()) {
            List<Tag> tags = tagRepository.findAllById(req.getTagIds());
            p.setTags(new HashSet<>(tags));
        }
        Post saved = postRepository.save(p);
        // 简单积分/等级规则：+10 分，每满 100 分等级 +1
        if (author.getPoints() == null) author.setPoints(0);
        if (author.getLevel() == null) author.setLevel(1);
        int newPoints = author.getPoints() + 10;
        author.setPoints(newPoints);
        int newLevel = Math.max(1, (newPoints / 100) + 1);
        if (newLevel != author.getLevel()) {
            author.setLevel(newLevel);
        }
        userRepository.save(author);
        return ResponseEntity.ok(toDetailMap(saved));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> detail(@PathVariable Long id) {
        Optional<Post> opt = postRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.status(404).body(Map.of("message", "内容不存在"));
        Post p = opt.get();
        p.setViewCount(p.getViewCount() + 1);
        postRepository.save(p);
        return ResponseEntity.ok(toDetailMap(p));
    }

    @GetMapping
    public ResponseEntity<?> list(@RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "10") int size,
                                  @RequestParam(required = false) String q,
                                  @RequestParam(required = false) Long categoryId,
                                  @RequestParam(required = false) Long tagId) {
        if (page < 0) page = 0;
        if (size <= 0 || size > 100) size = 10;
        Specification<Post> spec = (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            if (q != null && !q.isBlank()) {
                var like = "%" + q.trim() + "%";
                predicates.add(cb.or(
                        cb.like(root.get("title"), like),
                        cb.like(root.get("content"), like)
                ));
            }
            if (categoryId != null) {
                predicates.add(cb.equal(root.join("category", JoinType.LEFT).get("id"), categoryId));
            }
            if (tagId != null) {
                Join<Post, Tag> join = root.join("tags", JoinType.LEFT);
                predicates.add(cb.equal(join.get("id"), tagId));
                query.distinct(true);
            }
            return cb.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
        Page<Post> result = postRepository.findAll(spec, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        Map<String, Object> resp = new HashMap<>();
        resp.put("page", result.getNumber());
        resp.put("size", result.getSize());
        resp.put("totalElements", result.getTotalElements());
        resp.put("totalPages", result.getTotalPages());
        resp.put("items", result.getContent().stream().map(this::toListMap).collect(Collectors.toList()));
        return ResponseEntity.ok(resp);
    }

    private Map<String, Object> toListMap(Post p) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", p.getId());
        m.put("title", p.getTitle());
        m.put("imageUrl", p.getImageUrl());
        m.put("videoUrl", p.getVideoUrl());
        m.put("category", p.getCategory() == null ? null : Map.of("id", p.getCategory().getId(), "name", p.getCategory().getName()));
        m.put("tags", p.getTags().stream().map(t -> Map.of("id", t.getId(), "name", t.getName())).collect(Collectors.toList()));
        m.put("author", p.getAuthor() == null ? null : Map.of("id", p.getAuthor().getId(), "username", p.getAuthor().getUsername()));
        m.put("viewCount", p.getViewCount());
        m.put("createdAt", p.getCreatedAt());
        return m;
    }

    private Map<String, Object> toDetailMap(Post p) {
        Map<String, Object> m = toListMap(p);
        m.put("content", p.getContent());
        m.put("lat", p.getLat());
        m.put("lng", p.getLng());
        m.put("address", p.getAddress());
        
        // 添加互动统计
        m.put("likeCount", likeRepository.countByPost(p));
        m.put("commentCount", commentRepository.countByPost(p));
        m.put("favoriteCount", favoriteRepository.countByPost(p));
        
        return m;
    }
}
