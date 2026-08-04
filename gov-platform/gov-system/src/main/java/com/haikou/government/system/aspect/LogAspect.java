package com.haikou.government.system.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.haikou.government.common.core.utils.JwtUtils;
import com.haikou.government.system.annotation.Log;
import com.haikou.government.system.domain.SysOperLog;
import com.haikou.government.system.mapper.SysOperLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 操作日志 AOP 切面
 *
 * 工作原理：
 * 1. 拦截所有标注了 @Log 注解的 Controller 方法
 * 2. 方法执行前：记录开始时间、请求参数
 * 3. 方法执行后：记录返回结果、耗时
 * 4. 将日志信息保存到数据库
 *
 * @author xhl
 * @since 2026-08-04
 */
@Slf4j
@Aspect
@Component
public class LogAspect {

    @Autowired
    private SysOperLogMapper operLogMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * 切点：标注了 @Log 注解的方法
     */
    @Pointcut("@annotation(com.haikou.government.system.annotation.Log)")
    public void logPointcut() {
    }

    /**
     * 环绕通知：在方法执行前后记录日志
     */
    @Around("logPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 记录开始时间
        long startTime = System.currentTimeMillis();

        // 创建日志对象
        SysOperLog operLog = new SysOperLog();

        try {
            // 获取注解信息
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            Log logAnnotation = method.getAnnotation(Log.class);
            operLog.setTitle(logAnnotation.title() + " - " + logAnnotation.businessType().getDesc());
            operLog.setMethod(signature.getDeclaringTypeName() + "." + signature.getName());

            // 获取请求信息
            HttpServletRequest request = getRequest();
            if (request != null) {
                operLog.setRequestMethod(request.getMethod());
                operLog.setOperUrl(request.getRequestURI());
                operLog.setOperIp(getClientIp(request));
            }

            // 获取操作人（从 JWT Token 中解析）
            String username = getUsernameFromToken(request);
            operLog.setOperName(username != null ? username : "unknown");

            // 记录请求参数
            String params = objectMapper.writeValueAsString(joinPoint.getArgs());
            operLog.setOperParam(params.length() > 2000 ? params.substring(0, 2000) : params);

            // 执行目标方法
            Object result = joinPoint.proceed();

            // 记录返回结果
            String resultStr = objectMapper.writeValueAsString(result);
            operLog.setJsonResult(resultStr.length() > 2000 ? resultStr.substring(0, 2000) : resultStr);
            operLog.setStatus((byte) 0); // 成功

            return result;

        } catch (Throwable e) {
            // 记录异常信息
            operLog.setStatus((byte) 1); // 异常
            operLog.setErrorMsg(e.getMessage());
            throw e;

        } finally {
            // 记录耗时和时间
            long costTime = System.currentTimeMillis() - startTime;
            operLog.setCostTime(costTime);
            operLog.setOperTime(LocalDateTime.now());

            // 异步保存日志（这里用同步，实际项目可以用异步）
            try {
                operLogMapper.insert(operLog);
            } catch (Exception e) {
                log.error("保存操作日志失败", e);
            }

            log.debug("操作日志已记录: title={}, costTime={}ms", operLog.getTitle(), costTime);
        }
    }

    /**
     * 获取当前请求
     */
    private HttpServletRequest getRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    /**
     * 获取客户端真实 IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理时取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 从 Token 中解析用户名
     */
    private String getUsernameFromToken(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            try {
                return jwtUtils.getUsernameFromToken(token);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }
}
