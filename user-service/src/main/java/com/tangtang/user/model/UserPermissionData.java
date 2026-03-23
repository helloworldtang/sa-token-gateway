package com.tangtang.user.model;

import java.util.List;

/**
 * 用户权限数据模型
 * 
 * 存储在 Redis 中，key 格式：perm:user:{userId}
 * 网关从 Redis 读取此数据进行权限校验
 */
public class UserPermissionData {

    private Long userId;
    private String username;
    private List<String> roles;
    private List<String> permissions;

    public UserPermissionData() {}

    public UserPermissionData(Long userId, String username, List<String> roles, List<String> permissions) {
        this.userId = userId;
        this.username = username;
        this.roles = roles;
        this.permissions = permissions;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }

    public List<String> getPermissions() { return permissions; }
    public void setPermissions(List<String> permissions) { this.permissions = permissions; }
}
