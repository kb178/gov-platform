package com.haikou.government.system.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j 接口文档配置
 *
 * 功能：自动生成 API 接口文档
 * 访问地址：http://localhost:8081/doc.html
 *
 * @author xhl
 * @date 2026-08-02
 */
@Configuration
public class Knife4jConfig {

    /**
     * 自定义 API 文档信息
     *
     * @return OpenAPI 文档配置
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("系统服务接口文档")           // 文档标题
                        .version("1.0.0")                   // 版本号
                        .description("用户管理、角色管理、菜单管理等接口")  // 描述
                        .contact(new Contact()              // 联系人信息
                                .name("海口政务开发团队")
                                .email("dev@gov.haikou.com"))
                        .license(new License()              // 许可证信息
                                .name("Apache 2.0")
                                .url("http://www.apache.org/licenses/LICENSE-2.0")));
    }
}
