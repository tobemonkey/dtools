package com.dtools.auth.enums;

import com.dtools.common.exception.ApplicationException;

import java.util.Arrays;

/**
 * @description: 用户状态枚举，控制登录账号是否允许进入系统
 * @author: yesterday'jam
 * @date: 2026/06/08
 * @注意: 状态 code 用于数据库存储，禁止使用 enum.name() 作为协议值
 */
public enum UserStatus {

    /**
     * 正常启用状态。
     */
    ENABLED(1, "启用"),

    /**
     * 禁用状态，不允许登录。
     */
    DISABLED(0, "禁用");

    private final Integer code;

    private final String desc;

    UserStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * @description: 获取用户状态稳定数字 code
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: 该值用于数据库存储和审计日志
     */
    public Integer getCode() {
        return code;
    }

    /**
     * @description: 获取用户状态中文说明
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: desc 只用于展示和排查
     */
    public String getDesc() {
        return desc;
    }

    /**
     * @description: 根据稳定状态 code 反查用户状态枚举
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: 未知状态会抛出受控异常，避免误判为可登录
     */
    public static UserStatus fromCode(Integer code) {
        return Arrays.stream(values())
                .filter(status -> status.code.equals(code))
                .findFirst()
                .orElseThrow(() -> new ApplicationException("未知用户状态: " + code));
    }
}
