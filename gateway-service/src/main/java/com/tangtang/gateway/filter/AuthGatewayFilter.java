package com.tangtang.gateway.filter;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.exception.NotLoginException;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 网关鉴权过滤器
 * 
 * 路由规则：
 * - /users/** → user-service (认证、用户管理)
 * - /orders/** → order-service (订单管理)
 */
@Component
public class AuthGatewayFilter implements GlobalFilter, Ordered {

    private static final Map<Long, List<String>> USER_PERMISSIONS = new HashMap<>();
    private static final Map<Long, List<String>> USER_ROLES = new HashMap<>();

    static {
        // admin 用户 - 拥有所有权限
        USER_PERMISSIONS.put(10001L, Arrays.asList(
            "users:list", "users:add", "users:delete",
            "orders:list", "orders:create", "orders:delete"
        ));
        USER_ROLES.put(10001L, Arrays.asList("admin"));

        // 普通用户 - 只有部分权限
        USER_PERMISSIONS.put(10002L, Arrays.asList(
            "users:list", "orders:list"
        ));
        USER_ROLES.put(10002L, Arrays.asList("user"));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();
        String token = request.getHeaders().getFirst("satoken");

        // 白名单放行
        if (isExcluded(path)) {
            return chain.filter(exchange);
        }

        // Token 为空
        if (token == null || token.isEmpty()) {
            return unauthorized(exchange, "请先登录");
        }

        try {
            // 验证 Token，获取用户ID
            Object loginId = SaManager.getStpLogic("login").getLoginIdByToken(token);
            Long userId = Long.parseLong(String.valueOf(loginId));

            // 权限校验
            String result = checkPermission(path, userId);
            if (result != null) {
                return forbidden(exchange, result);
            }

            // 将用户ID传递给后端服务
            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("satoken-user-id", String.valueOf(loginId))
                    .build();

            return chain.filter(exchange.mutate().request(modifiedRequest).build());

        } catch (NotLoginException e) {
            return unauthorized(exchange, "Token 无效或已过期");
        } catch (Exception e) {
            return unauthorized(exchange, "请先登录");
        }
    }

    private String checkPermission(String path, Long userId) {
        List<String> permissions = USER_PERMISSIONS.get(userId);

        // orders 相关权限
        if (path.contains("/orders/create")) {
            if (permissions == null || !permissions.contains("orders:create")) {
                return "缺少权限: orders:create";
            }
        } else if (path.contains("/orders/delete")) {
            if (permissions == null || !permissions.contains("orders:delete")) {
                return "缺少权限: orders:delete";
            }
        } else if (path.startsWith("/orders/")) {
            if (permissions == null || !permissions.contains("orders:list")) {
                return "缺少权限: orders:list";
            }
        }
        // users 相关权限
        else if (path.startsWith("/users/")) {
            if (permissions == null || !permissions.contains("users:list")) {
                return "缺少权限: users:list";
            }
        }
        // admin 角色
        else if (path.contains("/admin/")) {
            List<String> roles = USER_ROLES.get(userId);
            if (roles == null || !roles.contains("admin")) {
                return "缺少角色: admin";
            }
        }

        return null;
    }

    private boolean isExcluded(String path) {
        return path.contains("/favicon") ||
               path.contains("/swagger") ||
               path.contains("/v3/api-docs") ||
               path.contains("/doc.html") ||
               path.contains("/webjars") ||
               path.contains("/users/auth/login") ||
               path.contains("/users/auth/register");
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        return writeResponse(exchange, 401, message);
    }

    private Mono<Void> forbidden(ServerWebExchange exchange, String message) {
        return writeResponse(exchange, 403, message);
    }

    private Mono<Void> writeResponse(ServerWebExchange exchange, int code, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.OK);
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        String body = String.format("{\"code\":%d,\"msg\":\"%s\",\"data\":null}", code, message);
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -100;
    }
}