package com.haikou.government.common.core.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类
 *
 * @author gov-platform
 */
@Slf4j
public class JwtUtils {

    /** 密钥（实际项目应该从配置读取） */
    private static final String SECRET = "gov-platform-jwt-secret-key-must-be-at-least-256-bits-long";

    /** Token 有效期（毫秒） */
    private static final long EXPIRE = 24 * 60 * 60 * 1000L;

    private static SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 Token
     */
    public static String createToken(Map<String, Object> claims) {
        return Jwts.builder()
                .claims(claims)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRE))
                .signWith(getSecretKey())
                .compact();
    }

    /**
     * 解析 Token
     */
    public static Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 根据用户信息生成 Token
     */
    public static String createToken(Long userId, String username, String uuid) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("user_id", userId);
        claims.put("username", username);
        claims.put("user_key", uuid);
        return createToken(claims);
    }

    /**
     * 从 Token 中获取用户名
     */
    public static String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("username", String.class);
    }

    /**
     * 从 Token 中获取用户ID
     */
    public static Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("user_id", Long.class);
    }

    /**
     * 从 Token 中获取用户Key
     */
    public static String getUserKeyFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("user_key", String.class);
    }

    /**
     * 验证 Token 是否过期
     */
    public static boolean isTokenExpired(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
}
