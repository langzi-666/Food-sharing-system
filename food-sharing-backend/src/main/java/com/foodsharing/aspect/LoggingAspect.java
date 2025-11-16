package com.foodsharing.aspect;

import com.foodsharing.service.SystemLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 日志切面 - 自动记录Controller方法的调用
 */
@Aspect
@Component
public class LoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    @Autowired(required = false)
    private SystemLogService systemLogService;

    /**
     * 定义切点：所有Controller的方法
     */
    @Pointcut("execution(* com.foodsharing.controller..*(..))")
    public void controllerMethods() {}

    /**
     * 方法执行前记录
     */
    @Before("controllerMethods()")
    public void logBefore(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().toShortString();
        logger.debug("Entering method: {}", methodName);
    }

    /**
     * 方法执行后记录（成功）
     */
    @AfterReturning(pointcut = "controllerMethods()", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        String methodName = joinPoint.getSignature().toShortString();
        String module = extractModule(joinPoint);
        String action = extractAction(joinPoint);
        
        logger.debug("Method {} completed successfully", methodName);
        
        // 记录到系统日志表（如果SystemLogService可用）
        if (systemLogService != null) {
            try {
                HttpServletRequest request = getCurrentRequest();
                systemLogService.logInfo(module, action, "操作成功: " + methodName, request);
            } catch (Exception e) {
                logger.warn("Failed to log to SystemLogService", e);
            }
        }
    }

    /**
     * 方法执行异常时记录
     */
    @AfterThrowing(pointcut = "controllerMethods()", throwing = "exception")
    public void logAfterThrowing(JoinPoint joinPoint, Exception exception) {
        String methodName = joinPoint.getSignature().toShortString();
        String module = extractModule(joinPoint);
        String action = extractAction(joinPoint);
        
        logger.error("Method {} threw exception: {}", methodName, exception.getMessage(), exception);
        
        // 记录错误到系统日志表
        if (systemLogService != null) {
            try {
                HttpServletRequest request = getCurrentRequest();
                systemLogService.logError(module, action, 
                    "操作失败: " + methodName + " - " + exception.getMessage(), request);
            } catch (Exception e) {
                logger.warn("Failed to log error to SystemLogService", e);
            }
        }
    }

    /**
     * 环绕通知：记录方法执行时间
     */
    @Around("controllerMethods()")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;
            
            String methodName = joinPoint.getSignature().toShortString();
            if (executionTime > 1000) { // 超过1秒的方法记录警告
                logger.warn("Method {} took {} ms", methodName, executionTime);
            } else {
                logger.debug("Method {} took {} ms", methodName, executionTime);
            }
            
            return result;
        } catch (Throwable e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("Method {} failed after {} ms", joinPoint.getSignature().toShortString(), executionTime);
            throw e;
        }
    }

    /**
     * 提取模块名称
     */
    private String extractModule(JoinPoint joinPoint) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        if (className.contains("Controller")) {
            String module = className.replace("Controller", "");
            // 映射到标准模块名
            if (module.contains("Auth")) return "AUTH";
            if (module.contains("User")) return "USER";
            if (module.contains("Post")) return "POST";
            if (module.contains("Merchant")) return "MERCHANT";
            if (module.contains("Admin")) return "ADMIN";
            if (module.contains("Comment")) return "COMMENT";
            return module.toUpperCase();
        }
        return "SYSTEM";
    }

    /**
     * 提取操作名称
     */
    private String extractAction(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        // 根据方法名推断操作类型
        if (methodName.startsWith("create") || methodName.startsWith("register")) return "CREATE";
        if (methodName.startsWith("update") || methodName.startsWith("modify")) return "UPDATE";
        if (methodName.startsWith("delete") || methodName.startsWith("remove")) return "DELETE";
        if (methodName.startsWith("get") || methodName.startsWith("list") || methodName.startsWith("find")) return "QUERY";
        if (methodName.startsWith("login")) return "LOGIN";
        if (methodName.startsWith("logout")) return "LOGOUT";
        return "OTHER";
    }

    /**
     * 获取当前HTTP请求
     */
    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            return null;
        }
    }
}

