package com.dtools.bootstrap.logging;

/**
 * @description: 接口 DB 请求日志事件，承载一次 API 请求的数据库日志快照
 * @author: yesterday'jam
 * @date: 2026/06/10
 * @注意: 失败请求参数只在请求失败时写入
 */
public class ApiRequestLogEvent {

    private String traceId;

    private Long userId;

    private String username;

    private String method;

    private String uri;

    private String clientIp;

    private String userAgent;

    private Integer httpStatus;

    private Integer responseCode;

    private Long costMs;

    private String exceptionType;

    private String exceptionMessage;

    private String exceptionStack;

    private String requestParamsOnError;

    public boolean isFailure() {
        if (responseCode != null) {
            return responseCode != 0;
        }
        return httpStatus != null && httpStatus >= 400;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
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

    public String getExceptionStack() {
        return exceptionStack;
    }

    public void setExceptionStack(String exceptionStack) {
        this.exceptionStack = exceptionStack;
    }

    public String getRequestParamsOnError() {
        return requestParamsOnError;
    }

    public void setRequestParamsOnError(String requestParamsOnError) {
        this.requestParamsOnError = requestParamsOnError;
    }
}

