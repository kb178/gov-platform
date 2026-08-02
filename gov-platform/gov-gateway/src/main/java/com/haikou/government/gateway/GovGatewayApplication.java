package com.haikou.government.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

/**
 * 网关服务启动类
 *
 * ==================== Gateway 网关简介 ====================
 *
 * Gateway 是什么？
 * - 所有请求的统一入口（类似公司前台）
 * - 负责路由转发、权限校验、跨域处理等
 *
 * 为什么需要 Gateway？
 * - 没有 Gateway：前端要记很多服务地址（8081、8082、8083...）
 * - 有 Gateway：前端只需要一个地址（8080），Gateway 负责分发
 *
 * ==================== 核心功能 ====================
 *
 * 1. 路由转发：根据路径转发到不同服务
 *    /system/** → gov-system:8081
 *    /item/**   → gov-item:8082
 *
 * 2. Token 校验：统一验证 JWT Token（AuthFilter）
 *    白名单路径直接放行（登录、注册等）
 *    其他路径必须携带有效 Token
 *
 * 3. 跨域处理：解决前端跨域问题（CorsConfig）
 *    前端 http://localhost:5173 访问后端 http://localhost:8080
 *
 * ==================== 启动注解说明 ====================
 *
 * @SpringBootApplication
 * - Spring Boot 启动注解
 * - 包含 @Configuration、@EnableAutoConfiguration、@ComponentScan
 *
 * @EnableDiscoveryClient
 * - 启用服务发现（从 Nacos 获取服务地址）
 * - Gateway 需要知道各个微服务的地址才能转发
 *
 * @ComponentScan
 * - 组件扫描，告诉 Spring 去哪些包下找 Bean
 * - "com.haikou.government.gateway"：Gateway 自己的代码
 * - "com.haikou.government.common"：公共模块的代码（如 JwtUtils）
 *
 * ==================== 注意事项 ====================
 *
 * 1. Gateway 使用 WebFlux（响应式），不是 Spring MVC
 *    - 不能使用 @Autowired 注入 HttpServletRequest
 *    - 使用 Mono/Flux，不是传统的 Servlet
 *
 * 2. Gateway 不能引入 spring-boot-starter-web
 *    - 会和 WebFlux 冲突，启动报错
 *
 * 3. 路由配置在 application.yml 中
 *    - id：路由唯一标识
 *    - uri：目标服务（lb:// 表示从 Nacos 获取）
 *    - predicates：匹配条件（路径匹配）
 *    - filters：过滤器（如去掉路径前缀）
 *
 * ==================== 端口规划 ====================
 *
 * Gateway：8080（统一入口）
 * gov-system：8081（系统服务）
 * gov-item：8082（事项服务）
 * gov-approval：8083（审批服务）
 * gov-license：8084（证照服务）
 * gov-message：8085（消息服务）
 * gov-data：8086（数据服务）
 *
 * @author xhl
 * @since 2026-08-02
 */
@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(basePackages = {
    "com.haikou.government.gateway",   // 扫描 Gateway 自己的代码
    "com.haikou.government.common"     // 扫描公共模块的代码（JwtUtils 等）
})
public class GovGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GovGatewayApplication.class, args);
        System.out.println("========================================");
        System.out.println("网关服务启动成功！");
        System.out.println("访问地址：http://localhost:8080");
        System.out.println("========================================");
    }
}
