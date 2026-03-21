package com.tangtang.common.config;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Sa-Token 统一拦截器
 * 
 * 后端服务只验证登录态，权限校验由网关统一处理
 */
public class SaTokenInterceptor implements HandlerInterceptor {

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
}
