package com.dtools.bootstrap.monitor.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @description: 接口调用统计概览响应 DTO
 * @author: yesterday'jam
 * @date: 2026/06/10
 * @注意: 错误率使用 0 到 1 的小数表示，耗时单位统一为毫秒
 */
public class ApiMonitorOverviewDTO {

    /**
     * 请求总数。
     */
    private Long totalCount = 0L;

    /**
     * 成功请求数。
     */
    private Long successCount = 0L;

    /**
     * 错误请求数。
     */
    private Long errorCount = 0L;

    /**
     * 错误率，errorCount / totalCount。
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
     * 最慢请求 HTTP 方法。
     */
    private String slowestMethod;

    /**
     * 最慢请求路径。
     */
    private String slowestUri;

    /**
     * 最慢请求耗时，单位毫秒。
     */
    private Long slowestCostMs = 0L;

    /**
     * HTTP 状态分布。
     */
    private List<ApiMonitorStatusItemDTO> statusSegments = new ArrayList<>();

    /**
     * 小时级趋势点。
     */
    private List<ApiMonitorTrendPointDTO> trendPoints = new ArrayList<>();

    public Long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
    }

    public Long getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(Long successCount) {
        this.successCount = successCount;
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

    public String getSlowestMethod() {
        return slowestMethod;
    }

    public void setSlowestMethod(String slowestMethod) {
        this.slowestMethod = slowestMethod;
    }

    public String getSlowestUri() {
        return slowestUri;
    }

    public void setSlowestUri(String slowestUri) {
        this.slowestUri = slowestUri;
    }

    public Long getSlowestCostMs() {
        return slowestCostMs;
    }

    public void setSlowestCostMs(Long slowestCostMs) {
        this.slowestCostMs = slowestCostMs;
    }

    public List<ApiMonitorStatusItemDTO> getStatusSegments() {
        return statusSegments;
    }

    public void setStatusSegments(List<ApiMonitorStatusItemDTO> statusSegments) {
        this.statusSegments = statusSegments;
    }

    public List<ApiMonitorTrendPointDTO> getTrendPoints() {
        return trendPoints;
    }

    public void setTrendPoints(List<ApiMonitorTrendPointDTO> trendPoints) {
        this.trendPoints = trendPoints;
    }
}
