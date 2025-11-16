package com.foodsharing.controller;

import com.foodsharing.entity.Like;
import com.foodsharing.entity.Post;
import com.foodsharing.entity.User;
import com.foodsharing.repository.LikeRepository;
import com.foodsharing.repository.PostRepository;
import com.foodsharing.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/likes")
public class LikeController {

    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public LikeController(LikeRepository likeRepository, 
                         PostRepository postRepository,
                         UserRepository userRepository) {
        this.likeRepository = likeRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/posts/{postId}")
    @Transactional
    public ResponseEntity<?> toggleLike(@PathVariable Long postId, Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).body(Map.of("message", "未登录"));
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) return ResponseEntity.status(404).body(Map.of("message", "用户不存在"));

        Post post = postRepository.findById(postId).orElse(null);
        if (post == null) return ResponseEntity.status(404).body(Map.of("message", "内容不存在"));

        boolean isLiked = likeRepository.existsByUserAndPost(user, post);
        
        if (isLiked) {
            // 取消点赞
            likeRepository.deleteByUserAndPost(user, post);
        } else {
            // 点赞
            Like like = new Like();
            like.setUser(user);
            like.setPost(post);
            likeRepository.save(like);
        }

        long likeCount = likeRepository.countByPost(post);
        
        return ResponseEntity.ok(Map.of(
            "liked", !isLiked,
            "likeCount", likeCount
        ));
    }

    @GetMapping("/posts/{postId}")
    public ResponseEntity<?> getLikeStatus(@PathVariable Long postId, Authentication authentication) {
        Post post = postRepository.findById(postId).orElse(null);
        if (post == null) return ResponseEntity.status(404).body(Map.of("message", "内容不存在"));

        long likeCount = likeRepository.countByPost(post);
        boolean liked = false;

        if (authentication != null) {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username).orElse(null);
            if (user != null) {
                liked = likeRepository.existsByUserAndPost(user, post);
            }
        }

        return ResponseEntity.ok(Map.of(
            "liked", liked,
            "likeCount", likeCount
        ));
    }
}
