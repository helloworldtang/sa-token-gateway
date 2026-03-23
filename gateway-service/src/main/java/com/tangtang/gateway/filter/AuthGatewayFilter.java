package com.tangtang.gateway.filter;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.exception.NotLoginException;
import com.tangtang.gateway.config.AppGatewayProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

/**
 * 网关鉴权过滤器
 * 
 * 职责：
 * 1. Swagger Basic 认证（生产环境保护）
 * 2. Token 验证
 * 3. 权限校验（从配置中心读取，支持动态更新）
 * 4. 将 userId 传递给后端服务
 */
@Component
public class AuthGatewayFilter implements GlobalFilter, Ordered {

    @Autowired
    private AppGatewayProperties gatewayProperties;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();
        String token = request.getHeaders().getFirst("satoken");

        // Swagger 路径处理
        if (isSwaggerPath(path)) {
            // 生产环境禁用 Swagger
            if (!gatewayProperties.getSwagger().isEnabled()) {
                return forbidden(exchange, "Swagger 已在生产环境禁用");
            }
            // Basic 认证
            String authResult = checkBasicAuth(request);
            if (authResult != null) {
                return requireBasicAuth(exchange, authResult);
            }
            return chain.filter(exchange);
        }

        // 白名单放行（登录接口）
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

            // 权限校验（从配置中心读取）
            String permissionError = checkPermission(path, userId);
            if (permissionError != null) {
                return forbidden(exchange, permissionError);
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

    /**
     * Swagger Basic 认证校验
     */
    private String checkBasicAuth(ServerHttpRequest request) {
        String authorization = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith("Basic ")) {
            return "需要 Basic 认证";
        }
        try {
            String credentials = new String(Base64.getDecoder().decode(authorization.substring(6)));
            String[] parts = credentials.split(":", 2);
            if (parts.length == 2) {
                String username = parts[0];
                String password = parts[1];
                AppGatewayProperties.SwaggerConfig swagger = gatewayProperties.getSwagger();
                if (swagger.getUsername().equals(username) && swagger.getPassword().equals(password)) {
                    return null; // 认证通过
                }
            }
        } catch (Exception ignored) {}
        return "用户名或密码错误";
    }

    /**
     * 权限校验（从配置中心读取，支持动态更新）
     */
    private String checkPermission(String path, Long userId) {
        AppGatewayProperties.UserPermission userPermission =
                gatewayProperties.getUserPermissions().get(String.valueOf(userId));

        if (userPermission == null) {
            return "用户不存在";
        }

        List<String> permissions = userPermission.getPermissions();
        List<String> roles = userPermission.getRoles();

        // orders 相关权限
        if (path.contains("/orders/create")) {
            if (!permissions.contains("orders:create")) return "缺少权限: orders:create";
        } else if (path.contains("/orders/delete")) {
            if (!permissions.contains("orders:delete")) return "缺少权限: orders:delete";
        } else if (path.startsWith("/orders/")) {
            if (!permissions.contains("orders:list")) return "缺少权限: orders:list";
        }
        // users 相关权限
        else if (path.startsWith("/users/")) {
            if (!permissions.contains("users:list")) return "缺少权限: users:list";
        }
        // admin 角色
        else if (path.contains("/admin/")) {
            if (!roles.contains("admin")) return "缺少角色: admin";
        }

        return null;
    }

    private boolean isSwaggerPath(String path) {
        return path.contains("/doc.html") ||
               path.contains("/swagger-ui") ||
               path.contains("/v3/api-docs") ||
               path.contains("/webjars");
    }

    private boolean isExcluded(String path) {
        return path.contains("/favicon") ||
               path.contains("/users/auth/login") ||
               path.contains("/users/auth/register");
    }

    private Mono<Void> requireBasicAuth(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("WWW-Authenticate", "Basic realm=\"Swagger UI\"");
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        String body = String.format("{\"code\":401,\"msg\":\"%s\",\"data\":null}", message);
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
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
