package com.dtools.bootstrap.logging.event;

/**
 * @description: 请求详情日志事件，承载一次 HTTP 请求的诊断日志快照
 * @author: yesterday'jam
 * @date: 2026/06/09
 * @注意: 事件只保存脱敏后的请求体，不保存未处理的敏感原文
 */
public class RequestDetailLogEvent {

    /**
     * 请求链路追踪 ID。
     */
    private String traceId;

    /**
     * 当前登录用户标识，未登录时为空。
     */
    private String userId;

    /**
     * HTTP 方法。
     */
    private String method;

    /**
     * 请求路径。
     */
    private String uri;

    /**
     * 原始 query string。
     */
    private String query;

    /**
     * HTTP 响应状态码。
     */
    private Integer status;

    /**
     * 请求耗时，单位毫秒。
     */
    private Long costMs;

    /**
     * 客户端 IP。
     */
    private String ip;

    /**
     * User-Agent 摘要。
     */
    private String userAgent;

    /**
     * 脱敏后的 JSON 请求体。
     */
    private String body;

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getCostMs() {
        return costMs;
    }

    public void setCostMs(Long costMs) {
        this.costMs = costMs;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}

