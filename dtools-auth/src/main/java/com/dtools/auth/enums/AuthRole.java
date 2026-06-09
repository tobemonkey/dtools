package com.dtools.auth.enums;

import com.dtools.common.exception.ApplicationException;

import java.util.Arrays;

/**
 * @description: 权限模块第一阶段内置角色枚举，作为用户角色协议值来源
 * @author: yesterday'jam
 * @date: 2026/06/08
 * @注意: 角色协议值使用 code 字段，禁止使用 enum.name() 参与存储或接口返回
 */
public enum AuthRole {

    /**
     * 系统所有者，拥有全部阶段 1 权限。
     */
    OWNER("owner", "所有者"),

    /**
     * 普通成员，可以执行工具并管理自己的数据。
     */
    MEMBER("member", "普通成员"),

    /**
     * 只读访问者，只能查看允许开放的只读数据。
     */
    VIEWER("viewer", "只读访问者");

    private final String code;

    private final String desc;

    AuthRole(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * @description: 获取角色稳定协议值
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: 该值用于数据库、接口返回和 JWT claims
     */
    public String getCode() {
        return code;
    }

    /**
     * @description: 获取角色中文说明
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: desc 只用于展示和排查，不作为权限判断依据
     */
    public String getDesc() {
        return desc;
    }

    /**
     * @description: 根据稳定角色 code 反查枚举
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: 未知角色会抛出受控异常，避免静默放行
     */
    public static AuthRole fromCode(String code) {
        return Arrays.stream(values())
                .filter(role -> role.code.equals(code))
                .findFirst()
                .orElseThrow(() -> new ApplicationException(AuthErrorReason.UNKNOWN_ROLE_CODE, code));
    }
}
