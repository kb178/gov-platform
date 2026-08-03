package com.haikou.government.gateway.filter;

import com.haikou.government.common.core.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

/**
 * 网关全局过滤器 - JWT Token 校验
 *
 * 作用：
 * 1. 拦截所有请求，校验 JWT Token
 * 2. 白名单路径直接放行（登录、注册、验证码等）
 * 3. Token 无效返回 401 未授权
 *
 * @author xhl
 * @since 2026-08-01
 */
@Slf4j
@Component
public class AuthFilter implements GlobalFilter, Ordered {

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * 白名单路径（不需要 Token）
     * 注意：路径要匹配 Gateway 路由转发前的路径（带 /system 前缀）
     */
    private static final List<String> WHITE_LIST = Arrays.asList(
            "/system/sysUser/sendCode",    // 发送验证码
            "/system/sysUser/register",     // 用户注册
            "/system/sysUser/login",        // 密码登录
            "/system/sysUser/smsLogin",     // 验证码登录
            "/doc.html",                    // Knife4j 接口文档
            "/webjars/",                    // Knife4j 静态资源
            "/swagger-resources/",          // Swagger 资源
            "/v3/api-docs/"                 // OpenAPI 文档
    );

    /**
     * 过滤逻辑
     *
     * @param exchange 请求上下文
     * @param chain    过滤器链
     * @return Mono<Void>
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // ========== 第一步：白名单路径直接放行 ==========
        if (isWhiteList(path)) {
            return chain.filter(exchange);
        }

        // ========== 第二步：获取 Token ==========
        String token = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (!StringUtils.hasText(token)) {
            log.warn("请求 {} 缺少 Token", path);
            return unauthorized(exchange);
        }

        // ========== 第三步：校验 Token ==========
        try {
            // 去掉 Bearer 前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            // 验证 Token 是否过期
            if (jwtUtils.isTokenExpired(token)) {
                log.warn("请求 {} Token 已过期", path);
                return unauthorized(exchange);
            }

            // 解析 Token 验证签名是否正确
            jwtUtils.parseToken(token);

            // ========== 第四步：将用户信息放入请求头，传递给下游服务 ==========
            Long userId = jwtUtils.getUserIdFromToken(token);
            String username = jwtUtils.getUsernameFromToken(token);

            // 将用户信息放入请求头
            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-User-Id", String.valueOf(userId))
                    .header("X-User-Name", username)
                    .build();

            ServerWebExchange mutatedExchange = exchange.mutate()
                    .request(mutatedRequest)
                    .build();

            log.debug("请求 {} Token 校验通过，用户：{}", path, username);
            return chain.filter(mutatedExchange);

        } catch (Exception e) {
            log.error("Token 校验异常：{}", e.getMessage());
            return unauthorized(exchange);
        }
    }

    /**
     * 判断是否在白名单中
     *
     * @param path 请求路径
     * @return true=白名单，false=需要校验
     */
    private boolean isWhiteList(String path) {
        return WHITE_LIST.stream().anyMatch(path::startsWith);
    }

    /**
     * 返回 401 未授权响应
     *
     * @param exchange 请求上下文
     * @return Mono<Void>
     */
    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        String body = "{\"code\":401,\"msg\":\"未授权，请先登录\",\"data\":null}";
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes())));
    }

    /**
     * 过滤器顺序（值越小，优先级越高）
     *
     * @return 顺序值
     */
    @Override
    public int getOrder() {
        return -100;
    }
}
