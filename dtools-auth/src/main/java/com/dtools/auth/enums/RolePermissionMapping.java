package com.dtools.auth.enums;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * @description: 第一阶段内置角色到权限与数据范围的静态映射
 * @author: yesterday'jam
 * @date: 2026/06/08
 * @注意: 后续动态角色管理落地前，权限集合以该映射为准
 */
public final class RolePermissionMapping {

    private static final Map<AuthRole, List<AuthPermission>> ROLE_PERMISSIONS = new EnumMap<>(AuthRole.class);

    private static final Map<AuthRole, DataScope> ROLE_DATA_SCOPE = new EnumMap<>(AuthRole.class);

    static {
        ROLE_PERMISSIONS.put(AuthRole.OWNER, List.of(
                AuthPermission.AUTH_USER_READ,
                AuthPermission.AUTH_USER_WRITE,
                AuthPermission.TOOL_DEF_READ,
                AuthPermission.TOOL_DEF_WRITE,
                AuthPermission.TOOL_EXECUTE,
                AuthPermission.HISTORY_READ_SELF,
                AuthPermission.HISTORY_READ_ALL,
                AuthPermission.HISTORY_DELETE_SELF,
                AuthPermission.SETTINGS_SELF_READ,
                AuthPermission.SETTINGS_SELF_WRITE,
                AuthPermission.SETTINGS_GLOBAL_READ,
                AuthPermission.SETTINGS_GLOBAL_WRITE,
                AuthPermission.AUDIT_READ
        ));
        ROLE_PERMISSIONS.put(AuthRole.MEMBER, List.of(
                AuthPermission.TOOL_DEF_READ,
                AuthPermission.TOOL_EXECUTE,
                AuthPermission.HISTORY_READ_SELF,
                AuthPermission.HISTORY_DELETE_SELF,
                AuthPermission.SETTINGS_SELF_READ,
                AuthPermission.SETTINGS_SELF_WRITE
        ));
        ROLE_PERMISSIONS.put(AuthRole.VIEWER, List.of(
                AuthPermission.TOOL_DEF_READ,
                AuthPermission.HISTORY_READ_SELF,
                AuthPermission.SETTINGS_SELF_READ
        ));
        ROLE_DATA_SCOPE.put(AuthRole.OWNER, DataScope.ALL);
        ROLE_DATA_SCOPE.put(AuthRole.MEMBER, DataScope.SELF);
        ROLE_DATA_SCOPE.put(AuthRole.VIEWER, DataScope.SELF);
    }

    private RolePermissionMapping() {
    }

    /**
     * @description: 获取角色拥有的权限集合
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: 返回集合不可变，调用方不得修改静态授权关系
     */
    public static List<AuthPermission> permissionsOf(AuthRole role) {
        return ROLE_PERMISSIONS.getOrDefault(role, List.of());
    }

    /**
     * @description: 获取角色默认数据范围
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: 多角色用户按 AuthUserAssembler 中的规则合并数据范围
     */
    public static DataScope dataScopeOf(AuthRole role) {
        return ROLE_DATA_SCOPE.getOrDefault(role, DataScope.SELF);
    }
}
