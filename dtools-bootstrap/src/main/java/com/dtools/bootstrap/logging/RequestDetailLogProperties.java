package com.dtools.bootstrap.logging;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @description: 请求详情日志配置属性，支持通过配置文件、环境变量和 JVM 参数覆盖
 * @author: yesterday'jam
 * @date: 2026/06/09
 * @注意: 请求详情日志是诊断能力，关闭后不得影响正常请求处理
 */
@Component
@ConfigurationProperties(prefix = "dtools.log.request-detail")
public class RequestDetailLogProperties {

    /**
     * 是否启用请求详情日志。
     */
    private Boolean enabled = false;

    /**
     * 是否异步写入请求详情日志。
     */
    private Boolean asyncEnabled = true;

    /**
     * 是否记录 query string。
     */
    private Boolean includeQuery = true;

    /**
     * 是否记录 JSON 请求体。
     */
    private Boolean includeBody = false;

    /**
     * 请求体日志最大长度，超出后截断。
     */
    private Integer maxBodyLength = 2000;

    /**
     * 异步队列容量，队列满时丢弃请求详情日志。
     */
    private Integer queueCapacity = 10000;

    /**
     * 日志保留天数。
     */
    private Integer retentionDays = 14;

    /**
     * 请求详情日志总大小上限。
     */
    private String totalSizeCap = "1GB";

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Boolean getAsyncEnabled() {
        return asyncEnabled;
    }

    public void setAsyncEnabled(Boolean asyncEnabled) {
        this.asyncEnabled = asyncEnabled;
    }

    public Boolean getIncludeQuery() {
        return includeQuery;
    }

    public void setIncludeQuery(Boolean includeQuery) {
        this.includeQuery = includeQuery;
    }

    public Boolean getIncludeBody() {
        return includeBody;
    }

    public void setIncludeBody(Boolean includeBody) {
        this.includeBody = includeBody;
    }

    public Integer getMaxBodyLength() {
        return maxBodyLength;
    }

    public void setMaxBodyLength(Integer maxBodyLength) {
        this.maxBodyLength = maxBodyLength;
    }

    public Integer getQueueCapacity() {
        return queueCapacity;
    }

    public void setQueueCapacity(Integer queueCapacity) {
        this.queueCapacity = queueCapacity;
    }

    public Integer getRetentionDays() {
        return retentionDays;
    }

    public void setRetentionDays(Integer retentionDays) {
        this.retentionDays = retentionDays;
    }

    public String getTotalSizeCap() {
        return totalSizeCap;
    }

    public void setTotalSizeCap(String totalSizeCap) {
        this.totalSizeCap = totalSizeCap;
    }
}

