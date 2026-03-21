package com.tangtang.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 网关服务启动类
 * 
 * 统一鉴权入口，所有请求都经过网关进行身份验证
 * 
 * @author 码骨丹心
 */
@SpringBootApplication
public class GatewayServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayServiceApplication.class, args);
        System.out.println("✅ 网关服务启动成功！");
        System.out.println("📖 网关地址: http://localhost:8080");
        System.out.println("🔐 登录接口: POST http://localhost:8081/auth/login");
        System.out.println("   测试用户: admin / 123456");
    }
}
