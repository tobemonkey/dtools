package com.dtools.common.exception;

import com.dtools.common.enums.ResponseCode;

/**
 * @description: 系统异常，适用于基础设施、不可恢复运行时错误等系统级失败
 * @author: yesterday'jam
 * @date: 2026/06/08
 * @注意: 接口返回统一系统异常文案，详细堆栈只进入日志
 */
public class SystemException extends RuntimeException {

    private final ResponseCode responseCode;

    public SystemException(String message) {
        super(message);
        this.responseCode = ResponseCode.SYSTEM_ERROR;
    }

    public SystemException(String message, Throwable cause) {
        super(message, cause);
        this.responseCode = ResponseCode.SYSTEM_ERROR;
    }

    /**
     * @description: 获取系统异常对应响应码
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: 当前系统异常统一映射为系统错误
     */
    public ResponseCode getResponseCode() {
        return responseCode;
    }
}
