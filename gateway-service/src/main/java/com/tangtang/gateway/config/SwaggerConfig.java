package com.tangtang.gateway.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 网关 Swagger 聚合配置
 * 
 * 方案：网关本身不生成 API 文档，而是代理后端服务的文档
 * 通过 Swagger UI 的 urls 配置，在下拉菜单中展示所有后端服务
 * 
 * 访问方式：
 * - http://localhost:8080/swagger-ui.html (Swagger UI)
 * - http://localhost:8080/doc.html (Knife4j)
 * 
 * 下拉菜单中的选项：
 * - 用户服务: /v3/api-docs/users
 * - 订单服务: /v3/api-docs/orders
 */
@Configuration
public class SwaggerConfig {

    /**
     * 网关 OpenAPI 信息
     * 
     * 这只是网关本身的信息，实际的 API 文档来自后端服务
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
}
