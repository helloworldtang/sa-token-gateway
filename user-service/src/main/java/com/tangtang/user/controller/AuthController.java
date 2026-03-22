package com.tangtang.user.controller;

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
 * 只负责登录/登出业务逻辑，不做任何鉴权
 * 鉴权由网关统一处理
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

        if ("admin".equals(username) && "123456".equals(password)) {
            StpUtil.login(10001);

            Map<String, Object> data = new HashMap<>();
            data.put("token", StpUtil.getTokenValue());
            data.put("userId", StpUtil.getLoginId());
            data.put("role", "admin");
            return SaResult.ok("登录成功").setData(data);
        }

        if ("user".equals(username) && "123456".equals(password)) {
            StpUtil.login(10002);

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
     * 
     * 注意：此接口通过网关访问时，网关已完成 Token 校验
     * userId 由网关通过 Header 传入
     */
    @Operation(summary = "获取用户信息", description = "获取当前登录用户信息")
    @GetMapping("/info")
    public SaResult info(
            @RequestHeader(value = "satoken-user-id", required = false) String userId) {
        if (userId == null) {
            return SaResult.error("未登录");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        return SaResult.ok().setData(data);
    }

    /**
     * 管理员接口
     * 
     * 注意：权限校验由网关统一处理，此处无需 @SaCheckRole
     */
    @Operation(summary = "管理员数据", description = "需要 admin 角色（由网关校验）")
    @GetMapping("/admin/data")
    public SaResult adminData() {
        return SaResult.ok("管理员专属数据");
    }
}
