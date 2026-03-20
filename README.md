# Sa-Token 网关统一鉴权 + 分布式 Session 实战

> 微服务鉴权不用愁！Sa-Token 网关统一鉴权 + 分布式 Session 实战

[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2023.0.0-brightgreen)](https://spring.io/projects/spring-cloud-gateway)
[![Sa-Token](https://img.shields.io/badge/Sa--Token-1.37.0-orange)](https://sa-token.cc)
[![JDK](https://img.shields.io/badge/JDK-17+-blue)](https://www.oracle.com/java/)
[![Redis](https://img.shields.io/badge/Redis-6.0+-red)](https://redis.io)

## 项目简介

本项目是《微服务鉴权不用愁！Sa-Token 网关统一鉴权 + 分布式 Session 实战》的配套代码，演示如何在微服务架构中使用 **Sa-Token** 实现：

- ✅ **网关统一鉴权**：所有请求经过网关验证，后端服务无感知
- ✅ **分布式 Session**：登录态存储在 Redis，多服务共享
- ✅ **权限精细化控制**：基于角色的权限管理

## 架构图

```
┌─────────────┐
│   客户端     │
└──────┬──────┘
       │
       ▼
┌─────────────────────────┐
│   Gateway (8080)         │
│  ┌───────────────────┐  │
│  │ Sa-Token 过滤器    │  │  ← 统一鉴权入口
│  │ Token 校验         │  │
│  └───────────────────┘  │
└──────┬──────────────────┘
       │
   ┌───┴───┐
   ▼       ▼
┌──────┐ ┌──────┐
│User  │ │Order │
│Service│ │Service│
│(8081)│ │(8082)│
└──┬───┘ └──┬───┘
   │       │
   └───────┴───────────┐
         ▼             ▼
   ┌─────────────────────────┐
   │      Redis              │  ← 分布式 Session
   │  Token / Session 存储   │
   └─────────────────────────┘
```

## 项目结构

```
sa-token-gateway/
├── pom.xml                          # Maven 父 POM
├── gateway-service/                  # 网关服务（统一鉴权）
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/tangtang/gateway/
│       │   ├── GatewayServiceApplication.java  # 启动类
│       │   └── config/
│       │       └── GatewayRouteConfig.java     # 路由配置
│       └── resources/
│           └── application.yml
├── user-service/                    # 用户服务
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/tangtang/user/
│       │   ├── UserServiceApplication.java
│       │   ├── config/
│       │   │   └── StpInterfaceImpl.java       # 权限数据接口
│       │   └── controller/
│       │       └── AuthController.java         # 认证接口
│       └── resources/
│           └── application.yml
└── order-service/                  # 订单服务
    ├── pom.xml
    └── src/main/
        ├── java/com/tangtang/order/
        │   ├── OrderServiceApplication.java
        │   └── controller/
        │       └── OrderController.java
        └── resources/
            └── application.yml
```

## 快速开始

### 1. 环境要求

- JDK 17+
- Redis 6.0+
- Maven 3.6+

### 2. 启动 Redis

```bash
redis-server --daemonize yes
```

### 3. 启动服务

```bash
# 编译项目
mvn clean compile -DskipTests

# 启动用户服务
cd user-service && mvn spring-boot:run &
sleep 8

# 启动订单服务
cd ../order-service && mvn spring-boot:run &
sleep 8

# 启动网关服务
cd ../gateway-service && mvn spring-boot:run &
sleep 8
```

### 4. 测试接口

#### 登录

```bash
curl -X POST "http://localhost:8081/auth/login?username=admin&password=123456"
```

返回：
```json
{
  "code": 200,
  "msg": "登录成功",
  "data": {
    "token": "xxx-xxx-xxx",
    "userId": "10001",
    "role": "admin"
  }
}
```

#### 访问订单服务（分布式 Session）

```bash
# 使用登录返回的 token
curl "http://localhost:8082/order/my" -H "satoken: 你的token"
```

返回：
```json
{
  "code": 200,
  "msg": "ok",
  "data": {
    "userId": "10001",
    "role": "admin",
    "message": "分布式 Session 验证成功！"
  }
}
```

## 模拟用户数据

| 用户名 | 密码 | 用户ID | 角色 | 权限 |
|--------|------|--------|------|------|
| admin | 123456 | 10001 | admin | user:*, order:*, admin:* |
| user | 123456 | 10002 | user | user:list, order:list |

## 核心代码

### 网关统一鉴权

```java
@Bean
public SaReactorFilter getSaReactorFilter() {
    // 认证策略
    SaFilterAuthStrategy auth = exchange -> {
        String token = exchange.getRequest().getHeaders().getFirst("satoken");
        if (token == null || token.isEmpty()) {
            throw new NotLoginException("Token 为空", null, null);
        }
    };

    // 异常处理
    SaFilterErrorStrategy error = e -> {
        // 返回统一 JSON 格式
        return "{\"code\":401,\"msg\":\"请先登录\",\"data\":null}";
    };

    return new SaReactorFilter()
            .addExclude("/api/user/auth/login")  // 放行登录接口
            .setAuth(auth)
            .setError(error);
}
```

### 分布式 Session 配置

```yaml
# application.yml
spring:
  redis:
    host: localhost
    port: 6379

sa-token:
  token-name: satoken
  timeout: 2592000
```

Sa-Token 会自动将 Token 和 Session 存储到 Redis，实现多服务共享。

## 技术栈

- Spring Cloud Gateway 2023.0.0
- Sa-Token 1.37.0
- Sa-Token Reactor（网关集成）
- Sa-Token Redis（分布式 Session）
- Redis 6.0+
- JDK 17+

## 特性

- 🚀 **低代码接入**：网关配置简单，无需手写拦截器
- 🔐 **统一鉴权**：所有请求在网关层统一验证
- 📦 **分布式 Session**：基于 Redis，多服务共享登录态
- 🎯 **权限精细化**：支持角色和权限双重校验
- ⚡ **高性能**：基于 WebFlux 非阻塞设计

## License

MIT License

---

**如果本项目对你有帮助，欢迎 Star ⭐**
