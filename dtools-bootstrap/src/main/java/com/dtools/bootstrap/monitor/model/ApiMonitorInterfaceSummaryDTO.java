package com.dtools.bootstrap.monitor.model;

import java.math.BigDecimal;

/**
 * @description: 接口维度调用统计摘要 DTO
 * @author: yesterday'jam
 * @date: 2026/06/10
 * @注意: method 和 uri 共同定位一个接口
 */
public class ApiMonitorInterfaceSummaryDTO {

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
     * 错误请求数。
     */
    private Long errorCount = 0L;

    /**
     * 错误率，errorCount / requestCount。
     */
    private BigDecimal errorRate = BigDecimal.ZERO;

    /**
     * 平均耗时，单位毫秒。
     */
    private Long avgCostMs = 0L;

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
     * 最近一次请求 TraceID。
     */
    private String latestTraceId;

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

    public Long getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(Long errorCount) {
        this.errorCount = errorCount;
    }

    public BigDecimal getErrorRate() {
        return errorRate;
    }

    public void setErrorRate(BigDecimal errorRate) {
        this.errorRate = errorRate;
    }

    public Long getAvgCostMs() {
        return avgCostMs;
    }

    public void setAvgCostMs(Long avgCostMs) {
        this.avgCostMs = avgCostMs;
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

    public String getLatestTraceId() {
        return latestTraceId;
    }

    public void setLatestTraceId(String latestTraceId) {
        this.latestTraceId = latestTraceId;
    }
}
