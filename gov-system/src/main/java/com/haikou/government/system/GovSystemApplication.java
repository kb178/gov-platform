package com.haikou.government.system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 系统服务启动类
 *
 * @author gov-platform
 */
@SpringBootApplication(scanBasePackages = {"com.haikou.government"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.haikou.government.api")
public class GovSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(GovSystemApplication.class, args);
    }
}
