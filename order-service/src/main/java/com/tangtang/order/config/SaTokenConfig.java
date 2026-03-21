package com.tangtang.order.config;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 配置
 * 
 * 后端服务：只验证登录态（StpUtil.checkLogin）
 * 权限校验由网关统一处理
 */
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    @Bean
    public HandlerInterceptor saInterceptor() {
        return new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
                try {
                    StpUtil.checkLogin();
                    return true;
                } catch (NotLoginException e) {
                    response.setContentType("application/json;charset=UTF-8");
                    response.setStatus(200);
                    response.getWriter().write("{\"code\":401,\"msg\":\"请先登录\",\"data\":null}");
                    response.getWriter().flush();
                    return false;
                }
            }
        };
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(saInterceptor())
        .addPathPatterns("/**")
        .excludePathPatterns("/swagger-ui/**")
        .excludePathPatterns("/v3/api-docs/**")
        .excludePathPatterns("/doc.html")
        .excludePathPatterns("/webjars/**")
        .excludePathPatterns("/favicon.ico");
    }
}
