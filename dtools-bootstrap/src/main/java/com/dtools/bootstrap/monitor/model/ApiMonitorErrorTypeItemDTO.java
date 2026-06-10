package com.dtools.bootstrap.monitor.model;

/**
 * @description: 接口错误类型统计项
 * @author: yesterday'jam
 * @date: 2026/06/10
 * @注意: exception_type 为空时由 SQL 归入 UNKNOWN，避免前端空标签
 */
public class ApiMonitorErrorTypeItemDTO {

    /**
     * 错误类型。
     */
    private String errorType;

    /**
     * 当前错误类型出现次数。
     */
    private Long count;

    public String getErrorType() {
        return errorType;
    }

    public void setErrorType(String errorType) {
        this.errorType = errorType;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }
}
