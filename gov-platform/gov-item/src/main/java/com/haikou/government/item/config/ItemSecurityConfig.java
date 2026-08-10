package com.haikou.government.item.config;

import com.haikou.government.common.security.filter.JwtAuthFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * 事项服务安全配置
 *
 * 覆盖默认安全配置，允许公开访问事项相关接口
 *
 * @author xhl
 * @since 2026-08-10
 */
@Slf4j
@Configuration
@EnableWebSecurity
@Order(1)
public class ItemSecurityConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain itemFilterChain(HttpSecurity http) throws Exception {
        log.info("========== ItemSecurityConfig 已加载 ==========");
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth
                // 事项分类 - 测试阶段全部开放
                .requestMatchers("/itemCategory/**").permitAll()
                // 事项信息 - 公开查询
                .requestMatchers("/itemInfo/list").permitAll()
                .requestMatchers("/itemInfo/detail/**").permitAll()
                .requestMatchers("/itemInfo/category/**").permitAll()
                // 材料模板 - 公开查询
                .requestMatchers("/itemMaterialTemplate/list").permitAll()
                .requestMatchers("/itemMaterialTemplate/{materialId}").permitAll()
                // 表单模板 - 公开查询
                .requestMatchers("/itemFormTemplate/item/**").permitAll()
                .requestMatchers("/itemFormTemplate/{templateId}").permitAll()
                // 其他接口需要认证
                .anyRequest().authenticated()
            );
        return http.build();
    }
}
