package com.haikou.government.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 网关服务启动类
 *
 * @author gov-platform
 */
@SpringBootApplication
@EnableDiscoveryClient
public class GovGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GovGatewayApplication.class, args);
    }
}
