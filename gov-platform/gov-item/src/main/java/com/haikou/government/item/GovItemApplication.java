package com.haikou.government.item;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * 事项服务启动类
 *
 * 功能：事项定义、表单配置、材料管理
 * 端口：8082
 *
 * @author xhl
 * @date 2026-08-02
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.haikou")
@ComponentScan(basePackages = {"com.haikou"})
@MapperScan("com.haikou.**.mapper")
public class GovItemApplication {

    public static void main(String[] args) {
        SpringApplication.run(GovItemApplication.class, args);
        System.out.println("====================================");
        System.out.println("事项服务启动成功！端口：8082");
        System.out.println("====================================");
    }
}
