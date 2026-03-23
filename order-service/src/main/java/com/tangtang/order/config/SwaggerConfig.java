package com.tangtang.order.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger 配置类
 * 
 * 通过 server 配置告知 Swagger UI 实际访问路径（网关前缀）
 */
@Configuration
public class SwaggerConfig {

    @Value("${springdoc.server-prefix:}")
    private String serverPrefix;

    @Bean
    public OpenAPI openAPI() {
        OpenAPI openAPI = new OpenAPI()
                .info(new Info()
                        .title("订单服务 API")
                        .description("订单列表、创建订单接口")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("码骨丹心")
                                .url("https://github.com/helloworldtang"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")));

        if (serverPrefix != null && !serverPrefix.isEmpty()) {
            openAPI.servers(List.of(
                    new Server().url(serverPrefix).description("通过网关访问"),
                    new Server().url("").description("直接访问")
            ));
        }

        return openAPI;
    }
}
