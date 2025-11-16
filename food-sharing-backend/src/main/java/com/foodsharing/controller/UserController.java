package com.foodsharing.controller;

import com.foodsharing.entity.User;
import com.foodsharing.repository.UserRepository;
import com.foodsharing.dto.UserProfileUpdateRequest;
import com.foodsharing.dto.PasswordChangeRequest;
import com.foodsharing.service.FileStorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    public UserController(UserRepository userRepository, FileStorageService fileStorageService) {
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).body(Map.of("message", "未登录"));
        }
        String username = authentication.getName();
        User u = userRepository.findByUsername(username).orElse(null);
        if (u == null) return ResponseEntity.status(404).body(Map.of("message", "用户不存在"));
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", u.getId());
        userMap.put("username", u.getUsername());
        userMap.put("email", u.getEmail());
        userMap.put("role", u.getRole());
        userMap.put("avatarUrl", u.getAvatarUrl());
        userMap.put("points", u.getPoints() != null ? u.getPoints() : 0);
        userMap.put("level", u.getLevel() != null ? u.getLevel() : 1);
        return ResponseEntity.ok(userMap);
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateMe(Authentication authentication, @RequestBody UserProfileUpdateRequest req) {
        if (authentication == null) {
            return ResponseEntity.status(401).body(Map.of("message", "未登录"));
        }
        String username = authentication.getName();
        User u = userRepository.findByUsername(username).orElse(null);
        if (u == null) return ResponseEntity.status(404).body(Map.of("message", "用户不存在"));

        if (req.getUsername() != null && !req.getUsername().equals(u.getUsername())) {
            var conflict = userRepository.findByUsername(req.getUsername());
            if (conflict.isPresent() && !conflict.get().getId().equals(u.getId())) {
                return ResponseEntity.badRequest().body(Map.of("message", "用户名已存在"));
            }
            u.setUsername(req.getUsername());
        }
        if (req.getEmail() != null && !req.getEmail().equals(u.getEmail())) {
            var conflict = userRepository.findByEmail(req.getEmail());
            if (conflict.isPresent() && !conflict.get().getId().equals(u.getId())) {
                return ResponseEntity.badRequest().body(Map.of("message", "邮箱已被使用"));
            }
            u.setEmail(req.getEmail());
        }
        if (req.getAvatarUrl() != null) {
            u.setAvatarUrl(req.getAvatarUrl());
        }
        userRepository.save(u);
        return ResponseEntity.ok(Map.of("message", "更新成功"));
    }

    @PostMapping(path = "/me/avatar", consumes = {"multipart/form-data"})
    public ResponseEntity<?> uploadAvatar(Authentication authentication, @RequestPart("file") MultipartFile file) {
        if (authentication == null) {
            return ResponseEntity.status(401).body(Map.of("message", "未登录"));
        }
        String username = authentication.getName();
        User u = userRepository.findByUsername(username).orElse(null);
        if (u == null) return ResponseEntity.status(404).body(Map.of("message", "用户不存在"));
        try {
            String url = fileStorageService.store(file);
            u.setAvatarUrl(url);
            userRepository.save(u);
            return ResponseEntity.ok(Map.of("url", url));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/me/password")
    public ResponseEntity<?> changePassword(Authentication authentication, @RequestBody PasswordChangeRequest req) {
        if (authentication == null) {
            return ResponseEntity.status(401).body(Map.of("message", "未登录"));
        }
        String username = authentication.getName();
        User u = userRepository.findByUsername(username).orElse(null);
        if (u == null) return ResponseEntity.status(404).body(Map.of("message", "用户不存在"));
        if (!u.getPassword().equals(req.getOldPassword())) {
            return ResponseEntity.badRequest().body(Map.of("message", "原密码不正确"));
        }
        u.setPassword(req.getNewPassword());
        userRepository.save(u);
        return ResponseEntity.ok(Map.of("message", "修改成功"));
    }
}
