package com.dtools.bootstrap.monitor.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;

/**
 * @description: 慢接口统计响应 DTO
 * @author: yesterday'jam
 * @date: 2026/06/10
 * @注意: timeoutRate 使用 0 到 1 的小数表示
 */
public class ApiMonitorSlowEndpointDTO {

    /**
     * HTTP 方法。
     */
    private String method;

    /**
     * 请求路径。
     */
    private String uri;

    /**
     * 请求总数。
     */
    private Long requestCount = 0L;

    /**
     * 平均耗时，单位毫秒。
     */
    private Long avgCostMs = 0L;

    /**
     * 90 分位耗时，单位毫秒。
     */
    private Long t90CostMs = 0L;

    /**
     * 95 分位耗时，单位毫秒。
     */
    private Long t95CostMs = 0L;

    /**
     * 99 分位耗时，单位毫秒。
     */
    private Long t99CostMs = 0L;

    /**
     * 最大耗时，单位毫秒。
     */
    private Long maxCostMs = 0L;

    /**
     * 超时率。
     */
    private BigDecimal timeoutRate = BigDecimal.ZERO;

    /**
     * Mapper 内部回填字段，表示超时请求数。
     */
    @JsonIgnore
    private Long timeoutCount = 0L;

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

    public Long getRequestCount() {
        return requestCount;
    }

    public void setRequestCount(Long requestCount) {
        this.requestCount = requestCount;
    }

    public Long getAvgCostMs() {
        return avgCostMs;
    }

    public void setAvgCostMs(Long avgCostMs) {
        this.avgCostMs = avgCostMs;
    }

    public Long getT90CostMs() {
        return t90CostMs;
    }

    public void setT90CostMs(Long t90CostMs) {
        this.t90CostMs = t90CostMs;
    }

    public Long getT95CostMs() {
        return t95CostMs;
    }

    public void setT95CostMs(Long t95CostMs) {
        this.t95CostMs = t95CostMs;
    }

    public Long getT99CostMs() {
        return t99CostMs;
    }

    public void setT99CostMs(Long t99CostMs) {
        this.t99CostMs = t99CostMs;
    }

    public Long getMaxCostMs() {
        return maxCostMs;
    }

    public void setMaxCostMs(Long maxCostMs) {
        this.maxCostMs = maxCostMs;
    }

    public BigDecimal getTimeoutRate() {
        return timeoutRate;
    }

    public void setTimeoutRate(BigDecimal timeoutRate) {
        this.timeoutRate = timeoutRate;
    }

    public Long getTimeoutCount() {
        return timeoutCount;
    }

    public void setTimeoutCount(Long timeoutCount) {
        this.timeoutCount = timeoutCount;
    }
}
