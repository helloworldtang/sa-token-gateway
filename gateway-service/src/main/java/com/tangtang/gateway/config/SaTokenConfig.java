package com.tangtang.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.ServerWebExchange;

/**
 * 网关 Sa-Token 配置
 */
@Configuration
public class SaTokenConfig {

    @Bean
    public cn.dev33.satoken.reactor.filter.SaReactorFilter saTokenFilter() {
        return new cn.dev33.satoken.reactor.filter.SaReactorFilter()
                .addExclude("/favicon.ico")
                .addExclude("/auth/login")
                .addExclude("/api/user/auth/login")
                .setAuth(exchange -> {
                    String token = exchange.getRequest().getHeaders().getFirst("satoken");
                    if (token == null || token.isEmpty()) {
                        throw new cn.dev33.satoken.exception.NotLoginException("Token 为空", null, null);
                    }
                })
                .setError(e -> {
                    int code = 401;
                    String msg = "请先登录";
                    if (!(e instanceof cn.dev33.satoken.exception.NotLoginException)) {
                        code = 403;
                        msg = "无权限访问";
                    }
                    return String.format("{\"code\":%d,\"msg\":\"%s\",\"data\":null}", code, msg);
                });
    }
}
