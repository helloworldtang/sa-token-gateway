package com.tangtang.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * 订单服务启动类
 * 
 * 扫描 common-core 模块的组件
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.tangtang.order", "com.tangtang.common"})
public class OrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
        System.out.println("✅ 订单服务启动成功！端口: 8082");
    }
}
