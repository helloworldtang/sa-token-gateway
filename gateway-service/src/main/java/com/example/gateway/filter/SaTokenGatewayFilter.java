package com.example.gateway.filter;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.reactor.filter.SaReactorFilter;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import com.alibaba.fastjson.JSON;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Sa-Token 网关过滤器配置
 * 
 * 在网关层统一处理登录鉴权，后端服务无需关心认证逻辑
 * 
 * @author 码骨丹心
 */
@Configuration
public class SaTokenGatewayFilter {

    /**
     * 注册 Sa-Token 全局过滤器
     */
    @Bean
    public SaReactorFilter getSaReactorFilter() {
        return new SaReactorFilter()
                // 指定拦截路由
                .addInclude("/**")
                // 指定放行路由（白名单）
                .addExclude("/favicon.ico", "/api/user/auth/login", "/api/user/auth/register")
                // 认证函数：每次请求都会执行
                .setAuth(obj -> {
                    System.out.println("---------- 进入网关鉴权 -----------");
                    
                    // 获取当前请求的 Token
                    String token = StpUtil.getTokenValue();
                    System.out.println("Token: " + token);
                    
                    // 登录校验
                    SaRouter.match("/**", r -> StpUtil.checkLogin());
                    
                    // 权限校验示例
                    SaRouter.match("/api/order/**", r -> StpUtil.checkPermission("order:*"));
                    SaRouter.match("/api/admin/**", r -> StpUtil.checkRole("admin"));
                })
                // 异常处理函数
                .setError(e -> {
                    System.out.println("---------- 鉴权异常 -----------");
                    
                    // 返回 JSON 格式的错误信息
                    SaResult result;
                    if (e instanceof NotLoginException) {
                        result = SaResult.error(401, "未登录，请先登录");
                    } else {
                        result = SaResult.error(403, "无权限访问: " + e.getMessage());
                    }
                    
                    return JSON.toJSONString(result);
                });
    }
}
