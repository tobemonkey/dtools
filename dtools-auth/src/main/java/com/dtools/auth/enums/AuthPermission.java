package com.dtools.auth.enums;

import com.dtools.common.exception.ApplicationException;

import java.util.Arrays;

/**
 * @description: 权限模块第一阶段稳定权限码枚举，作为接口和前端体验层的授权协议
 * @author: yesterday'jam
 * @date: 2026/06/08
 * @注意: 权限判断必须使用 code 字段，禁止使用 enum.name() 作为协议值
 */
public enum AuthPermission {

    AUTH_USER_READ("auth:user:read", "查看用户"),
    AUTH_USER_WRITE("auth:user:write", "新增、禁用或修改用户"),
    TOOL_DEF_READ("tool:def:read", "查看工具定义"),
    TOOL_DEF_WRITE("tool:def:write", "新增或修改工具定义"),
    TOOL_EXECUTE("tool:execute", "执行工具"),
    HISTORY_READ_SELF("history:read:self", "查看自己的执行历史"),
    HISTORY_READ_ALL("history:read:all", "查看全部执行历史"),
    HISTORY_DELETE_SELF("history:delete:self", "删除自己的执行历史"),
    SETTINGS_SELF_READ("settings:self:read", "查看个人设置"),
    SETTINGS_SELF_WRITE("settings:self:write", "修改个人设置"),
    SETTINGS_GLOBAL_READ("settings:global:read", "查看全局设置"),
    SETTINGS_GLOBAL_WRITE("settings:global:write", "修改全局设置"),
    AUDIT_READ("audit:read", "查看登录、权限和关键操作审计");

    private final String code;

    private final String desc;

    AuthPermission(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * @description: 获取稳定权限码
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: 该值用于 JWT claims、接口协议和权限注解
     */
    public String getCode() {
        return code;
    }

    /**
     * @description: 获取权限中文说明
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: desc 不参与程序判断
     */
    public String getDesc() {
        return desc;
    }

    /**
     * @description: 根据稳定权限码反查权限枚举
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: 未知权限码会抛出受控异常，避免错误权限被静默忽略
     */
    public static AuthPermission fromCode(String code) {
        return Arrays.stream(values())
                .filter(permission -> permission.code.equals(code))
                .findFirst()
                .orElseThrow(() -> new ApplicationException("未知权限码: " + code));
    }
}
