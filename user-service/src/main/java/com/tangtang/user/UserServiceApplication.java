package com.tangtang.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * 用户服务启动类
 * 
 * 扫描 common-core 模块的组件
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.tangtang.user", "com.tangtang.common"})
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
        System.out.println("✅ 用户服务启动成功！端口: 8081");
    }
}
