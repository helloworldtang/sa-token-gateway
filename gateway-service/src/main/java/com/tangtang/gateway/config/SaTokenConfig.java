package com.tangtang.gateway.config;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.reactor.filter.SaReactorFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.ServerWebExchange;

/**
 * 网关 Sa-Token 配置
 * 
 * 网关集中鉴权：
 * 1. 验证 Token（只验证登录态，不校验权限）
 * 2. 权限校验由后端服务各自处理
 * 
 * 注意：Sa-Token 的 @SaCheckPermission 注解需要 StpInterfaceImpl，
 * 因此后端服务仍需配置（可以抽取到公共模块）
 */
@Configuration
public class SaTokenConfig {

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
                // 认证函数：只校验 Token 是否有效
                .setAuth(r -> {
                    ServerWebExchange exchange = (ServerWebExchange) r;
                    String token = exchange.getRequest().getHeaders().getFirst("satoken");
                    if (token == null || token.isEmpty()) {
                        throw new NotLoginException("Token 为空", null, null);
                    }
                    // Sa-Token 会自动验证 Token 有效性
                })
                // 异常处理
                .setError(e -> {
                    int code = 401;
                    String msg = "请先登录";
                    if (e instanceof NotLoginException) {
                        msg = "Token 无效或已过期";
                    }
                    return String.format("{\"code\":%d,\"msg\":\"%s\",\"data\":null}", code, msg);
                });
    }
}
