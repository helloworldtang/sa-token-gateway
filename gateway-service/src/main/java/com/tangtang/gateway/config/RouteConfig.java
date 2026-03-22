package com.tangtang.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 网关路由配置
 */
@Configuration
public class RouteConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // ===== user-service =====
                // Swagger 具体路径
                .route(r -> r.path("/user/doc.html").filters(f -> f.setPath("/doc.html")).uri("http://localhost:8081"))
                .route(r -> r.path("/user/swagger-ui/**").filters(f -> f.stripPrefix(1)).uri("http://localhost:8081"))
                .route(r -> r.path("/user/v3/api-docs/**").filters(f -> f.stripPrefix(1)).uri("http://localhost:8081"))
                .route(r -> r.path("/user/webjars/**").filters(f -> f.stripPrefix(1)).uri("http://localhost:8081"))
                // 认证和业务接口
                .route(r -> r.path("/auth/**").uri("http://localhost:8081"))
                .route(r -> r.path("/user/**").uri("http://localhost:8081"))

                // ===== order-service =====
                // Swagger 具体路径
                .route(r -> r.path("/order/doc.html").filters(f -> f.setPath("/doc.html")).uri("http://localhost:8082"))
                .route(r -> r.path("/order/swagger-ui/**").filters(f -> f.stripPrefix(1)).uri("http://localhost:8082"))
                .route(r -> r.path("/order/v3/api-docs/**").filters(f -> f.stripPrefix(1)).uri("http://localhost:8082"))
                .route(r -> r.path("/order/webjars/**").filters(f -> f.stripPrefix(1)).uri("http://localhost:8082"))
                // 业务接口
                .route(r -> r.path("/order/**").uri("http://localhost:8082"))
                .build();
    }
}
