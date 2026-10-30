package com.haikou.government.common.security.utils;

import com.haikou.government.common.core.exception.BusinessException;
import com.haikou.government.common.security.filter.JwtAuthFilter;

/**
 * Security 工具类
 *
 * 功能：获取当前登录用户信息
 * 使用方式：SecurityUtils.getCurrentUserId()
 *
 * @author xhl
 * @date 2026-08-02
 */
public class SecurityUtils {

    /**
     * 获取当前登录用户ID
     *
     * @return 用户ID
     * @throws BusinessException 未登录时抛出异常
     */
    public static Long getCurrentUserId() {
        Long userId = JwtAuthFilter.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("用户未登录");
        }
        return userId;
    }

    /**
     * 获取当前登录用户ID（可选，不抛异常）
     *
     * @return 用户ID，未登录返回 null
     */
    public static Long getCurrentUserIdOrNull() {
        return JwtAuthFilter.getCurrentUserId();
    }
}
