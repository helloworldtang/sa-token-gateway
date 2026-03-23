package com.tangtang.gateway.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 网关 Swagger 聚合配置
 * 
 * 通过下拉菜单展示不同后端服务的 API 文档
 * 
 * 访问方式：
 * - http://localhost:8080/swagger-ui.html
 * - http://localhost:8080/doc.html (Knife4j)
 */
@Configuration
public class SwaggerConfig {

    /**
     * 网关 OpenAPI 信息
     */
    @Bean
    public OpenAPI gatewayOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API 网关")
                        .description("统一 API 入口，聚合所有微服务文档")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("码骨丹心")
                                .url("https://github.com/helloworldtang"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("网关地址")
                ));
    }

    /**
     * 用户服务 API 分组
     * 
     * 通过 /v3/api-docs/users 访问用户服务文档
     */
    @Bean
    public GroupedOpenApi usersApi() {
        return GroupedOpenApi.builder()
                .group("用户服务")
                .displayName("用户服务 - User Service")
                .pathsToMatch("/users/**")
                .build();
    }

    /**
     * 订单服务 API 分组
     * 
     * 通过 /v3/api-docs/orders 访问订单服务文档
     */
    @Bean
    public GroupedOpenApi ordersApi() {
        return GroupedOpenApi.builder()
                .group("订单服务")
                .displayName("订单服务 - Order Service")
                .pathsToMatch("/orders/**")
                .build();
    }
}
