package com.haikou.government.license;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 证照服务启动类
 *
 * @author gov-platform
 */
@SpringBootApplication(scanBasePackages = {"com.haikou.government"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.haikou.government.api")
public class GovLicenseApplication {

    public static void main(String[] args) {
        SpringApplication.run(GovLicenseApplication.class, args);
    }
}
