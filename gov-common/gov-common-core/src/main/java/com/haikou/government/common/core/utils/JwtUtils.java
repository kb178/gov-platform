package com.haikou.government.common.core.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


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
@Component
public class JwtUtils {

    /** 密钥（从配置文件读取） */
    @Value("${jwt.secret:gov-platform-jwt-secret-key-must-be-at-least-256-bits-long}")
    private String secret;

    /** Token 有效期（毫秒），默认24小时 */
    @Value("${jwt.expire:86400000}")
    private long expire;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    private SecretKey getSecretKey() {
        return secretKey;
    }

    /**
     * 生成 Token
     */
    public String createToken(Map<String, Object> claims) {
        return Jwts.builder()
                .claims(claims)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expire))
                .signWith(getSecretKey())
                .compact();
    }

    /**
     * 解析 Token
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 根据用户信息生成 Token
     */
    public String createToken(Long userId, String username, String uuid) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("user_id", userId);
        claims.put("username", username);
        claims.put("user_key", uuid);
        return createToken(claims);
    }

    /**
     * 从 Token 中获取用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("username", String.class);
    }

    /**
     * 从 Token 中获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("user_id", Long.class);
    }

    /**
     * 从 Token 中获取用户Key
     */
    public String getUserKeyFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("user_key", String.class);
    }

    /**
     * 验证 Token 是否过期
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
}
