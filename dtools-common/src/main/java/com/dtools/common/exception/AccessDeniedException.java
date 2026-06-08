package com.dtools.common.exception;

import com.dtools.common.enums.ResponseCode;

/**
 * @description: 权限异常，适用于已认证但无权访问资源的场景
 * @author: yesterday'jam
 * @date: 2026/06/08
 * @注意: 当前框架暂未实现权限体系，仅预留异常语义
 */
public class AccessDeniedException extends ClientException {

    public AccessDeniedException(String message) {
        super(ResponseCode.FORBIDDEN, message);
    }
}
