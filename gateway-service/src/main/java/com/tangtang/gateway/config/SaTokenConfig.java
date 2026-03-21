package com.tangtang.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 网关 Sa-Token 配置
 * 
 * 注意：权限校验已由 AuthGatewayFilter 实现
 */
@Configuration
public class SaTokenConfig {
    // 权限校验由 AuthGatewayFilter 处理
}
