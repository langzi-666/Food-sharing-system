package com.foodsharing.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    // 每个IP的请求计数（时间窗口：1分钟）
    private final Map<String, RequestCounter> requestCounters = new ConcurrentHashMap<>();
    private static final int MAX_REQUESTS_PER_MINUTE = 100; // 每分钟最大请求数
    private static final long TIME_WINDOW_MS = 60 * 1000; // 1分钟

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String clientIp = getClientIpAddress(request);
        RequestCounter counter = requestCounters.computeIfAbsent(clientIp, k -> new RequestCounter());

        long currentTime = System.currentTimeMillis();
        
        // 清理过期计数器
        if (currentTime - counter.getWindowStart() > TIME_WINDOW_MS) {
            counter.reset(currentTime);
        }

        // 检查是否超过限制
        if (counter.getCount().get() >= MAX_REQUESTS_PER_MINUTE) {
            response.setStatus(429); // Too Many Requests
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"message\":\"请求过于频繁，请稍后再试\"}");
            return false;
        }

        // 增加计数
        counter.getCount().incrementAndGet();
        return true;
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

    private static class RequestCounter {
        private final AtomicInteger count = new AtomicInteger(0);
        private long windowStart = System.currentTimeMillis();

        public AtomicInteger getCount() {
            return count;
        }

        public long getWindowStart() {
            return windowStart;
        }

        public void reset(long newWindowStart) {
            count.set(0);
            windowStart = newWindowStart;
        }
    }
}

