package com.dtools.common.enums;

/**
 * @description: 统一响应码枚举，作为接口协议、日志审计和调用方处理的稳定 code 来源
 * @author: yesterday'jam
 * @date: 2026/06/08
 * @注意: 禁止在业务代码中绕过该枚举随意返回字符串 code
 */
public enum ResponseCode {

    /**
     * 请求处理成功。
     */
    SUCCESS(0, "成功"),

    /**
     * 请求参数不符合接口约束。
     */
    PARAM_ERROR(40000, "参数错误"),

    /**
     * 客户端请求不符合业务可见约束。
     */
    CLIENT_ERROR(40001, "客户端请求错误"),

    /**
     * 应用业务处理失败，详细内部原因不直接暴露给调用方。
     */
    APPLICATION_ERROR(40002, "业务处理异常"),

    /**
     * 请求未认证或认证信息无效。
     */
    UNAUTHORIZED(40100, "未认证"),

    /**
     * 请求已认证但无权访问当前资源。
     */
    FORBIDDEN(40300, "无访问权限"),

    /**
     * 系统内部异常。
     */
    SYSTEM_ERROR(50000, "系统异常");

    private final Integer code;

    private final String desc;

    ResponseCode(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * @description: 获取稳定数字响应码
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: 该 code 是前后端协议和日志审计的稳定值
     */
    public Integer getCode() {
        return code;
    }

    /**
     * @description: 获取中文响应说明
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: desc 只用于展示和排查，不作为协议判断条件
     */
    public String getDesc() {
        return desc;
    }

    /**
     * @description: 根据稳定 code 反查响应码枚举
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: 找不到时抛出明确异常，避免静默吞掉未知 code
     */
    public static ResponseCode fromCode(Integer code) {
        for (ResponseCode responseCode : values()) {
            if (responseCode.code.equals(code)) {
                return responseCode;
            }
        }
        throw new IllegalArgumentException("未知响应码: " + code);
    }
}
