package com.dtools.common.response;

import com.dtools.common.enums.ResponseCode;

/**
 * @description: 后端统一接口响应结构，约束 code 必须使用 Integer 类型
 * @author: yesterday'jam
 * @date: 2026/06/08
 * @注意: Controller 返回业务数据时应使用该结构，避免响应协议发散
 */
public class ApiResponse<T> {

    /**
     * 稳定响应码，来源于统一响应码枚举。
     */
    private Integer code;

    /**
     * 响应说明，默认使用响应码中文说明。
     */
    private String message;

    /**
     * 响应数据。
     */
    private T data;

    /**
     * 请求链路追踪 ID。
     */
    private String traceId;

    public ApiResponse() {
    }

    public ApiResponse(Integer code, String message, T data, String traceId) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.traceId = traceId;
    }

    /**
     * @description: 构造成功响应
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: traceId 由调用方从当前请求上下文传入
     */
    public static <T> ApiResponse<T> success(T data, String traceId) {
        return new ApiResponse<>(ResponseCode.SUCCESS.getCode(), ResponseCode.SUCCESS.getDesc(), data, traceId);
    }

    /**
     * @description: 构造失败响应
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: code 必须来自统一响应码枚举
     */
    public static <T> ApiResponse<T> failure(ResponseCode responseCode, String message, String traceId) {
        return new ApiResponse<>(responseCode.getCode(), message, null, traceId);
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }
}
