package com.tangtang.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 网关配置属性
 * 
 * 支持配置中心动态更新（Nacos/Apollo 等）
 */
@Component
@ConfigurationProperties(prefix = "app.gateway")
public class AppGatewayProperties {

    private SwaggerConfig swagger = new SwaggerConfig();
    private Map<String, UserPermission> userPermissions = new HashMap<>();

    public SwaggerConfig getSwagger() {
        return swagger;
    }

    public void setSwagger(SwaggerConfig swagger) {
        this.swagger = swagger;
    }

    public Map<String, UserPermission> getUserPermissions() {
        return userPermissions;
    }

    public void setUserPermissions(Map<String, UserPermission> userPermissions) {
        this.userPermissions = userPermissions;
    }

    /**
     * Swagger 配置
     */
    public static class SwaggerConfig {
        private boolean enabled = true;
        private String username = "admin";
        private String password = "swagger123";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    /**
     * 用户权限数据
     */
    public static class UserPermission {
        private List<String> permissions = new ArrayList<>();
        private List<String> roles = new ArrayList<>();

        public List<String> getPermissions() {
            return permissions;
        }

        public void setPermissions(List<String> permissions) {
            this.permissions = permissions;
        }

        public List<String> getRoles() {
            return roles;
        }

        public void setRoles(List<String> roles) {
            this.roles = roles;
        }
    }
}
