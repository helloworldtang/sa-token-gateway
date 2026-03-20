package com.example.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 网关路由配置
 * 
 * 配置服务转发规则
 * 
 * @author 码骨丹心
 */
@Configuration
public class GatewayRouteConfig {

    /**
     * 配置路由规则
     */
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // 用户服务路由
                .route("user-service", r -> r.path("/api/user/**")
                        .uri("http://localhost:8081"))
                // 订单服务路由
                .route("order-service", r -> r.path("/api/order/**")
                        .uri("http://localhost:8082"))
                .build();
    }
}
