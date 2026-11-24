package com.haikou.government.gateway.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.ResourceHandlerRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;

/**
 * WebFlux 静态资源配置
 *
 * Spring Cloud Gateway 是 WebFlux 架构，webjar 的静态资源需要手动映射，
 * 否则 Swagger UI 的 JS/CSS 文件找不到（报 404 No static resource）
 *
 * @author xhl
 * @since 2026-11-24
 */
@Configuration
public class WebFluxConfig implements WebFluxConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // /webjars/** → 从 classpath:/META-INF/resources/webjars/ 下找
        // Swagger UI 的 index.html、JS、CSS 都在这个目录
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }
}
