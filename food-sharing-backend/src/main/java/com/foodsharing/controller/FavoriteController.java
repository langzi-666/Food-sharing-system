package com.foodsharing.controller;

import com.foodsharing.entity.Favorite;
import com.foodsharing.entity.Post;
import com.foodsharing.entity.User;
import com.foodsharing.repository.FavoriteRepository;
import com.foodsharing.repository.PostRepository;
import com.foodsharing.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/favorites")
public class FavoriteController {

    private final FavoriteRepository favoriteRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public FavoriteController(FavoriteRepository favoriteRepository,
                            PostRepository postRepository,
                            UserRepository userRepository) {
        this.favoriteRepository = favoriteRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/posts/{postId}")
    @Transactional
    public ResponseEntity<?> toggleFavorite(@PathVariable Long postId, Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).body(Map.of("message", "未登录"));
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) return ResponseEntity.status(404).body(Map.of("message", "用户不存在"));

        Post post = postRepository.findById(postId).orElse(null);
        if (post == null) return ResponseEntity.status(404).body(Map.of("message", "内容不存在"));

        boolean isFavorited = favoriteRepository.existsByUserAndPost(user, post);
        
        if (isFavorited) {
            // 取消收藏
            favoriteRepository.deleteByUserAndPost(user, post);
        } else {
            // 收藏
            Favorite favorite = new Favorite();
            favorite.setUser(user);
            favorite.setPost(post);
            favoriteRepository.save(favorite);
        }

        long favoriteCount = favoriteRepository.countByPost(post);
        
        return ResponseEntity.ok(Map.of(
            "favorited", !isFavorited,
            "favoriteCount", favoriteCount
        ));
    }

    @GetMapping("/posts/{postId}")
    public ResponseEntity<?> getFavoriteStatus(@PathVariable Long postId, Authentication authentication) {
        Post post = postRepository.findById(postId).orElse(null);
        if (post == null) return ResponseEntity.status(404).body(Map.of("message", "内容不存在"));

        long favoriteCount = favoriteRepository.countByPost(post);
        boolean favorited = false;

        if (authentication != null) {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username).orElse(null);
            if (user != null) {
                favorited = favoriteRepository.existsByUserAndPost(user, post);
            }
        }

        return ResponseEntity.ok(Map.of(
            "favorited", favorited,
            "favoriteCount", favoriteCount
        ));
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyFavorites(@RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "10") int size,
                                          Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).body(Map.of("message", "未登录"));
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) return ResponseEntity.status(404).body(Map.of("message", "用户不存在"));

        if (page < 0) page = 0;
        if (size <= 0 || size > 100) size = 10;

        Page<Favorite> result = favoriteRepository.findByUserOrderByCreatedAtDesc(
            user, PageRequest.of(page, size)
        );

        Map<String, Object> resp = new HashMap<>();
        resp.put("page", result.getNumber());
        resp.put("size", result.getSize());
        resp.put("totalElements", result.getTotalElements());
        resp.put("totalPages", result.getTotalPages());
        resp.put("items", result.getContent().stream().map(this::toFavoriteMap).collect(Collectors.toList()));

        return ResponseEntity.ok(resp);
    }

    private Map<String, Object> toFavoriteMap(Favorite favorite) {
        Post post = favorite.getPost();
        Map<String, Object> map = new HashMap<>();
        map.put("id", favorite.getId());
        map.put("createdAt", favorite.getCreatedAt());
        map.put("post", Map.of(
            "id", post.getId(),
            "title", post.getTitle(),
            "imageUrl", post.getImageUrl(),
            "viewCount", post.getViewCount(),
            "author", Map.of(
                "id", post.getAuthor().getId(),
                "username", post.getAuthor().getUsername()
            )
        ));
        return map;
    }
}
