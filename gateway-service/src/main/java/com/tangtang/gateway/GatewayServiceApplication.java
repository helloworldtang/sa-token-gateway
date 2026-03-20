package com.tangtang.gateway;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import cn.dev33.satoken.reactor.filter.SaReactorFilter;
import cn.dev33.satoken.filter.SaFilterAuthStrategy;
import cn.dev33.satoken.filter.SaFilterErrorStrategy;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.server.ServerWebExchange;

/**
 * 网关服务启动类
 * 
 * 统一鉴权入口，所有请求都经过网关进行身份验证
 * 
 * @author 码骨丹心
 */
@SpringBootApplication
public class GatewayServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayServiceApplication.class, args);
        System.out.println("✅ 网关服务启动成功！");
        System.out.println("📖 网关地址: http://localhost:8080");
        System.out.println("🔐 登录接口: POST http://localhost:8080/api/user/auth/login");
        System.out.println("   测试用户: admin / 123456");
    }

    /**
     * 注册 Sa-Token 网关过滤器
     */
    @Bean
    public SaReactorFilter getSaReactorFilter() {
        // 认证策略：所有请求都需要登录
        SaFilterAuthStrategy auth = obj -> {
            // 转换为 ServerWebExchange
            ServerWebExchange exchange = (ServerWebExchange) obj;
            // 获取 Token
            String token = exchange.getRequest().getHeaders().getFirst("satoken");
            if (token == null || token.isEmpty()) {
                throw new NotLoginException("Token 为空", null, null);
            }
        };

        // 异常处理策略
        SaFilterErrorStrategy error = e -> {
            int code = 403;
            String msg = "无权限访问";
            if (e instanceof NotLoginException) {
                code = 401;
                msg = "未登录，请先登录";
            } else if (e instanceof NotRoleException) {
                msg = "缺少角色: " + e.getMessage();
            } else if (e instanceof NotPermissionException) {
                msg = "缺少权限: " + e.getMessage();
            }
            return String.format("{\"code\":%d,\"msg\":\"%s\",\"data\":null}", code, msg);
        };

        return new SaReactorFilter()
                // 放行白名单
                .addExclude("/favicon.ico")
                .addExclude("/api/user/auth/login")
                .addExclude("/api/user/auth/register")
                // 认证函数
                .setAuth(auth)
                // 异常处理
                .setError(error);
    }
}
