package com.haikou.government.license;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * 证照服务启动类
 *
 * 功能：证照生成、电子签章、证照查验
 * 端口：8084
 *
 * @author xhl
 * @date 2026-08-02
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.haikou")
@ComponentScan(basePackages = {"com.haikou"})
@MapperScan("com.haikou.**.mapper")
public class GovLicenseApplication {

    public static void main(String[] args) {
        SpringApplication.run(GovLicenseApplication.class, args);
        System.out.println("====================================");
        System.out.println("证照服务启动成功！端口：8084");
        System.out.println("====================================");
    }
}
