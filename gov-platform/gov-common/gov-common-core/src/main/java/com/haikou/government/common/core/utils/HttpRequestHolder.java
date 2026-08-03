package com.haikou.government.common.core.utils;

import jakarta.servlet.http.HttpServletRequest;

/**
 * HTTP 请求上下文持有者（基于 ThreadLocal）
 *
 * 作用：
 * 在请求进入时，将 HttpServletRequest 存入 ThreadLocal
 * 业务代码可以直接获取 request 信息，无需传递参数
 * 请求结束时，清理 ThreadLocal，防止内存泄漏
 *
 * ThreadLocal 原理：
 * 每个线程都有自己独立的变量副本，互不干扰
 * 请求A和请求B的 IP、用户信息互不影响
 *
 * @author xhl
 * @since 2026-08-01
 */
public class HttpRequestHolder {

    private static final ThreadLocal<HttpServletRequest> REQUEST_HOLDER = new ThreadLocal<>();

    private HttpRequestHolder() {
        // 私有构造方法，防止实例化
    }

    /**
     * 设置当前请求的 HttpServletRequest
     *
     * @param request HTTP 请求对象
     */
    public static void set(HttpServletRequest request) {
        REQUEST_HOLDER.set(request);
    }

    /**
     * 获取当前请求的 HttpServletRequest
     *
     * @return HTTP 请求对象
     */
    public static HttpServletRequest get() {
        return REQUEST_HOLDER.get();
    }

    /**
     * 清理当前线程的请求上下文
     *
     * 必须在请求结束后调用，防止内存泄漏
     */
    public static void clear() {
        REQUEST_HOLDER.remove();
    }

    /**
     * 获取客户端真实IP地址
     *
     * @return IP 地址
     */
    public static String getClientIp() {
        HttpServletRequest request = REQUEST_HOLDER.get();
        if (request == null) {
            return "unknown";
        }
        return IpUtils.getClientIp(request);
    }
}
