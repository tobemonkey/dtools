package com.dtools.common.exception;

import com.dtools.common.enums.ErrorReason;
import com.dtools.common.enums.ResponseCode;

/**
 * @description: 认证异常，适用于未登录、凭证缺失或凭证无效场景
 * @author: yesterday'jam
 * @date: 2026/06/08
 * @注意: 当前框架暂未实现认证流程，仅预留异常语义
 */
public class AuthenticationException extends ClientException {

    public AuthenticationException(String message) {
        super(ResponseCode.UNAUTHORIZED, message);
    }

    public AuthenticationException(ErrorReason errorReason) {
        super(errorReason);
    }

    public AuthenticationException(ErrorReason errorReason, Object... messageArgs) {
        super(errorReason, messageArgs);
    }
}
