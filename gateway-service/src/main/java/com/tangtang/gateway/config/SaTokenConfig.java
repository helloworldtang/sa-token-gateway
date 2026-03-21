package com.tangtang.gateway.config;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.reactor.filter.SaReactorFilter;
import cn.dev33.satoken.filter.SaFilterAuthStrategy;
import cn.dev33.satoken.filter.SaFilterErrorStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.ServerWebExchange;

/**
 * 网关 Sa-Token 配置
 */
@Configuration
public class SaTokenConfig {

    @Bean
    public SaReactorFilter saTokenFilter() {
        // 认证策略
        SaFilterAuthStrategy auth = obj -> {
            ServerWebExchange exchange = (ServerWebExchange) obj;
            String token = exchange.getRequest().getHeaders().getFirst("satoken");
            if (token == null || token.isEmpty()) {
                throw new NotLoginException("Token 为空", null, null);
            }
        };

        // 异常处理策略
        SaFilterErrorStrategy error = e -> {
            int code = 401;
            String msg = "请先登录";
            if (!(e instanceof NotLoginException)) {
                code = 403;
                msg = "无权限访问";
            }
            return String.format("{\"code\":%d,\"msg\":\"%s\",\"data\":null}", code, msg);
        };

        return new SaReactorFilter()
                .addExclude("/favicon.ico")
                .addExclude("/swagger-ui.html")
                .addExclude("/swagger-ui/**")
                .addExclude("/v3/api-docs/**")
                .addExclude("/webjars/**")
                .addExclude("/doc.html")
                .addExclude("/auth/login")
                .setAuth(auth)
                .setError(error);
    }
}
