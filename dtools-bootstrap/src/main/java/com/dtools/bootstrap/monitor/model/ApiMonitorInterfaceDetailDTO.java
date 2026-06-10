package com.dtools.bootstrap.monitor.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @description: 单接口调用详情响应 DTO
 * @author: yesterday'jam
 * @date: 2026/06/10
 * @注意: summary 为空时由 Service 返回零值摘要
 */
public class ApiMonitorInterfaceDetailDTO {

    /**
     * 接口摘要。
     */
    private ApiMonitorInterfaceSummaryDTO summary = new ApiMonitorInterfaceSummaryDTO();

    /**
     * 小时级趋势点。
     */
    private List<ApiMonitorTrendPointDTO> trendPoints = new ArrayList<>();

    /**
     * HTTP 状态分布。
     */
    private List<ApiMonitorStatusItemDTO> statusItems = new ArrayList<>();

    /**
     * 错误类型分布。
     */
    private List<ApiMonitorErrorTypeItemDTO> errorTypeItems = new ArrayList<>();

    /**
     * 耗时桶分布。
     */
    private List<ApiMonitorLatencyBucketDTO> latencyBuckets = new ArrayList<>();

    /**
     * 最近错误列表。
     */
    private List<ApiMonitorRecentErrorDTO> recentErrors = new ArrayList<>();

    public ApiMonitorInterfaceSummaryDTO getSummary() {
        return summary;
    }

    public void setSummary(ApiMonitorInterfaceSummaryDTO summary) {
        this.summary = summary;
    }

    public List<ApiMonitorTrendPointDTO> getTrendPoints() {
        return trendPoints;
    }

    public void setTrendPoints(List<ApiMonitorTrendPointDTO> trendPoints) {
        this.trendPoints = trendPoints;
    }

    public List<ApiMonitorStatusItemDTO> getStatusItems() {
        return statusItems;
    }

    public void setStatusItems(List<ApiMonitorStatusItemDTO> statusItems) {
        this.statusItems = statusItems;
    }

    public List<ApiMonitorErrorTypeItemDTO> getErrorTypeItems() {
        return errorTypeItems;
    }

    public void setErrorTypeItems(List<ApiMonitorErrorTypeItemDTO> errorTypeItems) {
        this.errorTypeItems = errorTypeItems;
    }

    public List<ApiMonitorLatencyBucketDTO> getLatencyBuckets() {
        return latencyBuckets;
    }

    public void setLatencyBuckets(List<ApiMonitorLatencyBucketDTO> latencyBuckets) {
        this.latencyBuckets = latencyBuckets;
    }

    public List<ApiMonitorRecentErrorDTO> getRecentErrors() {
        return recentErrors;
    }

    public void setRecentErrors(List<ApiMonitorRecentErrorDTO> recentErrors) {
        this.recentErrors = recentErrors;
    }
}
