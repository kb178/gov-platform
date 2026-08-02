package com.haikou.government.approval;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * 审批服务启动类
 *
 * 功能：审批流程、审批记录、催办督办
 * 端口：8083
 *
 * @author xhl
 * @date 2026-08-02
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.haikou")
@ComponentScan(basePackages = {"com.haikou"})
@MapperScan("com.haikou.**.mapper")
public class GovApprovalApplication {

    public static void main(String[] args) {
        SpringApplication.run(GovApprovalApplication.class, args);
        System.out.println("====================================");
        System.out.println("审批服务启动成功！端口：8083");
        System.out.println("====================================");
    }
}
