package com.haikou.government.common.core.utils;

import jakarta.servlet.http.HttpServletRequest;

/**
 * IP 地址工具类
 *
 * @author xhl
 * @since 2026-08-01
 */
public class IpUtils {

    private IpUtils() {
        // 私有构造方法，防止实例化
    }

    /**
     * 获取客户端真实IP地址
     *
     * 优先级：
     * 1. X-Forwarded-For（经过多个代理时，第一个IP为真实IP）
     * 2. X-Real-IP（Nginx 代理常用）
     * 3. request.getRemoteAddr()（直接连接）
     *
     * @param request HTTP 请求
     * @return IP 地址
     */
    public static String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }

        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // 多个代理时，第一个IP为真实IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }
}
