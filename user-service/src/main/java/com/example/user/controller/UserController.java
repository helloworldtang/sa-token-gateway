package com.example.user.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * 用户管理控制器
 * 
 * @author 码骨丹心
 */
@RestController
@RequestMapping("/user")
public class UserController {

    /**
     * 获取用户列表
     */
    @SaCheckPermission("user:list")
    @GetMapping("/list")
    public SaResult list() {
        List<String> users = Arrays.asList("张三", "李四", "王五");
        return SaResult.ok().setData(users);
    }

    /**
     * 获取当前用户信息
     */
    @SaCheckLogin
    @GetMapping("/profile")
    public SaResult profile() {
        return SaResult.ok("当前用户: " + StpUtil.getLoginId());
    }
}
