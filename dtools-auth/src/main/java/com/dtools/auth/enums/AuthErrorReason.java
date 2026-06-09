package com.dtools.auth.enums;

import com.dtools.common.enums.ErrorReason;
import com.dtools.common.enums.ResponseCode;

/**
 * @description: 认证模块异常原因枚举，统一维护登录、令牌和权限基础数据错误文案
 * @author: yesterday'jam
 * @date: 2026/06/09
 * @注意: 认证模块新增可预期失败时必须先补充该枚举，再由异常类抛出
 */
public enum AuthErrorReason implements ErrorReason {

    /**
     * 用户名不存在或密码不匹配。
     */
    LOGIN_FAILED(ResponseCode.UNAUTHORIZED, "用户名或密码错误"),

    /**
     * 账号处于禁用状态。
     */
    ACCOUNT_DISABLED(ResponseCode.UNAUTHORIZED, "账号已禁用"),

    /**
     * 刷新凭证不存在、过期、已撤销或并发消费失败。
     */
    REFRESH_TOKEN_INVALID_OR_EXPIRED(ResponseCode.UNAUTHORIZED, "刷新凭证无效或已过期"),

    /**
     * JWT 对应用户不存在或已禁用。
     */
    CURRENT_USER_INVALID(ResponseCode.UNAUTHORIZED, "认证用户不存在或已禁用"),

    /**
     * 登录账号未分配任何角色。
     */
    ROLE_NOT_ASSIGNED(ResponseCode.UNAUTHORIZED, "账号未分配角色"),

    /**
     * 当前运行环境缺少 SHA-256 算法支持。
     */
    SHA256_UNSUPPORTED(ResponseCode.APPLICATION_ERROR, "当前运行环境不支持 SHA-256"),

    /**
     * JWT 访问令牌签发失败。
     */
    ACCESS_TOKEN_ISSUE_FAILED(ResponseCode.APPLICATION_ERROR, "签发访问令牌失败"),

    /**
     * 数据库或协议中出现未知用户状态 code。
     */
    UNKNOWN_USER_STATUS(ResponseCode.APPLICATION_ERROR, "未知用户状态: %s"),

    /**
     * 数据库或协议中出现未知数据范围 code。
     */
    UNKNOWN_DATA_SCOPE(ResponseCode.APPLICATION_ERROR, "未知数据范围: %s"),

    /**
     * 数据库或协议中出现未知角色编码。
     */
    UNKNOWN_ROLE_CODE(ResponseCode.APPLICATION_ERROR, "未知角色编码: %s"),

    /**
     * 数据库或协议中出现未知权限码。
     */
    UNKNOWN_PERMISSION_CODE(ResponseCode.APPLICATION_ERROR, "未知权限码: %s");

    private final ResponseCode responseCode;

    private final String message;

    AuthErrorReason(ResponseCode responseCode, String message) {
        this.responseCode = responseCode;
        this.message = message;
    }

    /**
     * @description: 获取认证异常原因对应的统一响应码
     * @author: yesterday'jam
     * @date: 2026/06/09
     * @注意: 前端只应依赖 ResponseCode.code 做协议判断
     */
    @Override
    public ResponseCode getResponseCode() {
        return responseCode;
    }

    /**
     * @description: 获取认证异常原因受控文案
     * @author: yesterday'jam
     * @date: 2026/06/09
     * @注意: 认证失败文案需避免泄露账号存在性和内部令牌状态
     */
    @Override
    public String getMessage() {
        return message;
    }
}
