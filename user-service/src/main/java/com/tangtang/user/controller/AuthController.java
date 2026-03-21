package com.tangtang.user.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户认证控制器
 * 
 * @author 码骨丹心
 */
@Tag(name = "用户认证", description = "登录、登出、用户信息")
@RestController
@RequestMapping("/auth")
public class AuthController {

    /**
     * 登录接口
     */
    @Operation(summary = "用户登录", description = "用户名密码登录，返回 token")
    @PostMapping("/login")
    public SaResult login(
            @Parameter(description = "用户名 (admin 或 user)", required = true, example = "admin")
            @RequestParam String username,
            @Parameter(description = "密码 (123456)", required = true, example = "123456")
            @RequestParam String password) {
        // 模拟登录校验
        if ("admin".equals(username) && "123456".equals(password)) {
            // 🔥 登录并设置角色权限
            StpUtil.login(10001);
            StpUtil.getSession().set("role", "admin");
            StpUtil.getSession().set("permissions", "user:*,order:*,admin:*");
            
            Map<String, Object> data = new HashMap<>();
            data.put("token", StpUtil.getTokenValue());
            data.put("userId", StpUtil.getLoginId());
            data.put("role", "admin");
            
            return SaResult.ok("登录成功").setData(data);
        }
        
        if ("user".equals(username) && "123456".equals(password)) {
            StpUtil.login(10002);
            StpUtil.getSession().set("role", "user");
            StpUtil.getSession().set("permissions", "user:list,order:list");
            
            Map<String, Object> data = new HashMap<>();
            data.put("token", StpUtil.getTokenValue());
            data.put("userId", StpUtil.getLoginId());
            data.put("role", "user");
            
            return SaResult.ok("登录成功").setData(data);
        }
        
        return SaResult.error("用户名或密码错误");
    }

    /**
     * 登出
     */
    @Operation(summary = "用户登出", description = "退出登录状态")
    @PostMapping("/logout")
    public SaResult logout() {
        StpUtil.logout();
        return SaResult.ok("登出成功");
    }

    /**
     * 获取当前登录信息
     */
    @Operation(summary = "获取用户信息", description = "获取当前登录用户信息")
    @SaCheckLogin
    @GetMapping("/info")
    public SaResult info() {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", StpUtil.getLoginId());
        data.put("token", StpUtil.getTokenValue());
        data.put("role", StpUtil.getSession().get("role"));
        data.put("permissions", StpUtil.getSession().get("permissions"));
        
        return SaResult.ok().setData(data);
    }

    /**
     * 管理员接口
     */
    @Operation(summary = "管理员数据", description = "需要 admin 角色")
    @SaCheckRole("admin")
    @GetMapping("/admin/data")
    public SaResult adminData() {
        return SaResult.ok("管理员专属数据");
    }
}
