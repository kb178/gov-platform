package com.haikou.government.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * 跨域配置
 *
 * 什么是跨域？
 * 浏览器的同源策略限制：协议、域名、端口任一不同就是跨域
 * 例如：前端 http://localhost:5173 访问后端 http://localhost:8080 就是跨域
 *
 * 为什么在 Gateway 统一配置？
 * 1. 不用每个微服务都写跨域配置
 * 2. 统一管理，方便维护
 *
 * @author xhl
 * @since 2026-08-01
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsFilter() {
        // 1. 创建跨域配置
        CorsConfiguration config = new CorsConfiguration();

        // 允许所有来源（生产环境应该指定具体域名）
        config.addAllowedOriginPattern("*");

        // 允许所有请求头
        config.addAllowedHeader("*");

        // 允许所有请求方法（GET、POST、PUT、DELETE 等）
        config.addAllowedMethod("*");

        // 允许携带 Cookie
        config.setAllowCredentials(true);

        // 预检请求缓存时间（秒）
        config.setMaxAge(3600L);

        // 暴露响应头（前端可以获取的响应头）
        config.addExposedHeader("Authorization");

        // 2. 创建配置源
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        // 3. 创建过滤器
        return new CorsWebFilter(source);
    }
}
