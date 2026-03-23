package com.tangtang.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 网关应用配置属性
 * 
 * 注意：不能用 GatewayProperties（与 Spring Cloud Gateway 内置 Bean 冲突）
 * 配置前缀：app.gateway
 */
@Component
@ConfigurationProperties(prefix = "app.gateway")
public class AppGatewayProperties {

    private SwaggerConfig swagger = new SwaggerConfig();

    public SwaggerConfig getSwagger() {
        return swagger;
    }

    public void setSwagger(SwaggerConfig swagger) {
        this.swagger = swagger;
    }

    /**
     * Swagger Basic 认证配置
     * 生产环境设 enabled: false 禁用 Swagger
     */
    public static class SwaggerConfig {
        private boolean enabled = true;
        private String username = "admin";
        private String password = "swagger123";

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}
