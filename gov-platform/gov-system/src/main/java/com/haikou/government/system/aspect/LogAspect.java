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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

/**
 * 操作日志 AOP 切面
 *
 * 功能：
 * 1. 拦截所有标注了 @Log 注解的 Controller 方法
 * 2. 记录请求信息、响应结果、耗时
 * 3. 敏感信息自动脱敏（密码、身份证、手机号）
 * 4. 异步写入数据库，不影响业务性能
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
     * 密码字段正则（匹配 JSON 中的 password 字段值）
     */
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "(\"password\"\\s*:\\s*\")([^\"]*)(\")",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * 身份证号正则（18位）
     */
    private static final Pattern ID_CARD_PATTERN = Pattern.compile(
            "\\d{6}(19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[\\dXx]"
    );

    /**
     * 手机号正则（11位）
     */
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "(?<![\\d])1[3-9]\\d{9}(?![\\d])"
    );

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

            // 记录请求参数（脱敏处理）
            String params = objectMapper.writeValueAsString(joinPoint.getArgs());
            operLog.setOperParam(desensitize(params));

            // 执行目标方法
            Object result = joinPoint.proceed();

            // 记录返回结果（脱敏处理）
            String resultStr = objectMapper.writeValueAsString(result);
            operLog.setJsonResult(desensitize(resultStr));
            operLog.setStatus((byte) 0); // 成功

            return result;

        } catch (Throwable e) {
            // 记录异常信息
            operLog.setStatus((byte) 1); // 异常
            operLog.setErrorMsg(truncate(e.getMessage(), 2000));
            throw e;

        } finally {
            // 记录耗时和时间
            long costTime = System.currentTimeMillis() - startTime;
            operLog.setCostTime(costTime);
            operLog.setOperTime(LocalDateTime.now());

            // 异步保存日志
            asyncSaveLog(operLog);

            log.debug("操作日志已记录: title={}, costTime={}ms", operLog.getTitle(), costTime);
        }
    }

    /**
     * 异步保存日志（不影响业务接口响应时间）
     * @Async注解会开启新的线程自动执行的，不会阻塞业务线程
     */
    @Async
    public void asyncSaveLog(SysOperLog operLog) {
        try {
            operLogMapper.insert(operLog);
        } catch (Exception e) {
            log.error("保存操作日志失败", e);
        }
    }

    /**
     * 敏感信息脱敏
     *
     * 处理规则：
     * 1. password 字段值替换为 ***
     * 2. 身份证号保留前3后4：110***1234
     * 3. 手机号保留前3后4：138****8000
     */
    private String desensitize(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }

        // 截断过长内容
        content = truncate(content, 2000);

        // 1. 密码脱敏：将 password 字段值替换为 ***
        content = PASSWORD_PATTERN.matcher(content).replaceAll("$1***$3");

        // 2. 身份证号脱敏：保留前3后4
        content = ID_CARD_PATTERN.matcher(content).replaceAll(match -> {
            String idCard = match.group();
            return idCard.substring(0, 3) + "***" + idCard.substring(14);
        });

        // 3. 手机号脱敏：保留前3后4
        content = PHONE_PATTERN.matcher(content).replaceAll(match -> {
            String phone = match.group();
            return phone.substring(0, 3) + "****" + phone.substring(7);
        });

        return content;
    }

    /**
     * 截断字符串
     */
    private String truncate(String str, int maxLength) {
        if (str == null) {
            return null;
        }
        return str.length() > maxLength ? str.substring(0, maxLength) : str;
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
     *
     * 优先级：X-Forwarded-For > Proxy-Client-IP > WL-Proxy-Client-IP > X-Real-IP > remoteAddr
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
