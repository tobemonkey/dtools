package com.dtools.bootstrap.monitor.model;

import java.time.LocalDateTime;

/**
 * @description: 接口调用趋势点，按小时聚合请求量、错误量和平均耗时
 * @author: yesterday'jam
 * @date: 2026/06/10
 * @注意: bucketTime 为聚合桶起始时间
 */
public class ApiMonitorTrendPointDTO {

    /**
     * 趋势时间桶起始时间。
     */
    private LocalDateTime bucketTime;

    /**
     * 当前时间桶请求总数。
     */
    private Long totalCount;

    /**
     * 当前时间桶错误请求数。
     */
    private Long errorCount;

    /**
     * 当前时间桶平均耗时，单位毫秒。
     */
    private Long avgCostMs;

    public LocalDateTime getBucketTime() {
        return bucketTime;
    }

    public void setBucketTime(LocalDateTime bucketTime) {
        this.bucketTime = bucketTime;
    }

    public Long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
    }

    public Long getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(Long errorCount) {
        this.errorCount = errorCount;
    }

    public Long getAvgCostMs() {
        return avgCostMs;
    }

    public void setAvgCostMs(Long avgCostMs) {
        this.avgCostMs = avgCostMs;
    }
}
