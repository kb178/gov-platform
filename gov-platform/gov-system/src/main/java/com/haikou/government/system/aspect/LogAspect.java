package com.haikou.government.system.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.haikou.government.common.core.utils.HttpRequestHolder;
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
    // TODO: @Log 注解和 LogAspect 后续下沉到 gov-common 模块
    // 当第二个服务需要操作日志时再重构

    @Autowired
    private SysOperLogMapper operLogMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * 密码字段正则
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
        long startTime = System.currentTimeMillis();
        SysOperLog operLog = new SysOperLog();

        try {
            // 获取注解信息
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            Log logAnnotation = method.getAnnotation(Log.class);
            operLog.setTitle(logAnnotation.title() + " - " + logAnnotation.businessType().getDesc());
            operLog.setMethod(signature.getDeclaringTypeName() + "." + signature.getName());

            // 获取请求信息（使用 HttpRequestHolder，无需手动获取 request）
            HttpServletRequest request = HttpRequestHolder.get();
            if (request != null) {
                operLog.setRequestMethod(request.getMethod());
                operLog.setOperUrl(request.getRequestURI());
                operLog.setOperIp(HttpRequestHolder.getClientIp());  // 直接调用，无需重复实现
            }

            // 获取操作人（从 JWT Token 中解析）
            operLog.setOperName(getUsernameFromToken(request));

            // 记录请求参数（脱敏处理）
            String params = objectMapper.writeValueAsString(joinPoint.getArgs());
            operLog.setOperParam(desensitize(params));

            // 执行目标方法
            Object result = joinPoint.proceed();

            // 记录返回结果（脱敏处理）
            String resultStr = objectMapper.writeValueAsString(result);
            operLog.setJsonResult(desensitize(resultStr));
            operLog.setStatus((byte) 0);

            return result;

        } catch (Throwable e) {
            operLog.setStatus((byte) 1);
            operLog.setErrorMsg(truncate(e.getMessage(), 2000));
            throw e;

        } finally {
            long costTime = System.currentTimeMillis() - startTime;
            operLog.setCostTime(costTime);
            operLog.setOperTime(LocalDateTime.now());

            // 异步保存日志
            asyncSaveLog(operLog);

            log.debug("操作日志已记录: title={}, costTime={}ms", operLog.getTitle(), costTime);
        }
    }

    /**
     * 异步保存日志
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
     */
    private String desensitize(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }

        content = truncate(content, 2000);

        // 密码脱敏
        content = PASSWORD_PATTERN.matcher(content).replaceAll("$1***$3");

        // 身份证号脱敏：保留前3后4
        content = ID_CARD_PATTERN.matcher(content).replaceAll(match -> {
            String idCard = match.group();
            return idCard.substring(0, 3) + "***" + idCard.substring(14);
        });

        // 手机号脱敏：保留前3后4
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
