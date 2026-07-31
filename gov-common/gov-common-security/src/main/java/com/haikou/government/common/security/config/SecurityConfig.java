package com.haikou.government.common.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 配置
 *
 * @author gov-platform
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * 密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 安全过滤器链
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 禁用 CSRF
            .csrf(AbstractHttpConfigurer::disable)
            // 无状态会话
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // 请求授权
            .authorizeHttpRequests(auth -> auth
                // 放行的接口
                .requestMatchers(
                    "/login",
                    "/register",
                    "/captcha",
                    "/doc.html",
                    "/webjars/**",
                    "/swagger-resources/**",
                    "/v3/api-docs/**",
                    "/actuator/**"
                ).permitAll()
                // 其他接口需要认证
                .anyRequest().authenticated()
            );

        return http.build();
    }
}
