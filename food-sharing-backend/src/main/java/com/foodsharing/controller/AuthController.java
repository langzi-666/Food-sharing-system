package com.foodsharing.controller;

import com.foodsharing.dto.AuthRequest;
import com.foodsharing.dto.RegisterRequest;
import com.foodsharing.entity.User;
import com.foodsharing.repository.UserRepository;
import com.foodsharing.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder,
                          AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Validated @RequestBody RegisterRequest req) {
        if (userRepository.existsByUsername(req.getUsername())) {
            return ResponseEntity.badRequest().body(Map.of("message", "用户名已存在"));
        }
        if (userRepository.existsByEmail(req.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("message", "邮箱已被使用"));
        }
        User u = new User();
        u.setUsername(req.getUsername());
        u.setEmail(req.getEmail());
        u.setPassword(passwordEncoder.encode(req.getPassword()));
        userRepository.save(u);
        return ResponseEntity.ok(Map.of("message", "注册成功"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Validated @RequestBody AuthRequest req) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsernameOrEmail(), req.getPassword())
        );
        String token = jwtUtil.generateToken(auth.getName());
        Map<String, Object> body = new HashMap<>();
        body.put("token", token);
        body.put("tokenType", "Bearer");
        return ResponseEntity.ok(body);
    }
}
