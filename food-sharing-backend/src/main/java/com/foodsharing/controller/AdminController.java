package com.foodsharing.controller;

import com.foodsharing.annotation.RequiresAdmin;
import com.foodsharing.entity.*;
import com.foodsharing.repository.*;
import com.foodsharing.service.SystemLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@RequiresAdmin
public class AdminController {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;
    private final FavoriteRepository favoriteRepository;
    private final MerchantRepository merchantRepository;
    private final SystemConfigRepository systemConfigRepository;
    private final SystemLogRepository systemLogRepository;
    private final SystemLogService systemLogService;

    public AdminController(UserRepository userRepository, PostRepository postRepository,
                          CommentRepository commentRepository, LikeRepository likeRepository,
                          FavoriteRepository favoriteRepository, MerchantRepository merchantRepository,
                          SystemConfigRepository systemConfigRepository, SystemLogRepository systemLogRepository,
                          SystemLogService systemLogService) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.likeRepository = likeRepository;
        this.favoriteRepository = favoriteRepository;
        this.merchantRepository = merchantRepository;
        this.systemConfigRepository = systemConfigRepository;
        this.systemLogRepository = systemLogRepository;
        this.systemLogService = systemLogService;
    }

    // ========== 用户管理 ==========
    @GetMapping("/users")
    public ResponseEntity<?> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Boolean enabled) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdTime"));
        Page<User> users;
        
        if (keyword != null && !keyword.isEmpty()) {
            // 简单搜索，实际应该使用Specification
            users = userRepository.findAll(pageable);
        } else {
            users = userRepository.findAll(pageable);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("items", users.getContent());
        result.put("totalElements", users.getTotalElements());
        result.put("totalPages", users.getTotalPages());
        result.put("currentPage", page);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUser(@PathVariable Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }

    @PutMapping("/users/{id}/enable")
    public ResponseEntity<?> enableUser(@PathVariable Long id, @RequestBody Map<String, Boolean> body, 
                                       Authentication auth, HttpServletRequest request) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        Boolean enabled = body.get("enabled");
        user.setEnabled(enabled != null ? enabled : true);
        userRepository.save(user);
        
        systemLogService.logInfo("USER", enabled ? "ENABLE" : "DISABLE", 
                "管理员 " + auth.getName() + " " + (enabled ? "启用" : "禁用") + "用户: " + user.getUsername(), request);
        
        return ResponseEntity.ok(Map.of("message", "操作成功"));
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<?> updateUserRole(@PathVariable Long id, @RequestBody Map<String, String> body,
                                            Authentication auth, HttpServletRequest request) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        String role = body.get("role");
        if (role != null) {
            user.setRole(role);
            userRepository.save(user);
            
            systemLogService.logInfo("USER", "UPDATE_ROLE", 
                    "管理员 " + auth.getName() + " 修改用户 " + user.getUsername() + " 角色为: " + role, request);
        }
        return ResponseEntity.ok(Map.of("message", "操作成功"));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id, Authentication auth, HttpServletRequest request) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        userRepository.delete(user);
        
        systemLogService.logInfo("USER", "DELETE", 
                "管理员 " + auth.getName() + " 删除用户: " + user.getUsername(), request);
        
        return ResponseEntity.ok(Map.of("message", "删除成功"));
    }

    // ========== 内容审核 ==========
    @GetMapping("/posts/pending")
    public ResponseEntity<?> getPendingPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Post> posts = postRepository.findByStatus("PENDING", pageable);
        
        Map<String, Object> result = new HashMap<>();
        result.put("items", posts.getContent());
        result.put("totalElements", posts.getTotalElements());
        result.put("totalPages", posts.getTotalPages());
        result.put("currentPage", page);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/posts/{id}/approve")
    public ResponseEntity<?> approvePost(@PathVariable Long id, Authentication auth, HttpServletRequest request) {
        Post post = postRepository.findById(id).orElse(null);
        if (post == null) {
            return ResponseEntity.notFound().build();
        }
        post.setStatus("APPROVED");
        postRepository.save(post);
        
        systemLogService.logInfo("POST", "APPROVE", 
                "管理员 " + auth.getName() + " 审核通过内容: " + post.getTitle(), request);
        
        return ResponseEntity.ok(Map.of("message", "审核通过"));
    }

    @PutMapping("/posts/{id}/reject")
    public ResponseEntity<?> rejectPost(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body,
                                        Authentication auth, HttpServletRequest request) {
        Post post = postRepository.findById(id).orElse(null);
        if (post == null) {
            return ResponseEntity.notFound().build();
        }
        post.setStatus("REJECTED");
        postRepository.save(post);
        
        String reason = body != null ? body.get("reason") : null;
        systemLogService.logWarn("POST", "REJECT", 
                "管理员 " + auth.getName() + " 拒绝内容: " + post.getTitle() + (reason != null ? ", 原因: " + reason : ""), request);
        
        return ResponseEntity.ok(Map.of("message", "已拒绝"));
    }

    // ========== 数据统计 ==========
    @GetMapping("/statistics/overview")
    public ResponseEntity<?> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // 用户统计
        long totalUsers = userRepository.count();
        long enabledUsers = userRepository.countByEnabled(true);
        long disabledUsers = userRepository.countByEnabled(false);
        long adminUsers = userRepository.countByRole("ROLE_ADMIN");
        
        Map<String, Object> userStats = new HashMap<>();
        userStats.put("total", totalUsers);
        userStats.put("enabled", enabledUsers);
        userStats.put("disabled", disabledUsers);
        userStats.put("admins", adminUsers);
        stats.put("users", userStats);
        
        // 内容统计
        long totalPosts = postRepository.count();
        long pendingPosts = postRepository.findByStatus("PENDING", PageRequest.of(0, 1)).getTotalElements();
        long approvedPosts = postRepository.findByStatus("APPROVED", PageRequest.of(0, 1)).getTotalElements();
        long rejectedPosts = postRepository.findByStatus("REJECTED", PageRequest.of(0, 1)).getTotalElements();
        
        Map<String, Object> postStats = new HashMap<>();
        postStats.put("total", totalPosts);
        postStats.put("pending", pendingPosts);
        postStats.put("approved", approvedPosts);
        postStats.put("rejected", rejectedPosts);
        stats.put("posts", postStats);
        
        // 互动统计
        long totalComments = commentRepository.count();
        long totalLikes = likeRepository.count();
        long totalFavorites = favoriteRepository.count();
        
        Map<String, Object> interactionStats = new HashMap<>();
        interactionStats.put("comments", totalComments);
        interactionStats.put("likes", totalLikes);
        interactionStats.put("favorites", totalFavorites);
        stats.put("interactions", interactionStats);
        
        // 商家统计
        long totalMerchants = merchantRepository.count();
        stats.put("merchants", totalMerchants);
        
        return ResponseEntity.ok(stats);
    }

    // ========== 系统配置 ==========
    @GetMapping("/configs")
    public ResponseEntity<?> getConfigs() {
        return ResponseEntity.ok(systemConfigRepository.findAll());
    }

    @GetMapping("/configs/{key}")
    public ResponseEntity<?> getConfig(@PathVariable String key) {
        return systemConfigRepository.findByConfigKey(key)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/configs")
    public ResponseEntity<?> createConfig(@RequestBody SystemConfig config, Authentication auth, HttpServletRequest request) {
        if (systemConfigRepository.findByConfigKey(config.getConfigKey()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "配置键已存在"));
        }
        config.setCreatedAt(LocalDateTime.now());
        config.setUpdatedAt(LocalDateTime.now());
        systemConfigRepository.save(config);
        
        systemLogService.logInfo("SYSTEM", "CREATE_CONFIG", 
                "管理员 " + auth.getName() + " 创建配置: " + config.getConfigKey(), request);
        
        return ResponseEntity.ok(config);
    }

    @PutMapping("/configs/{key}")
    public ResponseEntity<?> updateConfig(@PathVariable String key, @RequestBody SystemConfig configUpdate,
                                         Authentication auth, HttpServletRequest request) {
        SystemConfig config = systemConfigRepository.findByConfigKey(key).orElse(null);
        if (config == null) {
            return ResponseEntity.notFound().build();
        }
        config.setConfigValue(configUpdate.getConfigValue());
        if (configUpdate.getDescription() != null) {
            config.setDescription(configUpdate.getDescription());
        }
        config.setUpdatedAt(LocalDateTime.now());
        systemConfigRepository.save(config);
        
        systemLogService.logInfo("SYSTEM", "UPDATE_CONFIG", 
                "管理员 " + auth.getName() + " 更新配置: " + key, request);
        
        return ResponseEntity.ok(config);
    }

    @DeleteMapping("/configs/{key}")
    public ResponseEntity<?> deleteConfig(@PathVariable String key, Authentication auth, HttpServletRequest request) {
        SystemConfig config = systemConfigRepository.findByConfigKey(key).orElse(null);
        if (config == null) {
            return ResponseEntity.notFound().build();
        }
        systemConfigRepository.delete(config);
        
        systemLogService.logInfo("SYSTEM", "DELETE_CONFIG", 
                "管理员 " + auth.getName() + " 删除配置: " + key, request);
        
        return ResponseEntity.ok(Map.of("message", "删除成功"));
    }

    // ========== 日志查看 ==========
    @GetMapping("/logs")
    public ResponseEntity<?> getLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String level) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<SystemLog> logs;
        
        if (module != null && level != null) {
            logs = systemLogRepository.findByModuleAndLevel(module, level, pageable);
        } else if (module != null) {
            logs = systemLogRepository.findByModule(module, pageable);
        } else if (level != null) {
            logs = systemLogRepository.findByLevel(level, pageable);
        } else {
            logs = systemLogRepository.findAll(pageable);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("items", logs.getContent());
        result.put("totalElements", logs.getTotalElements());
        result.put("totalPages", logs.getTotalPages());
        result.put("currentPage", page);
        return ResponseEntity.ok(result);
    }
}

