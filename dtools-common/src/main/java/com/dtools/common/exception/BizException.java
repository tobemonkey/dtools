package com.dtools.common.exception;

import com.dtools.common.enums.ResponseCode;

/**
 * @description: 受控业务异常，携带统一响应码供全局异常处理转换
 * @author: yesterday'jam
 * @date: 2026/06/08
 * @注意: 业务可预期失败应抛出该异常，不应直接返回散乱错误结构
 */
public class BizException extends RuntimeException {

    private final ResponseCode responseCode;

    public BizException(ResponseCode responseCode, String message) {
        super(message);
        this.responseCode = responseCode;
    }

    /**
     * @description: 获取业务异常对应响应码
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: 全局异常处理会使用该响应码构造接口返回
     */
    public ResponseCode getResponseCode() {
        return responseCode;
    }
}
