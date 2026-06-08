package com.dtools.common.exception;

import com.dtools.common.enums.ResponseCode;

/**
 * @description: 应用业务异常，适用于内部业务处理失败但不应暴露详细原因的场景
 * @author: yesterday'jam
 * @date: 2026/06/08
 * @注意: 该异常详细 message 只进入日志，接口返回统一业务异常文案
 */
public class ApplicationException extends BizException {

    public ApplicationException(String message) {
        super(ResponseCode.APPLICATION_ERROR, message);
    }

    public ApplicationException(String message, Throwable cause) {
        super(ResponseCode.APPLICATION_ERROR, message, cause);
    }

    public ApplicationException(ResponseCode responseCode, String message) {
        super(responseCode, message);
    }
}
