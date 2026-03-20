package com.example.user.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户认证控制器
 * 
 * 注意：网关已做统一鉴权，这里可以省略登录校验
 * 但如果直接访问此服务，仍需自行校验
 * 
 * @author 码骨丹心
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    /**
     * 登录接口
     * 
     * 登录成功后，Token 会自动写入 Redis（分布式 Session）
     */
    @PostMapping("/login")
    public SaResult login(@RequestParam String username, 
                          @RequestParam String password) {
        // 模拟登录校验
        if ("admin".equals(username) && "123456".equals(password)) {
            // 🔥 登录并设置角色权限
            StpUtil.login(10001);
            
            // 设置角色（实际应从数据库查询）
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
    @PostMapping("/logout")
    public SaResult logout() {
        StpUtil.logout();
        return SaResult.ok("登出成功");
    }

    /**
     * 获取当前登录信息
     */
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
    @SaCheckRole("admin")
    @GetMapping("/admin/data")
    public SaResult adminData() {
        return SaResult.ok("管理员专属数据");
    }
}
