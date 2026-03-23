package com.tangtang.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tangtang.user.init.PermissionDataInitializer;
import com.tangtang.user.model.UserPermissionData;
import cn.dev33.satoken.util.SaResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * 权限管理接口（管理员使用）
 * 
 * 用于新员工入职、权限变更时刷新 Redis 中的权限数据
 * 网关会实时读取最新数据，无需重启任何服务
 */
@Tag(name = "权限管理", description = "管理员权限数据维护（需要 admin 角色，由网关校验）")
@RestController
@RequestMapping("/auth/admin/permission")
public class PermissionController {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 刷新指定用户的权限数据
     * 
     * 使用场景：
     * - 新员工入职
     * - 员工角色变更
     * - 权限调整
     */
    @Operation(summary = "刷新用户权限", description = "将用户权限数据写入 Redis，网关实时生效")
    @PostMapping("/refresh")
    public SaResult refreshPermission(@RequestBody UserPermissionData permissionData) {
        try {
            String key = PermissionDataInitializer.PERM_KEY_PREFIX + permissionData.getUserId();
            String value = objectMapper.writeValueAsString(permissionData);
            redisTemplate.opsForValue().set(key, value);
            return SaResult.ok("权限数据已更新，网关实时生效")
                    .setData("key: " + key);
        } catch (Exception e) {
            return SaResult.error("更新失败: " + e.getMessage());
        }
    }

    /**
     * 查询指定用户的权限数据
     */
    @Operation(summary = "查询用户权限", description = "从 Redis 查询用户当前权限数据")
    @GetMapping("/{userId}")
    public SaResult getPermission(@PathVariable Long userId) {
        try {
            String key = PermissionDataInitializer.PERM_KEY_PREFIX + userId;
            String value = redisTemplate.opsForValue().get(key);
            if (value == null) {
                return SaResult.error("用户权限数据不存在");
            }
            UserPermissionData data = objectMapper.readValue(value, UserPermissionData.class);
            return SaResult.ok().setData(data);
        } catch (Exception e) {
            return SaResult.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 删除用户权限数据（离职/禁用）
     */
    @Operation(summary = "删除用户权限", description = "从 Redis 删除用户权限数据，网关实时拒绝该用户访问")
    @DeleteMapping("/{userId}")
    public SaResult deletePermission(@PathVariable Long userId) {
        String key = PermissionDataInitializer.PERM_KEY_PREFIX + userId;
        redisTemplate.delete(key);
        return SaResult.ok("权限数据已删除，网关实时生效");
    }
}
