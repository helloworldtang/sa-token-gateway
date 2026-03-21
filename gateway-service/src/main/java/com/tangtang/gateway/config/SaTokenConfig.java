package com.tangtang.gateway.config;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.reactor.filter.SaReactorFilter;
import cn.dev33.satoken.stp.StpLogic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.ServerWebExchange;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 网关 Sa-Token 配置 - 方案2
 * 
 * 网关集中鉴权：
 * 1. 验证 Token
 * 2. 校验权限
 * 3. 后端服务只验证登录态
 */
@Configuration
public class SaTokenConfig {

    /**
     * 用户权限数据（实际应从数据库查询）
     */
    private static final Map<Long, List<String>> USER_PERMISSIONS = new HashMap<>();

    static {
        // admin 用户 - 拥有所有权限
        USER_PERMISSIONS.put(10001L, Arrays.asList(
            "user:list", "user:add", "user:delete",
            "order:list", "order:create", "order:delete"
        ));

        // 普通用户 - 只有部分权限
        USER_PERMISSIONS.put(10002L, Arrays.asList(
            "user:list", "order:list"
        ));
    }

    @Bean
    public SaReactorFilter saTokenFilter() {
        return new SaReactorFilter()
                // 放行白名单
                .addExclude("/favicon.ico")
                .addExclude("/swagger-ui.html")
                .addExclude("/swagger-ui/**")
                .addExclude("/v3/api-docs/**")
                .addExclude("/webjars/**")
                .addExclude("/doc.html")
                .addExclude("/user/auth/login")
                // 认证函数
                .setAuth(r -> {
                    ServerWebExchange exchange = (ServerWebExchange) r;
                    String path = exchange.getRequest().getPath().value();
                    String token = exchange.getRequest().getHeaders().getFirst("satoken");

                    // Token 为空
                    if (token == null || token.isEmpty()) {
                        throw new NotLoginException("Token 为空", null, null);
                    }

                    // 验证 Token，获取用户ID
                    Object loginId = SaManager.getStpLogic("login").getLoginIdByToken(token);
                    Long userId = Long.parseLong(String.valueOf(loginId));

                    // 权限校验
                    String requiredPermission = getRequiredPermission(path);
                    if (requiredPermission != null) {
                        List<String> permissions = USER_PERMISSIONS.get(userId);
                        if (permissions == null || !permissions.contains(requiredPermission)) {
                            throw new NotPermissionException(requiredPermission);
                        }
                    }
                })
                // 异常处理
                .setError(e -> {
                    int code = 401;
                    String msg = "请先登录";
                    if (e instanceof NotPermissionException) {
                        code = 403;
                        msg = "缺少权限: " + e.getMessage();
                    }
                    return String.format("{\"code\":%d,\"msg\":\"%s\",\"data\":null}", code, msg);
                });
    }

    /**
     * 根据路径获取所需权限
     */
    private String getRequiredPermission(String path) {
        if (path.contains("/order/list") || path.contains("/order/my")) {
            return "order:list";
        }
        if (path.contains("/order/create")) {
            return "order:create";
        }
        if (path.contains("/order/delete")) {
            return "order:delete";
        }
        if (path.contains("/user/")) {
            return "user:list";
        }
        return null; // 不需要权限校验
    }
}
