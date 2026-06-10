package com.dtools.bootstrap.monitor.model;

/**
 * @description: 接口调用 HTTP 状态分布项
 * @author: yesterday'jam
 * @date: 2026/06/10
 * @注意: status 使用字符串便于前端同时展示 2xx 分组或具体状态码
 */
public class ApiMonitorStatusItemDTO {

    /**
     * HTTP 状态码或状态分组。
     */
    private String status;

    /**
     * 当前状态命中的请求数量。
     */
    private Long count;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }
}
