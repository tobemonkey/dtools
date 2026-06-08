package com.dtools.auth.security;

import com.dtools.auth.enums.AuthPermission;
import com.dtools.auth.enums.AuthRole;
import com.dtools.auth.enums.DataScope;
import com.dtools.auth.enums.RolePermissionMapping;
import com.dtools.auth.model.dto.CurrentUserDTO;
import com.dtools.auth.model.entity.AuthUserEntity;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * @description: 当前用户权限摘要组装器，负责从用户和角色计算权限码与数据范围
 * @author: yesterday'jam
 * @date: 2026/06/08
 * @注意: 多角色用户权限取并集，数据范围只要包含 all 即升级为 all
 */
public final class AuthUserAssembler {

    private AuthUserAssembler() {
    }

    /**
     * @description: 根据用户实体和角色 code 组装当前用户摘要
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: 返回 roles 和 permissions 均为稳定 code，不使用枚举名称
     */
    public static CurrentUserDTO assemble(AuthUserEntity user, List<String> roleCodes) {
        List<AuthRole> roles = roleCodes.stream()
                .map(AuthRole::fromCode)
                .toList();
        LinkedHashSet<String> permissions = new LinkedHashSet<>();
        DataScope dataScope = DataScope.SELF;
        for (AuthRole role : roles) {
            RolePermissionMapping.permissionsOf(role).stream()
                    .map(AuthPermission::getCode)
                    .forEach(permissions::add);
            if (RolePermissionMapping.dataScopeOf(role) == DataScope.ALL) {
                dataScope = DataScope.ALL;
            }
        }
        CurrentUserDTO currentUser = new CurrentUserDTO();
        currentUser.setId(user.getId());
        currentUser.setUsername(user.getUsername());
        currentUser.setDisplayName(user.getDisplayName());
        currentUser.setRoles(roles.stream()
                .map(AuthRole::getCode)
                .sorted()
                .toList());
        currentUser.setPermissions(permissions.stream()
                .sorted(Comparator.naturalOrder())
                .toList());
        currentUser.setDataScope(dataScope.getCode());
        return currentUser;
    }
}
