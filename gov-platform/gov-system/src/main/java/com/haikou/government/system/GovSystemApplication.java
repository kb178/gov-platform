package com.haikou.government.system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 系统服务启动类
 *
 * @author gov-platform
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableAsync
@ComponentScan(basePackages = {
    "com.haikou.government.system",
    "com.haikou.government.common"
})
public class GovSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(GovSystemApplication.class, args);
        System.out.println("========================================");
        System.out.println("系统服务启动成功！");
        System.out.println("========================================");
    }
}
