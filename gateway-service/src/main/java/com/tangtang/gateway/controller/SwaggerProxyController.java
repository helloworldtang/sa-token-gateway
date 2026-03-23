package com.tangtang.gateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Swagger 文档代理控制器
 * 
 * 代理后端服务的 /v3/api-docs 请求到网关
 * 这样 Swagger UI 可以通过网关访问所有后端服务的文档
 */
@RestController
@RequestMapping("/v3/api-docs")
public class SwaggerProxyController {

    @Autowired
    private WebClient.Builder webClientBuilder;

    private static final String USERS_SERVICE_URL = "http://localhost:8081";
    private static final String ORDERS_SERVICE_URL = "http://localhost:8082";

    /**
     * 代理用户服务的 API 文档
     */
    @GetMapping("/users")
    public Mono<ResponseEntity<Object>> getUsersApiDocs() {
        return webClientBuilder.build()
                .get()
                .uri(USERS_SERVICE_URL + "/v3/api-docs")
                .retrieve()
                .toEntity(Object.class)
                .onErrorResume(e -> Mono.just(ResponseEntity.status(500).body((Object)"Failed to fetch users API docs")));
    }

    /**
     * 代理订单服务的 API 文档
     */
    @GetMapping("/orders")
    public Mono<ResponseEntity<Object>> getOrdersApiDocs() {
        return webClientBuilder.build()
                .get()
                .uri(ORDERS_SERVICE_URL + "/v3/api-docs")
                .retrieve()
                .toEntity(Object.class)
                .onErrorResume(e -> Mono.just(ResponseEntity.status(500).body((Object)"Failed to fetch orders API docs")));
    }

    /**
     * 代理用户服务的 Swagger 配置
     */
    @GetMapping("/users/swagger-config")
    public Mono<ResponseEntity<Object>> getUsersSwaggerConfig() {
        return webClientBuilder.build()
                .get()
                .uri(USERS_SERVICE_URL + "/v3/api-docs/swagger-config")
                .retrieve()
                .toEntity(Object.class)
                .onErrorResume(e -> Mono.just(ResponseEntity.status(500).body((Object)"Failed to fetch users swagger config")));
    }

    /**
     * 代理订单服务的 Swagger 配置
     */
    @GetMapping("/orders/swagger-config")
    public Mono<ResponseEntity<Object>> getOrdersSwaggerConfig() {
        return webClientBuilder.build()
                .get()
                .uri(ORDERS_SERVICE_URL + "/v3/api-docs/swagger-config")
                .retrieve()
                .toEntity(Object.class)
                .onErrorResume(e -> Mono.just(ResponseEntity.status(500).body((Object)"Failed to fetch orders swagger config")));
    }
}
