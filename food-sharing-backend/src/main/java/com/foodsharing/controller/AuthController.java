package com.foodsharing.controller;

import com.foodsharing.dto.ApiResponse;
import com.foodsharing.dto.AuthRequest;
import com.foodsharing.dto.RegisterRequest;
import com.foodsharing.entity.User;
import com.foodsharing.exception.BusinessException;
import com.foodsharing.repository.UserRepository;
import com.foodsharing.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthController(UserRepository userRepository,
                          AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<?>> register(@Validated @RequestBody RegisterRequest req) {
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new BusinessException("用户名已存在");
        }
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new BusinessException("邮箱已被使用");
        }
        User u = new User();
        u.setUsername(req.getUsername());
        u.setEmail(req.getEmail());
        u.setPassword(req.getPassword()); // 直接保存明文密码，不加密
        userRepository.save(u);
        return ResponseEntity.ok(ApiResponse.success("注册成功"));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> login(@Validated @RequestBody AuthRequest req) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.getUsernameOrEmail(), req.getPassword())
            );
            String token = jwtUtil.generateToken(auth.getName());
            Map<String, Object> body = new HashMap<>();
            body.put("token", token);
            body.put("tokenType", "Bearer");
            return ResponseEntity.ok(ApiResponse.success(body));
        } catch (BadCredentialsException e) {
            throw new BusinessException(401, "用户名或密码错误");
        }
    }
}
