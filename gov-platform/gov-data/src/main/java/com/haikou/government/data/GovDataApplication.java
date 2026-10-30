package com.haikou.government.data;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * 数据服务启动类
 *
 * 功能：数据统计、报表分析、数据大屏
 * 端口：8086
 *
 * @author xhl
 * @date 2026-08-02
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.haikou")
@ComponentScan(basePackages = {"com.haikou"})
@MapperScan("com.haikou.**.mapper")
public class GovDataApplication {

    public static void main(String[] args) {
        SpringApplication.run(GovDataApplication.class, args);
        System.out.println("====================================");
        System.out.println("数据服务启动成功！端口：8086");
        System.out.println("====================================");
    }
}
