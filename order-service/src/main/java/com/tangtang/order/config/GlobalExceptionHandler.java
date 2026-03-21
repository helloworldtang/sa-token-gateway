package com.tangtang.order.config;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import cn.dev33.satoken.util.SaResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * 统一返回格式
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理未登录异常
     */
    @ExceptionHandler(NotLoginException.class)
    public SaResult handleNotLoginException(NotLoginException e) {
        return SaResult.code(401).setMsg("请先登录");
    }

    /**
     * 处理缺少角色异常
     */
    @ExceptionHandler(NotRoleException.class)
    public SaResult handleNotRoleException(NotRoleException e) {
        return SaResult.code(403).setMsg("缺少角色: " + e.getRole());
    }

    /**
     * 处理缺少权限异常
     */
    @ExceptionHandler(NotPermissionException.class)
    public SaResult handleNotPermissionException(NotPermissionException e) {
        return SaResult.code(403).setMsg("缺少权限: " + e.getPermission());
    }

    /**
     * 处理其他异常
     */
    @ExceptionHandler(Exception.class)
    public SaResult handleException(Exception e) {
        return SaResult.code(500).setMsg(e.getMessage());
    }
}
