package com.foodsharing.service;

import com.foodsharing.entity.SystemLog;
import com.foodsharing.repository.SystemLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class SystemLogService {
    private final SystemLogRepository systemLogRepository;

    public SystemLogService(SystemLogRepository systemLogRepository) {
        this.systemLogRepository = systemLogRepository;
    }

    public void log(String level, String module, String action, String message, HttpServletRequest request) {
        SystemLog log = new SystemLog();
        log.setLevel(level);
        log.setModule(module);
        log.setAction(action);
        log.setMessage(message);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            log.setUsername(authentication.getName());
        }

        if (request != null) {
            String ipAddress = getClientIpAddress(request);
            log.setIpAddress(ipAddress);
        }

        systemLogRepository.save(log);
    }

    public void logInfo(String module, String action, String message, HttpServletRequest request) {
        log("INFO", module, action, message, request);
    }

    public void logWarn(String module, String action, String message, HttpServletRequest request) {
        log("WARN", module, action, message, request);
    }

    public void logError(String module, String action, String message, HttpServletRequest request) {
        log("ERROR", module, action, message, request);
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }
}

