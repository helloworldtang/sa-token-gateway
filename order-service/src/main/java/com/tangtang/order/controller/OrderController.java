package com.tangtang.order.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单控制器
 * 
 * 注意：权限校验已由网关统一处理（方案2），
 * 后端服务只验证登录态，不校验权限
 * 
 * @author 码骨丹心
 */
@Tag(name = "订单管理", description = "订单列表、创建订单、获取我的订单")
@RestController
@RequestMapping("/order")
public class OrderController {

    /**
     * 获取订单列表
     * 权限已在网关校验，后端直接返回数据
     */
    @Operation(summary = "订单列表", description = "需要登录")
    @SaCheckLogin
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
     * 权限已在网关校验，后端直接处理业务
     */
    @Operation(summary = "创建订单", description = "需要登录")
    @SaCheckLogin
    @PostMapping("/create")
    public SaResult create(
            @Parameter(description = "商品名称", required = true, example = "iPhone 15")
            @RequestParam String productName,
            @Parameter(description = "价格", required = true, example = "5999")
            @RequestParam Double price) {
        Map<String, Object> order = createOrder(System.currentTimeMillis(), 
                                                productName, price);
        order.put("userId", StpUtil.getLoginId());
        
        return SaResult.ok("订单创建成功").setData(order);
    }

    /**
     * 获取当前用户订单
     */
    @Operation(summary = "我的订单", description = "获取当前用户的订单，需要登录")
    @SaCheckLogin
    @GetMapping("/my")
    public SaResult myOrders() {
        Object userId = StpUtil.getLoginId();
        
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
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
