package com.tangtang.user.config;

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
 * 这样 Swagger UI 请求 api-docs 时会带上正确的前缀
 */
@Configuration
public class SwaggerConfig {

    /**
     * 网关访问前缀，通过配置文件注入
     * 本地直接访问时为空，通过网关访问时为 /users
     */
    @Value("${springdoc.server-prefix:}")
    private String serverPrefix;

    @Bean
    public OpenAPI openAPI() {
        OpenAPI openAPI = new OpenAPI()
                .info(new Info()
                        .title("用户服务 API")
                        .description("用户登录、认证接口")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("码骨丹心")
                                .url("https://github.com/helloworldtang"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")));

        // 配置 server 路径，让 Swagger UI 通过网关前缀请求 api-docs
        if (serverPrefix != null && !serverPrefix.isEmpty()) {
            openAPI.servers(List.of(new Server().url(serverPrefix).description("网关访问")));
        }

        return openAPI;
    }
}
