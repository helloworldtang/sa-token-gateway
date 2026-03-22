package com.tangtang.gateway.filter;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.router.SaRouter;
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

/**
 * 网关鉴权过滤器
 * 
 * 职责：
 * 1. Token 验证
 * 2. 权限校验（调用 StpInterfaceImpl 查询权限）
 * 3. 将 userId 传递给后端服务
 */
@Component
public class AuthGatewayFilter implements GlobalFilter, Ordered {

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

            // 权限校验
            SaRouter.match(path, "/order/create", router -> 
                SaManager.getStpLogic("login").checkPermission("order:create"));
            SaRouter.match(path, "/order/delete", router -> 
                SaManager.getStpLogic("login").checkPermission("order:delete"));
            SaRouter.match(path, "/order/**", router -> 
                SaManager.getStpLogic("login").checkPermission("order:list"));
            SaRouter.match(path, "/user/**", router -> 
                SaManager.getStpLogic("login").checkPermission("user:list"));
            SaRouter.match(path, "/auth/admin/**", router -> 
                SaManager.getStpLogic("login").checkRole("admin"));

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

    private boolean isExcluded(String path) {
        return path.contains("/favicon") ||
               path.contains("/swagger") ||
               path.contains("/v3/api-docs") ||
               path.contains("/doc.html") ||
               path.contains("/webjars") ||
               path.contains("/auth/login") ||
               path.contains("/auth/register");
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
