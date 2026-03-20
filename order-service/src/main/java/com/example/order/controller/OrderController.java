package com.example.order.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单控制器
 * 
 * 演示分布式 Session：
 * 在网关登录后，订单服务可以获取到相同的登录态
 * 
 * @author 码骨丹心
 */
@RestController
@RequestMapping("/order")
public class OrderController {

    /**
     * 获取订单列表
     * 
     * 网关已鉴权，这里直接处理业务
     */
    @SaCheckPermission("order:list")
    @GetMapping("/list")
    public SaResult list() {
        List<Map<String, Object>> orders = Arrays.asList(
            createOrder(1L, "iPhone 15", 5999.00),
            createOrder(2L, "MacBook Pro", 14999.00),
            createOrder(3L, "AirPods Pro", 1899.00)
        );
        
        return SaResult.ok("订单列表").setData(orders);
    }

    /**
     * 创建订单
     */
    @SaCheckPermission("order:create")
    @PostMapping("/create")
    public SaResult create(@RequestParam String productName,
                           @RequestParam Double price) {
        Map<String, Object> order = createOrder(System.currentTimeMillis(), 
                                                productName, price);
        order.put("userId", StpUtil.getLoginId());
        
        return SaResult.ok("订单创建成功").setData(order);
    }

    /**
     * 获取当前用户订单（演示分布式 Session）
     */
    @SaCheckLogin
    @GetMapping("/my")
    public SaResult myOrders() {
        // 通过分布式 Session 获取当前登录用户
        Object userId = StpUtil.getLoginId();
        Object role = StpUtil.getSession().get("role");
        
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("role", role);
        data.put("message", "分布式 Session 验证成功！");
        
        return SaResult.ok().setData(data);
    }

    private Map<String, Object> createOrder(Long id, String name, Double price) {
        Map<String, Object> order = new HashMap<>();
        order.put("id", id);
        order.put("productName", name);
        order.put("price", price);
        order.put("status", "PAID");
        return order;
    }
}
