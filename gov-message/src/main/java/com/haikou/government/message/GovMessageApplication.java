package com.haikou.government.message;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 消息服务启动类
 *
 * @author gov-platform
 */
@SpringBootApplication(scanBasePackages = {"com.haikou.government"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.haikou.government.api")
public class GovMessageApplication {

    public static void main(String[] args) {
        SpringApplication.run(GovMessageApplication.class, args);
    }
}
