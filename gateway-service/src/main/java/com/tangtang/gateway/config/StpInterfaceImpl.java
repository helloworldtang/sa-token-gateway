//package com.tangtang.gateway.config;
//
//import cn.dev33.satoken.stp.StpInterface;
//import org.springframework.stereotype.Component;
//
//import java.util.*;
//
///**
// * 网关权限数据接口实现
// *
// * 鉴权逻辑只写一次，Sa-Token 自动调用此接口查询权限
// */
////@Component
//public class StpInterfaceImpl implements StpInterface {
//
//    private static final Map<Long, List<String>> USER_PERMISSIONS = new HashMap<>();
//    private static final Map<Long, List<String>> USER_ROLES = new HashMap<>();
//
//    static {
//        // admin 用户 - 拥有所有权限
//        USER_PERMISSIONS.put(10001L, Arrays.asList(
//            "user:list", "user:add", "user:delete",
//            "order:list", "order:create", "order:delete"
//        ));
//        USER_ROLES.put(10001L, Arrays.asList("admin"));
//
//        // 普通用户 - 只有部分权限
//        USER_PERMISSIONS.put(10002L, Arrays.asList(
//            "user:list", "order:list"
//        ));
//        USER_ROLES.put(10002L, Arrays.asList("user"));
//    }
//
//    @Override
//    public List<String> getPermissionList(Object loginId, String loginType) {
//        Long userId = Long.parseLong(String.valueOf(loginId));
//        return USER_PERMISSIONS.getOrDefault(userId, Collections.emptyList());
//    }
//
//    @Override
//    public List<String> getRoleList(Object loginId, String loginType) {
//        Long userId = Long.parseLong(String.valueOf(loginId));
//        return USER_ROLES.getOrDefault(userId, Collections.emptyList());
//    }
//}
