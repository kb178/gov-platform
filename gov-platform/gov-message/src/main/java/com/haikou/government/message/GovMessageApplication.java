package com.haikou.government.message;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * 消息服务启动类
 *
 * 功能：站内信、短信通知、邮件推送
 * 端口：8085
 *
 * @author xhl
 * @date 2026-08-02
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.haikou")
@ComponentScan(basePackages = {"com.haikou"})
@MapperScan("com.haikou.**.mapper")
public class GovMessageApplication {

    public static void main(String[] args) {
        SpringApplication.run(GovMessageApplication.class, args);
        System.out.println("====================================");
        System.out.println("消息服务启动成功！端口：8085");
        System.out.println("====================================");
    }
}
