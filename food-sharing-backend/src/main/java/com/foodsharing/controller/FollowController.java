package com.foodsharing.controller;

import com.foodsharing.entity.Follow;
import com.foodsharing.entity.User;
import com.foodsharing.repository.FollowRepository;
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
@RequestMapping("/api/v1/follows")
public class FollowController {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    public FollowController(FollowRepository followRepository,
                          UserRepository userRepository) {
        this.followRepository = followRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/users/{userId}")
    @Transactional
    public ResponseEntity<?> toggleFollow(@PathVariable Long userId, Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).body(Map.of("message", "未登录"));
        }

        String username = authentication.getName();
        User follower = userRepository.findByUsername(username).orElse(null);
        if (follower == null) return ResponseEntity.status(404).body(Map.of("message", "用户不存在"));

        User following = userRepository.findById(userId).orElse(null);
        if (following == null) return ResponseEntity.status(404).body(Map.of("message", "目标用户不存在"));

        if (follower.getId().equals(following.getId())) {
            return ResponseEntity.badRequest().body(Map.of("message", "不能关注自己"));
        }

        boolean isFollowing = followRepository.existsByFollowerAndFollowing(follower, following);
        
        if (isFollowing) {
            // 取消关注
            followRepository.deleteByFollowerAndFollowing(follower, following);
        } else {
            // 关注
            Follow follow = new Follow();
            follow.setFollower(follower);
            follow.setFollowing(following);
            followRepository.save(follow);
        }

        long followerCount = followRepository.countFollowers(following);
        
        return ResponseEntity.ok(Map.of(
            "following", !isFollowing,
            "followerCount", followerCount
        ));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<?> getFollowStatus(@PathVariable Long userId, Authentication authentication) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return ResponseEntity.status(404).body(Map.of("message", "用户不存在"));

        long followerCount = followRepository.countFollowers(user);
        long followingCount = followRepository.countFollowing(user);
        boolean following = false;

        if (authentication != null) {
            String username = authentication.getName();
            User currentUser = userRepository.findByUsername(username).orElse(null);
            if (currentUser != null && !currentUser.getId().equals(userId)) {
                following = followRepository.existsByFollowerAndFollowing(currentUser, user);
            }
        }

        return ResponseEntity.ok(Map.of(
            "following", following,
            "followerCount", followerCount,
            "followingCount", followingCount
        ));
    }

    @GetMapping("/followers")
    public ResponseEntity<?> getFollowers(@RequestParam(defaultValue = "0") int page,
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

        Page<Follow> result = followRepository.findByFollowingOrderByCreatedAtDesc(
            user, PageRequest.of(page, size)
        );

        Map<String, Object> resp = new HashMap<>();
        resp.put("page", result.getNumber());
        resp.put("size", result.getSize());
        resp.put("totalElements", result.getTotalElements());
        resp.put("totalPages", result.getTotalPages());
        resp.put("items", result.getContent().stream().map(this::toFollowerMap).collect(Collectors.toList()));

        return ResponseEntity.ok(resp);
    }

    @GetMapping("/following")
    public ResponseEntity<?> getFollowing(@RequestParam(defaultValue = "0") int page,
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

        Page<Follow> result = followRepository.findByFollowerOrderByCreatedAtDesc(
            user, PageRequest.of(page, size)
        );

        Map<String, Object> resp = new HashMap<>();
        resp.put("page", result.getNumber());
        resp.put("size", result.getSize());
        resp.put("totalElements", result.getTotalElements());
        resp.put("totalPages", result.getTotalPages());
        resp.put("items", result.getContent().stream().map(this::toFollowingMap).collect(Collectors.toList()));

        return ResponseEntity.ok(resp);
    }

    private Map<String, Object> toFollowerMap(Follow follow) {
        User follower = follow.getFollower();
        Map<String, Object> map = new HashMap<>();
        map.put("id", follow.getId());
        map.put("createdAt", follow.getCreatedAt());
        map.put("user", Map.of(
            "id", follower.getId(),
            "username", follower.getUsername(),
            "avatarUrl", follower.getAvatarUrl(),
            "level", follower.getLevel(),
            "points", follower.getPoints()
        ));
        return map;
    }

    private Map<String, Object> toFollowingMap(Follow follow) {
        User following = follow.getFollowing();
        Map<String, Object> map = new HashMap<>();
        map.put("id", follow.getId());
        map.put("createdAt", follow.getCreatedAt());
        map.put("user", Map.of(
            "id", following.getId(),
            "username", following.getUsername(),
            "avatarUrl", following.getAvatarUrl(),
            "level", following.getLevel(),
            "points", following.getPoints()
        ));
        return map;
    }
}
