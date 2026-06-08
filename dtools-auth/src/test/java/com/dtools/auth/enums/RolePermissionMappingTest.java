package com.dtools.auth.enums;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @description: 角色权限静态映射测试，验证阶段 1 权限码覆盖关系
 * @author: yesterday'jam
 * @date: 2026/06/08
 * @注意: 后续调整角色权限时必须同步更新该测试
 */
class RolePermissionMappingTest {

    /**
     * @description: 验证 owner 拥有阶段 1 全部权限和 all 数据范围
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: owner 是第一阶段最高权限角色
     */
    @Test
    void ownerShouldHaveAllStageOnePermissions() {
        List<String> permissions = RolePermissionMapping.permissionsOf(AuthRole.OWNER).stream()
                .map(AuthPermission::getCode)
                .toList();

        assertThat(permissions).containsExactlyInAnyOrder(
                "auth:user:read",
                "auth:user:write",
                "tool:def:read",
                "tool:def:write",
                "tool:execute",
                "history:read:self",
                "history:read:all",
                "history:delete:self",
                "settings:self:read",
                "settings:self:write",
                "settings:global:read",
                "settings:global:write",
                "audit:read"
        );
        assertThat(RolePermissionMapping.dataScopeOf(AuthRole.OWNER)).isEqualTo(DataScope.ALL);
    }

    /**
     * @description: 验证 viewer 只拥有只读权限且不能执行工具
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: viewer 不应包含 tool:execute
     */
    @Test
    void viewerShouldNotExecuteTools() {
        List<String> permissions = RolePermissionMapping.permissionsOf(AuthRole.VIEWER).stream()
                .map(AuthPermission::getCode)
                .toList();

        assertThat(permissions).contains("tool:def:read", "history:read:self", "settings:self:read");
        assertThat(permissions).doesNotContain("tool:execute");
        assertThat(RolePermissionMapping.dataScopeOf(AuthRole.VIEWER)).isEqualTo(DataScope.SELF);
    }
}
