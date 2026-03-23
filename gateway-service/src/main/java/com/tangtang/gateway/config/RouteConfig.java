package com.tangtang.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 网关路由配置
 * 
 * 路由规则：
 * - /users/**  → user-service (8081)
 * - /orders/** → order-service (8082)
 * 
 * Swagger 路由（去掉服务前缀）：
 * - /users/doc.html → /doc.html
 * - /users/v3/api-docs/** → /v3/api-docs/**
 * - /users/swagger-ui/** → /swagger-ui/**
 * - /users/webjars/** → /webjars/**
 */
@Configuration
public class RouteConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // ===== users 服务 =====
                // Swagger 路由（具体路径优先，去掉 /users 前缀）
                .route("users-doc",
                        r -> r.path("/users/doc.html")
                                .filters(f -> f.setPath("/doc.html"))
                                .uri("http://localhost:8081"))
                .route("users-api-docs-root",
                        r -> r.path("/users/v3/api-docs")
                                .filters(f -> f.setPath("/v3/api-docs"))
                                .uri("http://localhost:8081"))
                .route("users-swagger-config",
                        r -> r.path("/users/v3/api-docs/swagger-config")
                                .filters(f -> f.setPath("/v3/api-docs/swagger-config"))
                                .uri("http://localhost:8081"))
                .route("users-api-docs",
                        r -> r.path("/users/v3/api-docs/**")
                                .filters(f -> f.rewritePath("/users/v3/api-docs/(?<segment>.*)", "/v3/api-docs/${segment}"))
                                .uri("http://localhost:8081"))
                .route("users-swagger-ui",
                        r -> r.path("/users/swagger-ui/**")
                                .filters(f -> f.rewritePath("/users/swagger-ui/(?<segment>.*)", "/swagger-ui/${segment}"))
                                .uri("http://localhost:8081"))
                .route("users-webjars",
                        r -> r.path("/users/webjars/**")
                                .filters(f -> f.rewritePath("/users/webjars/(?<segment>.*)", "/webjars/${segment}"))
                                .uri("http://localhost:8081"))
                // 业务路由（通配放最后）
                .route("users-auth",
                        r -> r.path("/users/auth/**")
                                .filters(f -> f.rewritePath("/users/auth/(?<segment>.*)", "/auth/${segment}"))
                                .uri("http://localhost:8081"))
                .route("users-other",
                        r -> r.path("/users/**")
                                .uri("http://localhost:8081"))

                // ===== orders 服务 =====
                // Swagger 路由（具体路径优先，去掉 /orders 前缀）
                .route("orders-doc",
                        r -> r.path("/orders/doc.html")
                                .filters(f -> f.setPath("/doc.html"))
                                .uri("http://localhost:8082"))
                .route("orders-api-docs-root",
                        r -> r.path("/orders/v3/api-docs")
                                .filters(f -> f.setPath("/v3/api-docs"))
                                .uri("http://localhost:8082"))
                .route("orders-swagger-config",
                        r -> r.path("/orders/v3/api-docs/swagger-config")
                                .filters(f -> f.setPath("/v3/api-docs/swagger-config"))
                                .uri("http://localhost:8082"))
                .route("orders-api-docs",
                        r -> r.path("/orders/v3/api-docs/**")
                                .filters(f -> f.rewritePath("/orders/v3/api-docs/(?<segment>.*)", "/v3/api-docs/${segment}"))
                                .uri("http://localhost:8082"))
                .route("orders-swagger-ui",
                        r -> r.path("/orders/swagger-ui/**")
                                .filters(f -> f.rewritePath("/orders/swagger-ui/(?<segment>.*)", "/swagger-ui/${segment}"))
                                .uri("http://localhost:8082"))
                .route("orders-webjars",
                        r -> r.path("/orders/webjars/**")
                                .filters(f -> f.rewritePath("/orders/webjars/(?<segment>.*)", "/webjars/${segment}"))
                                .uri("http://localhost:8082"))
                // 业务路由（通配放最后，去掉 /orders 前缀转发到 /order）
                .route("orders-business",
                        r -> r.path("/orders/**")
                                .filters(f -> f.rewritePath("/orders/(?<segment>.*)", "/order/${segment}"))
                                .uri("http://localhost:8082"))
                .build();
    }
}
