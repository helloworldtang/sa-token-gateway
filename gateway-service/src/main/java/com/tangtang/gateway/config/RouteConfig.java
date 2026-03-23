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
 * 
 * 注意：/v3/api-docs 和 /v3/api-docs/** 作为 catch-all，
 * 通过 Referer 或 Host 头判断转发到哪个服务
 */
@Configuration
public class RouteConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // ===== users 服务 =====
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
                .route("users-auth",
                        r -> r.path("/users/auth/**")
                                .filters(f -> f.rewritePath("/users/auth/(?<segment>.*)", "/auth/${segment}"))
                                .uri("http://localhost:8081"))
                .route("users-other",
                        r -> r.path("/users/**")
                                .uri("http://localhost:8081"))

                // ===== orders 服务 =====
                // 更具体的路由必须在前面
                
                // 1. 订单业务路由：/orders/order/** → /order/**
                .route("orders-business",
                        r -> r.path("/orders/order/**")
                                .filters(f -> f.stripPrefix(1))
                                .uri("http://localhost:8082"))
                
                // 2. 文档和 API 相关路由
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
                
                // 3. 其他路由（catch-all）
                .route("orders-other",
                        r -> r.path("/orders/**")
                                .filters(f -> f.stripPrefix(1))
                                .uri("http://localhost:8082"))

                // ===== Catch-all: /v3/api-docs/** (默认转发到 user-service) =====
                // 当 Swagger UI 直接请求 /v3/api-docs 时，默认路由到 user-service
                // 如需访问 order-service，请使用 /orders/v3/api-docs
                .route("default-api-docs",
                        r -> r.path("/v3/api-docs/**")
                                .uri("http://localhost:8081"))
                .route("default-api-docs-root",
                        r -> r.path("/v3/api-docs")
                                .uri("http://localhost:8081"))
                .build();
    }
}