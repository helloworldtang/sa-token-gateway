package com.tangtang.order.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 订单控制器
 * 
 * 只处理业务逻辑，不做任何鉴权
 * userId 由网关通过 Header 传入
 */
@Tag(name = "订单管理", description = "订单列表、创建订单、获取我的订单")
@RestController
@RequestMapping("/order")
public class OrderController {

    @Operation(summary = "订单列表", description = "获取订单列表")
    @GetMapping("/list")
    public Map<String, Object> list() {
        List<Map<String, Object>> orders = Arrays.asList(
            createOrder(1L, "iPhone 15", 5999.00),
            createOrder(2L, "MacBook Pro", 14999.00),
            createOrder(3L, "AirPods Pro", 1899.00)
        );
        return result(200, "订单列表", orders);
    }

    @Operation(summary = "创建订单", description = "创建新订单")
    @PostMapping("/create")
    public Map<String, Object> create(
            @Parameter(description = "商品名称", required = true, example = "iPhone 15")
            @RequestParam String productName,
            @Parameter(description = "价格", required = true, example = "5999")
            @RequestParam Double price,
            @RequestHeader(value = "satoken-user-id", required = false) String userId) {

        Map<String, Object> order = createOrder(System.currentTimeMillis(), productName, price);
        order.put("userId", userId);
        return result(200, "订单创建成功", order);
    }

    @Operation(summary = "我的订单", description = "获取当前用户的订单")
    @GetMapping("/my")
    public Map<String, Object> myOrders(
            @RequestHeader(value = "satoken-user-id", required = false) String userId) {

        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("message", "分布式 Session 验证成功！");
        return result(200, "ok", data);
    }

    private Map<String, Object> result(int code, String msg, Object data) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", code);
        result.put("msg", msg);
        result.put("data", data);
        return result;
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
