package com.dtools.bootstrap.monitor.model;

import java.time.LocalDateTime;

/**
 * @description: 最近接口错误摘要，用于错误列表和接口详情排查
 * @author: yesterday'jam
 * @date: 2026/06/10
 * @注意: 只返回诊断摘要，不返回 exception_stack 和请求参数快照
 */
public class ApiMonitorRecentErrorDTO {

    /**
     * 请求链路追踪 ID。
     */
    private String traceId;

    /**
     * HTTP 方法。
     */
    private String method;

    /**
     * 请求路径。
     */
    private String uri;

    /**
     * HTTP 响应状态码。
     */
    private Integer httpStatus;

    /**
     * 统一响应码。
     */
    private Integer responseCode;

    /**
     * 接口耗时，单位毫秒。
     */
    private Long costMs;

    /**
     * 异常类型。
     */
    private String exceptionType;

    /**
     * 异常摘要。
     */
    private String exceptionMessage;

    /**
     * 日志创建时间。
     */
    private LocalDateTime createdAt;

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public Integer getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(Integer httpStatus) {
        this.httpStatus = httpStatus;
    }

    public Integer getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(Integer responseCode) {
        this.responseCode = responseCode;
    }

    public Long getCostMs() {
        return costMs;
    }

    public void setCostMs(Long costMs) {
        this.costMs = costMs;
    }

    public String getExceptionType() {
        return exceptionType;
    }

    public void setExceptionType(String exceptionType) {
        this.exceptionType = exceptionType;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }

    public void setExceptionMessage(String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
