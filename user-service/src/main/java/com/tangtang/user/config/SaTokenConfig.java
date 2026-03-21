package com.tangtang.user.config;

import com.tangtang.common.config.SaTokenInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 配置 - 方案2
 * 
 * 后端服务只验证登录态，不校验权限
 * 权限校验由网关统一处理
 */
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaTokenInterceptor())
        .addPathPatterns("/**")
        .excludePathPatterns("/auth/login")
        .excludePathPatterns("/swagger-ui/**")
        .excludePathPatterns("/v3/api-docs/**")
        .excludePathPatterns("/doc.html")
        .excludePathPatterns("/webjars/**")
        .excludePathPatterns("/favicon.ico");
    }
}
