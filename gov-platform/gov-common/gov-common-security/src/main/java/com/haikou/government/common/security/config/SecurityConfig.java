package com.haikou.government.common.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 安全配置类
 *
 * 核心作用：
 * 1. 配置哪些接口需要登录才能访问
 * 2. 配置哪些接口可以匿名访问（如登录、注册、发送验证码）
 * 3. 关闭 CSRF、Session 等不需要的功能
 *
 * @author xhl
 * @since 2026-08-01
 */
@Configuration // 声明为配置类，Spring 启动时会自动加载
@EnableWebSecurity // 启用 Spring Security 安全功能
public class SecurityConfig {

    /**
     * 配置安全过滤器链
     *
     * @param http HttpSecurity 对象，用于配置安全策略
     * @return SecurityFilterChain 安全过滤器链
     * @throws Exception 配置异常
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // ========== 1. 禁用 CSRF ==========
            // CSRF（跨站请求伪造）：攻击者诱导用户访问恶意网站，利用用户的登录状态发起请求
            // 为什么禁用：前后端分离项目使用 JWT Token，不依赖 Cookie，所以不需要 CSRF 保护
            .csrf(csrf -> csrf.disable())

            // ========== 2. 配置 Session 策略 ==========
            // STATELESS（无状态）：不创建 HttpSession，完全依赖 JWT Token 进行身份验证
            // 为什么：前后端分离项目，服务器不保存会话状态，每次请求都要带 Token
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // ========== 3. 配置请求授权规则 ==========
            .authorizeHttpRequests(auth -> auth
                // 配置哪些路径可以匿名访问（不需要登录）
                .requestMatchers(
                        "/sysUser/sendCode",    // 发送验证码
                        "/sysUser/register",     // 用户注册
                        "/sysUser/login",        // 密码登录
                        "/sysUser/smsLogin",     // 验证码登录
                        "/doc.html",             // Knife4j 接口文档
                        "/webjars/**",           // Knife4j 静态资源
                        "/swagger-resources/**", // Swagger 资源
                        "/v3/api-docs/**"        // OpenAPI 文档
                ).permitAll() // permitAll() 允许所有人访问，不需要认证

                // 其他所有请求都需要登录认证
                .anyRequest().authenticated()
            );

        // 构建并返回安全过滤器链
        return http.build();
    }
}
