package com.foodsharing.controller;

import com.foodsharing.entity.Post;
import com.foodsharing.entity.User;
import com.foodsharing.repository.UserRepository;
import com.foodsharing.service.RecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final UserRepository userRepository;

    public RecommendationController(RecommendationService recommendationService,
                                    UserRepository userRepository) {
        this.recommendationService = recommendationService;
        this.userRepository = userRepository;
    }

    @GetMapping("/personalized")
    public ResponseEntity<?> getPersonalizedRecommendations(
            Authentication authentication,
            @RequestParam(defaultValue = "10") int limit) {
        
        User user = null;
        if (authentication != null) {
            String username = authentication.getName();
            user = userRepository.findByUsername(username).orElse(null);
        }

        List<Post> posts = recommendationService.getPersonalizedRecommendations(user, limit);
        
        Map<String, Object> resp = new HashMap<>();
        resp.put("items", posts.stream().map(this::toPostMap).collect(Collectors.toList()));
        resp.put("count", posts.size());
        
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/location-based")
    public ResponseEntity<?> getLocationBasedRecommendations(
            @RequestParam Double lat,
            @RequestParam Double lng,
            @RequestParam(defaultValue = "10") int limit) {
        
        List<Post> posts = recommendationService.getLocationBasedRecommendations(lat, lng, limit);
        
        Map<String, Object> resp = new HashMap<>();
        resp.put("items", posts.stream().map(this::toPostMap).collect(Collectors.toList()));
        resp.put("count", posts.size());
        
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/popular")
    public ResponseEntity<?> getPopularPosts(@RequestParam(defaultValue = "10") int limit) {
        List<Post> posts = recommendationService.getPopularPosts(limit);
        
        Map<String, Object> resp = new HashMap<>();
        resp.put("items", posts.stream().map(this::toPostMap).collect(Collectors.toList()));
        resp.put("count", posts.size());
        
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/hot")
    public ResponseEntity<?> getHotPosts(@RequestParam(defaultValue = "10") int limit) {
        List<Post> posts = recommendationService.getHotPosts(limit);
        
        Map<String, Object> resp = new HashMap<>();
        resp.put("items", posts.stream().map(this::toPostMap).collect(Collectors.toList()));
        resp.put("count", posts.size());
        
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/latest")
    public ResponseEntity<?> getLatestPosts(@RequestParam(defaultValue = "10") int limit) {
        List<Post> posts = recommendationService.getLatestPosts(limit);
        
        Map<String, Object> resp = new HashMap<>();
        resp.put("items", posts.stream().map(this::toPostMap).collect(Collectors.toList()));
        resp.put("count", posts.size());
        
        return ResponseEntity.ok(resp);
    }

    private Map<String, Object> toPostMap(Post p) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", p.getId());
        m.put("title", p.getTitle());
        m.put("content", p.getContent());
        m.put("imageUrl", p.getImageUrl());
        m.put("videoUrl", p.getVideoUrl());
        m.put("viewCount", p.getViewCount());
        m.put("createdAt", p.getCreatedAt());
        if (p.getCategory() != null) {
            m.put("category", Map.of("id", p.getCategory().getId(), "name", p.getCategory().getName()));
        }
        if (p.getAuthor() != null) {
            m.put("author", Map.of("id", p.getAuthor().getId(), "username", p.getAuthor().getUsername()));
        }
        return m;
    }
}

