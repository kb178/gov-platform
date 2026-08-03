package com.haikou.government.common.core.interceptor;

import com.haikou.government.common.core.utils.HttpRequestHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 请求拦截器
 *
 * 作用：
 * 在请求进入 Controller 之前，将 HttpServletRequest 存入 ThreadLocal
 * 在请求结束后，清理 ThreadLocal，防止内存泄漏
 *
 * 执行顺序：
 * 1. preHandle → 进入 Controller 之前
 * 2. Controller 执行
 * 3. afterCompletion → 请求结束后（无论成功失败）
 *
 * @author xhl
 * @since 2026-08-01
 */
@Slf4j
public class RequestInterceptor implements HandlerInterceptor {

    /**
     * 请求进入 Controller 之前执行
     *
     * @param request  HTTP 请求
     * @param response HTTP 响应
     * @param handler  处理器
     * @return true=继续执行，false=中断请求
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 将 request 存入 ThreadLocal
        HttpRequestHolder.set(request);
        return true;
    }

    /**
     * 请求结束后执行（无论成功失败都会执行）
     *
     * 必须清理 ThreadLocal，防止：
     * 1. 内存泄漏（线程复用时，旧数据还在）
     * 2. 数据混乱（线程被其他请求复用时，读到旧数据）
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        HttpRequestHolder.clear();
    }
}
