package com.dtools.common.exception;

import com.dtools.common.enums.ResponseCode;

/**
 * @description: 客户端可见异常，适用于参数合法但不满足业务可见约束的场景
 * @author: yesterday'jam
 * @date: 2026/06/08
 * @注意: 该异常 message 会返回给调用方，禁止放入敏感内部信息
 */
public class ClientException extends BizException {

    public ClientException(String message) {
        super(ResponseCode.CLIENT_ERROR, message);
    }

    public ClientException(ResponseCode responseCode, String message) {
        super(responseCode, message);
    }

    public ClientException(ResponseCode responseCode, String message, Throwable cause) {
        super(responseCode, message, cause);
    }
}
