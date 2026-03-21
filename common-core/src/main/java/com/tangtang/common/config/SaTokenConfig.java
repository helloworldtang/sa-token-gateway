package com.tangtang.common.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 统一拦截器（可选配置）
 * 
 * 后端服务可选使用：
 * - 若使用：只验证登录态，不校验权限
 * - 若不使用：依赖网关统一鉴权（推荐）
 * 
 * 使用方式：在后端服务添加 @EnableSaTokenInterceptor 注解
 */
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    /**
     * 创建 Sa-Token 拦截器
     * 
     * 只验证登录态，不校验权限（权限由网关统一处理）
     */
    public static HandlerInterceptor createInterceptor() {
        return new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
                try {
                    StpUtil.checkLogin();
                    return true;
                } catch (Exception e) {
                    response.setContentType("application/json;charset=UTF-8");
                    response.setStatus(200);
                    response.getWriter().write("{\"code\":401,\"msg\":\"请先登录\",\"data\":null}");
                    response.getWriter().flush();
                    return false;
                }
            }
        };
    }

    /**
     * 注册拦截器（默认不启用）
     * 
     * 若需要后端服务也验证登录态，取消注释
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 默认不注册拦截器，依赖网关统一鉴权
        // 如需启用，取消下面注释：
        // registry.addInterceptor(createInterceptor())
        //     .addPathPatterns("/**")
        //     .excludePathPatterns("/auth/login")
        //     .excludePathPatterns("/swagger-ui/**")
        //     .excludePathPatterns("/v3/api-docs/**")
        //     .excludePathPatterns("/doc.html")
        //     .excludePathPatterns("/webjars/**")
        //     .excludePathPatterns("/favicon.ico");
    }
}
