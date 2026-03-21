package com.tangtang.gateway.config;

import cn.dev33.satoken.stp.StpInterface;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 网关权限数据接口实现
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    private static final Map<Long, List<String>> USER_PERMISSIONS = new HashMap<>();
    private static final Map<Long, List<String>> USER_ROLES = new HashMap<>();

    static {
        USER_PERMISSIONS.put(10001L, Arrays.asList(
            "user:list", "user:add", "user:delete",
            "order:list", "order:create", "order:delete"
        ));
        USER_ROLES.put(10001L, Arrays.asList("admin"));

        USER_PERMISSIONS.put(10002L, Arrays.asList(
            "user:list", "order:list"
        ));
        USER_ROLES.put(10002L, Arrays.asList("user"));
    }

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        Long userId = Long.parseLong(String.valueOf(loginId));
        return USER_PERMISSIONS.getOrDefault(userId, Collections.emptyList());
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        Long userId = Long.parseLong(String.valueOf(loginId));
        return USER_ROLES.getOrDefault(userId, Collections.emptyList());
    }
}
