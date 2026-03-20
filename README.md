# Sa-Token 网关统一鉴权 + 分布式 Session 实战

> 微服务鉴权不用愁！Sa-Token 网关统一鉴权 + 分布式 Session 实战

[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2023.0.0-brightgreen)](https://spring.io/projects/spring-cloud-gateway)
[![Sa-Token](https://img.shields.io/badge/Sa--Token-1.37.0-orange)](https://sa-token.cc)
[![JDK](https://img.shields.io/badge/JDK-17+-blue)](https://www.oracle.com/java/)

## 项目简介

本项目是《微服务鉴权不用愁！Sa-Token 网关统一鉴权 + 分布式 Session 实战》的配套代码，演示如何在微服务架构中使用 **Sa-Token** 实现：

- ✅ **网关统一鉴权**：所有请求经过网关验证，后端服务无感知
- ✅ **分布式 Session**：登录态存储在 Redis，多服务共享
- ✅ **权限控制**：基于注解的细粒度权限管理

## 架构图

```
┌─────────────┐
│   客户端     │
└──────┬──────┘
       │
       ▼
┌─────────────────────────┐
│      Gateway (8080)      │  ← 统一鉴权入口
│   - Sa-Token 过滤器      │
│   - 登录校验              │
│   - 权限校验              │
└──────┬──────────────────┘
       │
   ┌───┴───┐
   ▼       ▼
┌──────┐ ┌──────┐
│User  │ │Order │
│(8081)│ │(8082)│
└──────┘ └──────┘
   │       │
   └───┬───┘
       ▼
┌─────────────┐
│    Redis    │  ← 分布式 Session
└─────────────┘
```

## 项目结构

```
sa-token-gateway/
├── gateway-service/          # 网关服务（统一鉴权）
│   ├── src/main/java/com/example/gateway/
│   │   ├── GatewayServiceApplication.java
│   │   ├── config/
│   │   │   └── GatewayRouteConfig.java    # 路由配置
│   │   └── filter/
│   │       └── SaTokenGatewayFilter.java  # 鉴权过滤器
│   └── pom.xml
├── user-service/             # 用户服务
│   ├── src/main/java/com/example/user/
│   │   ├── UserServiceApplication.java
│   │   └── controller/
│   │       ├── AuthController.java        # 登录接口
│   │       └── UserController.java        # 用户接口
│   └── pom.xml
├── order-service/            # 订单服务
│   ├── src/main/java/com/example/order/
│   │   ├── OrderServiceApplication.java
│   │   └── controller/
│   │       └── OrderController.java       # 订单接口
│   └── pom.xml
├── pom.xml                   # 父 POM
└── README.md
```

## 快速开始

### 1. 环境准备

- JDK 17+
- Maven 3.8+
- Redis 6.0+（用于分布式 Session）

### 2. 启动 Redis

```bash
redis-server
```

### 3. 启动服务

```bash
# 1. 启动网关服务
cd gateway-service
mvn spring-boot:run

# 2. 启动用户服务（新终端）
cd user-service
mvn spring-boot:run

# 3. 启动订单服务（新终端）
cd order-service
mvn spring-boot:run
```

### 4. 测试接口

#### 4.1 登录（通过网关）

```bash
curl -X POST "http://localhost:8080/api/user/auth/login" \
  -d "username=admin" \
  -d "password=123456"
```

返回：
```json
{
  "code": 200,
  "msg": "登录成功",
  "data": {
    "token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...",
    "userId": 10001,
    "role": "admin"
  }
}
```

#### 4.2 访问订单服务（分布式 Session 验证）

```bash
curl "http://localhost:8080/api/order/my" \
  -H "satoken: eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9..."
```

返回：
```json
{
  "code": 200,
  "msg": "ok",
  "data": {
    "userId": 10001,
    "role": "admin",
    "message": "分布式 Session 验证成功！"
  }
}
```

#### 4.3 未登录访问（被拦截）

```bash
curl "http://localhost:8080/api/order/list"
```

返回：
```json
{
  "code": 401,
  "msg": "未登录，请先登录"
}
```

## 核心代码

### 网关统一鉴权

```java
@Bean
public SaReactorFilter getSaReactorFilter() {
    return new SaReactorFilter()
        // 放行白名单
        .addExclude("/api/user/auth/login")
        // 认证函数
        .setAuth(obj -> {
            // 登录校验
            SaRouter.match("/**", r -> StpUtil.checkLogin());
            // 权限校验
            SaRouter.match("/api/order/**", r -> StpUtil.checkPermission("order:*"));
        })
        // 异常处理
        .setError(e -> {
            return JSON.toJSONString(SaResult.error(401, "未登录"));
        });
}
```

### 分布式 Session

登录态自动存储在 Redis，所有服务共享：

```java
// 用户服务登录
StpUtil.login(10001);
StpUtil.getSession().set("role", "admin");

// 订单服务获取（同一 Token）
Object role = StpUtil.getSession().get("role");  // "admin"
```

## 技术栈

- Spring Cloud Gateway 2023.0.0
- Spring Boot 3.2.0
- Sa-Token 1.37.0
- Redis 6.0+

## 学习资源

- [Sa-Token 官方文档](https://sa-token.cc)
- [Spring Cloud Gateway 文档](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/)

## 许可证

MIT License

---

**如果本项目对你有帮助，欢迎 Star ⭐**
