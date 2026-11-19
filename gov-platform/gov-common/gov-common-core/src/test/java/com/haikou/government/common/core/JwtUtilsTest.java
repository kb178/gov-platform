package com.haikou.government.common.core;

import com.haikou.government.common.core.utils.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JwtUtils 单元测试
 *
 * 测试 JWT Token 的生成、解析、过期等核心逻辑
 *
 * @author gov-platform
 */
@DisplayName("JWT工具类单元测试")
class JwtUtilsTest {

    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        // 手动注入配置（因为没有 Spring 容器）
        // 通过反射设置私有字段，模拟 Spring @Value 注入
        try {
            var secretField = JwtUtils.class.getDeclaredField("secret");
            secretField.setAccessible(true);
            secretField.set(jwtUtils, "gov-platform-jwt-secret-key-must-be-at-least-256-bits-long");

            var expireField = JwtUtils.class.getDeclaredField("expire");
            expireField.setAccessible(true);
            expireField.set(jwtUtils, 7200000L); // 2小时

            // 调用初始化方法生成 SecretKey
            var initMethod = JwtUtils.class.getDeclaredMethod("init");
            initMethod.setAccessible(true);
            initMethod.invoke(jwtUtils);
        } catch (Exception e) {
            throw new RuntimeException("初始化 JwtUtils 失败", e);
        }
    }

    @Test
    @DisplayName("生成Token并解析 - 验证用户信息正确")
    void createAndParseToken() {
        // 生成 Token
        String token = jwtUtils.createToken(1L, "admin", "test-uuid-123");

        assertNotNull(token);

        // 解析验证
        Long userId = jwtUtils.getUserIdFromToken(token);
        String username = jwtUtils.getUsernameFromToken(token);
        String uuid = jwtUtils.getUserKeyFromToken(token);

        assertEquals(1L, userId);
        assertEquals("admin", username);
        assertEquals("test-uuid-123", uuid);
    }

    @Test
    @DisplayName("Token未过期 - isTokenExpired返回false")
    void tokenNotExpired() {
        String token = jwtUtils.createToken(1L, "admin", "uuid-1");
        assertFalse(jwtUtils.isTokenExpired(token));
    }

    @Test
    @DisplayName("解析无效Token - 抛出异常")
    void parseInvalidToken() {
        assertThrows(Exception.class, () -> jwtUtils.parseToken("invalid-token"));
    }

    @Test
    @DisplayName("使用自定义claims生成Token")
    void createTokenWithCustomClaims() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("user_id", 100L);
        claims.put("username", "testuser");
        claims.put("role", "admin");

        String token = jwtUtils.createToken(claims);
        assertNotNull(token);

        // 能正常解析
        Long userId = jwtUtils.getUserIdFromToken(token);
        assertEquals(100L, userId);
    }

    @Test
    @DisplayName("过期Token - isTokenExpired返回true")
    void expiredToken() throws Exception {
        // 创建一个立即过期的 JwtUtils
        JwtUtils expiredJwtUtils = new JwtUtils();
        var secretField = JwtUtils.class.getDeclaredField("secret");
        secretField.setAccessible(true);
        secretField.set(expiredJwtUtils, "gov-platform-jwt-secret-key-must-be-at-least-256-bits-long");

        var expireField = JwtUtils.class.getDeclaredField("expire");
        expireField.setAccessible(true);
        expireField.set(expiredJwtUtils, -1000L); // 负数，立即过期

        var initMethod = JwtUtils.class.getDeclaredMethod("init");
        initMethod.setAccessible(true);
        initMethod.invoke(expiredJwtUtils);

        String token = expiredJwtUtils.createToken(1L, "admin", "uuid-1");
        assertTrue(expiredJwtUtils.isTokenExpired(token));
    }
}
