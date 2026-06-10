package com.dtools.bootstrap.monitor.model;

/**
 * @description: 接口耗时桶统计项
 * @author: yesterday'jam
 * @date: 2026/06/10
 * @注意: bucketName 由后端按固定区间生成，前端只负责展示
 */
public class ApiMonitorLatencyBucketDTO {

    /**
     * 耗时桶名称。
     */
    private String bucketName;

    /**
     * 当前耗时桶请求数量。
     */
    private Long count;

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }
}
