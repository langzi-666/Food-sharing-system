package com.foodsharing.security;

import com.foodsharing.annotation.RequiresAdmin;
import com.foodsharing.entity.User;
import com.foodsharing.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminAuthInterceptor implements HandlerInterceptor {

    private final UserRepository userRepository;

    public AdminAuthInterceptor(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RequiresAdmin requiresAdmin = handlerMethod.getMethodAnnotation(RequiresAdmin.class);
        if (requiresAdmin == null) {
            requiresAdmin = handlerMethod.getBeanType().getAnnotation(RequiresAdmin.class);
        }

        if (requiresAdmin != null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"message\":\"未登录\"}");
                return false;
            }

            String username = authentication.getName();
            User user = userRepository.findByUsername(username).orElse(null);
            if (user == null || !"ROLE_ADMIN".equals(user.getRole())) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("{\"message\":\"需要管理员权限\"}");
                return false;
            }
        }

        return true;
    }
}

