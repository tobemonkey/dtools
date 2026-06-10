package com.dtools.bootstrap.monitor.model;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * @description: 接口调用统计查询条件，承载时间范围、接口关键字和接口标识
 * @author: yesterday'jam
 * @date: 2026/06/10
 * @注意: startTime 和 endTime 为空时表示不限制对应时间边界
 */
public class ApiMonitorQuery {

    /**
     * 查询开始时间，按 api_request_log.created_at 过滤。
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startTime;

    /**
     * 查询结束时间，按 api_request_log.created_at 过滤。
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endTime;

    /**
     * 接口关键字，匹配 method 或 uri。
     */
    private String keyword;

    /**
     * HTTP 方法，接口详情查询使用。
     */
    private String method;

    /**
     * 接口路径，接口详情查询使用。
     */
    private String uri;

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
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
}
