package com.tangtang.user.init;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tangtang.user.model.UserPermissionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 用户权限数据初始化器
 * 
 * 应用启动时将用户权限数据写入 Redis，供网关读取。
 * 
 * Redis Key 格式：perm:user:{userId}
 * 
 * 实际生产中，数据来源于数据库（User 表 + Role 表 + Permission 表）。
 * 这里用硬编码模拟数据库查询，演示完整流程。
 * 
 * 扩展方式：
 * - 新员工入职：调用 /users/admin/permission/refresh 接口刷新 Redis
 * - 权限变更：同上，或通过消息队列异步更新
 */
@Component
public class PermissionDataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(PermissionDataInitializer.class);

    /** Redis Key 前缀，与网关保持一致 */
    public static final String PERM_KEY_PREFIX = "perm:user:";

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info(">>> 开始初始化用户权限数据到 Redis...");

        // 模拟从数据库加载的用户权限数据
        // 实际项目中替换为：userRepository.findAll() + roleRepository...
        List<UserPermissionData> allUsers = loadFromDatabase();

        for (UserPermissionData user : allUsers) {
            String key = PERM_KEY_PREFIX + user.getUserId();
            String value = objectMapper.writeValueAsString(user);
            redisTemplate.opsForValue().set(key, value);
            log.info(">>> 已写入权限数据: userId={}, roles={}, permissions={}",
                    user.getUserId(), user.getRoles(), user.getPermissions());
        }

        log.info(">>> 用户权限数据初始化完成，共 {} 条", allUsers.size());
    }

    /**
     * 模拟从数据库加载用户权限数据
     * 
     * 实际项目替换为数据库查询：
     * SELECT u.id, u.username, r.name as role, p.code as permission
     * FROM users u
     * LEFT JOIN user_roles ur ON u.id = ur.user_id
     * LEFT JOIN roles r ON ur.role_id = r.id
     * LEFT JOIN role_permissions rp ON r.id = rp.role_id
     * LEFT JOIN permissions p ON rp.permission_id = p.id
     */
    private List<UserPermissionData> loadFromDatabase() {
        return Arrays.asList(
            new UserPermissionData(
                10001L, "admin",
                Arrays.asList("admin"),
                Arrays.asList(
                    "users:list", "users:add", "users:delete",
                    "orders:list", "orders:create", "orders:delete"
                )
            ),
            new UserPermissionData(
                10002L, "user",
                Arrays.asList("user"),
                Arrays.asList(
                    "users:list",
                    "orders:list"
                )
            )
        );
    }
}
