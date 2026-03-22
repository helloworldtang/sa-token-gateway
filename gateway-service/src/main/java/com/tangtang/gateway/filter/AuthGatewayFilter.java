package com.tangtang.gateway.filter;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.stp.StpInterface;
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
 * 职责：
 * 1. Token 验证
 * 2. 权限校验（手动调用 StpInterface）
 * 3. 将 userId 传递给后端服务
 */
@Component
public class AuthGatewayFilter implements GlobalFilter, Ordered {

    /**
     * 用户权限数据
     */
    private static final Map<Long, List<String>> USER_PERMISSIONS = new HashMap<>();
    private static final Map<Long, List<String>> USER_ROLES = new HashMap<>();

    static {
        // admin 用户
        USER_PERMISSIONS.put(10001L, Arrays.asList(
            "user:list", "user:add", "user:delete",
            "order:list", "order:create", "order:delete"
        ));
        USER_ROLES.put(10001L, Arrays.asList("admin"));

        // 普通用户
        USER_PERMISSIONS.put(10002L, Arrays.asList(
            "user:list", "order:list"
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
            String requiredPermission = getRequiredPermission(path);
            if (requiredPermission != null) {
                // 手动检查权限
                if (!hasPermission(userId, requiredPermission)) {
                    return forbidden(exchange, "缺少权限: " + requiredPermission);
                }
            }

            // 角色校验
            String requiredRole = getRequiredRole(path);
            if (requiredRole != null) {
                if (!hasRole(userId, requiredRole)) {
                    return forbidden(exchange, "缺少角色: " + requiredRole);
                }
            }

            // 将用户ID传递给后端服务
            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("satoken-user-id", String.valueOf(loginId))
                    .build();

            return chain.filter(exchange.mutate().request(modifiedRequest).build());

        } catch (NotLoginException e) {
            return unauthorized(exchange, "Token 无效或已过期");
        } catch (NotPermissionException e) {
            return forbidden(exchange, "缺少权限: " + e.getPermission());
        } catch (Exception e) {
            return unauthorized(exchange, "请先登录");
        }
    }

    private boolean hasPermission(Long userId, String permission) {
        List<String> permissions = USER_PERMISSIONS.get(userId);
        return permissions != null && permissions.contains(permission);
    }

    private boolean hasRole(Long userId, String role) {
        List<String> roles = USER_ROLES.get(userId);
        return roles != null && roles.contains(role);
    }

    private boolean isExcluded(String path) {
        return path.contains("/favicon") ||
               path.contains("/swagger") ||
               path.contains("/v3/api-docs") ||
               path.contains("/doc.html") ||
               path.contains("/webjars") ||
               path.contains("/auth/login") ||
               path.contains("/auth/register");
    }

    private String getRequiredPermission(String path) {
        if (path.equals("/order/create")) return "order:create";
        if (path.equals("/order/delete")) return "order:delete";
        if (path.startsWith("/order/")) return "order:list";
        if (path.startsWith("/user/")) return "user:list";
        return null;
    }

    private String getRequiredRole(String path) {
        if (path.equals("/auth/admin/data")) return "admin";
        return null;
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
