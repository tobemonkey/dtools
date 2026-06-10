package com.dtools.bootstrap.monitor.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @description: 接口错误统计汇总响应 DTO
 * @author: yesterday'jam
 * @date: 2026/06/10
 * @注意: 最近错误只返回摘要字段，避免暴露堆栈和请求快照
 */
public class ApiMonitorErrorSummaryDTO {

    /**
     * 错误类型分布。
     */
    private List<ApiMonitorErrorTypeItemDTO> errorTypeItems = new ArrayList<>();

    /**
     * HTTP 状态分布。
     */
    private List<ApiMonitorStatusItemDTO> statusItems = new ArrayList<>();

    /**
     * 最近错误列表。
     */
    private List<ApiMonitorRecentErrorDTO> recentErrors = new ArrayList<>();

    public List<ApiMonitorErrorTypeItemDTO> getErrorTypeItems() {
        return errorTypeItems;
    }

    public void setErrorTypeItems(List<ApiMonitorErrorTypeItemDTO> errorTypeItems) {
        this.errorTypeItems = errorTypeItems;
    }

    public List<ApiMonitorStatusItemDTO> getStatusItems() {
        return statusItems;
    }

    public void setStatusItems(List<ApiMonitorStatusItemDTO> statusItems) {
        this.statusItems = statusItems;
    }

    public List<ApiMonitorRecentErrorDTO> getRecentErrors() {
        return recentErrors;
    }

    public void setRecentErrors(List<ApiMonitorRecentErrorDTO> recentErrors) {
        this.recentErrors = recentErrors;
    }
}
