package com.foodsharing.controller;

import com.foodsharing.dto.CommentCreateRequest;
import com.foodsharing.entity.Comment;
import com.foodsharing.entity.Post;
import com.foodsharing.entity.User;
import com.foodsharing.repository.CommentRepository;
import com.foodsharing.repository.PostRepository;
import com.foodsharing.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/comments")
public class CommentController {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public CommentController(CommentRepository commentRepository,
                           PostRepository postRepository,
                           UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/posts/{postId}")
    public ResponseEntity<?> createComment(@PathVariable Long postId,
                                         @Validated @RequestBody CommentCreateRequest req,
                                         Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).body(Map.of("message", "未登录"));
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) return ResponseEntity.status(404).body(Map.of("message", "用户不存在"));

        Post post = postRepository.findById(postId).orElse(null);
        if (post == null) return ResponseEntity.status(404).body(Map.of("message", "内容不存在"));

        Comment comment = new Comment();
        comment.setUser(user);
        comment.setPost(post);
        comment.setContent(req.getContent());

        if (req.getParentId() != null) {
            Comment parent = commentRepository.findById(req.getParentId()).orElse(null);
            if (parent != null && parent.getPost().getId().equals(postId)) {
                comment.setParent(parent);
            }
        }

        Comment saved = commentRepository.save(comment);
        return ResponseEntity.ok(toCommentMap(saved));
    }

    @GetMapping("/posts/{postId}")
    public ResponseEntity<?> getComments(@PathVariable Long postId,
                                       @RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "10") int size) {
        Post post = postRepository.findById(postId).orElse(null);
        if (post == null) return ResponseEntity.status(404).body(Map.of("message", "内容不存在"));

        if (page < 0) page = 0;
        if (size <= 0 || size > 100) size = 10;

        Page<Comment> result = commentRepository.findByPostAndParentIsNullOrderByCreatedAtDesc(
            post, PageRequest.of(page, size)
        );

        Map<String, Object> resp = new HashMap<>();
        resp.put("page", result.getNumber());
        resp.put("size", result.getSize());
        resp.put("totalElements", result.getTotalElements());
        resp.put("totalPages", result.getTotalPages());
        resp.put("items", result.getContent().stream().map(this::toCommentMapWithReplies).collect(Collectors.toList()));

        return ResponseEntity.ok(resp);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Long commentId, Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).body(Map.of("message", "未登录"));
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) return ResponseEntity.status(404).body(Map.of("message", "用户不存在"));

        Comment comment = commentRepository.findById(commentId).orElse(null);
        if (comment == null) return ResponseEntity.status(404).body(Map.of("message", "评论不存在"));

        if (!comment.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body(Map.of("message", "无权限删除"));
        }

        commentRepository.delete(comment);
        return ResponseEntity.ok(Map.of("message", "删除成功"));
    }

    private Map<String, Object> toCommentMap(Comment comment) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", comment.getId());
        map.put("content", comment.getContent());
        map.put("createdAt", comment.getCreatedAt());
        map.put("user", Map.of(
            "id", comment.getUser().getId(),
            "username", comment.getUser().getUsername(),
            "avatarUrl", comment.getUser().getAvatarUrl()
        ));
        return map;
    }

    private Map<String, Object> toCommentMapWithReplies(Comment comment) {
        Map<String, Object> map = toCommentMap(comment);
        
        // 获取回复
        List<Comment> replies = commentRepository.findByParentOrderByCreatedAtAsc(comment);
        map.put("replies", replies.stream().map(this::toCommentMap).collect(Collectors.toList()));
        
        return map;
    }
}
