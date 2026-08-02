package com.haikou.government.common.security.filter;

import com.haikou.government.common.core.utils.JwtUtils;
import com.haikou.government.common.core.utils.StringUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

/**
 * JWT 认证过滤器
 *
 * 功能：
 * 1. 从请求头中获取 Token
 * 2. 验证 Token 是否有效
 * 3. 将用户信息存入 Spring Security 上下文
 *
 * 执行顺序：在 Spring Security 之前执行
 *
 * @author xhl
 * @date 2026-08-02
 */
@Slf4j
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * Token 请求头名称
     */
    private static final String AUTHORIZATION_HEADER = "Authorization";

    /**
     * Token 前缀
     */
    private static final String BEARER_PREFIX = "Bearer ";

    /**
     * 过滤器核心逻辑
     *
     * @param request     请求
     * @param response    响应
     * @param filterChain 过滤器链
     * @throws ServletException 异常
     * @throws IOException      异常
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // ========== 1. 获取 Token ==========
        String token = getTokenFromRequest(request);

        // ========== 2. Token 为空，直接放行 ==========
        // 由后续的 Security 配置决定是否需要认证
        if (StringUtils.isBlank(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        // ========== 3. 验证 Token ==========
        try {
            // 验证 Token 是否过期
            if (jwtUtils.isTokenExpired(token)) {
                log.warn("Token 已过期");
                filterChain.doFilter(request, response);
                return;
            }

            // 解析 Token，验证签名是否正确
            jwtUtils.parseToken(token);

            // ========== 4. 获取用户信息 ==========
            Long userId = jwtUtils.getUserIdFromToken(token);
            String username = jwtUtils.getUsernameFromToken(token);

            // ========== 5. 将用户信息存入 Security 上下文 ==========
            // 创建认证对象（参数：用户名、密码、权限列表）
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(username, null, new ArrayList<>());

            // 将用户ID存入认证对象的 details 中
            authentication.setDetails(userId);

            // 将认证对象存入 Security 上下文
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // ========== 6. 将用户ID放入请求头，传递给下游服务 ==========
            // 注意：这里不能直接修改 request，需要使用 wrapper
            // 但为了简单，我们使用 ThreadLocal 来传递用户ID
            // 后续可以通过 SecurityContextHolder 获取用户信息

            log.debug("Token 验证通过，用户：{}", username);

        } catch (Exception e) {
            log.error("Token 验证失败：{}", e.getMessage());
            // Token 无效，不设置认证信息，由后续配置决定处理方式
        }

        // ========== 7. 继续执行过滤器链 ==========
        filterChain.doFilter(request, response);
    }

    /**
     * 从请求头中获取 Token
     *
     * @param request 请求
     * @return Token（不包含 Bearer 前缀）
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.isNotBlank(authHeader) && authHeader.startsWith(BEARER_PREFIX)) {
            return authHeader.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    /**
     * 获取当前登录用户ID
     *
     * 从 Security 上下文中获取用户ID
     *
     * @return 用户ID，未登录返回 null
     */
    public static Long getCurrentUserId() {
        UsernamePasswordAuthenticationToken authentication =
                (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getDetails() instanceof Long) {
            return (Long) authentication.getDetails();
        }
        return null;
    }
}
