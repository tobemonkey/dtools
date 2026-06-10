package com.dtools.bootstrap.logging.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @description: 接口 DB 请求日志配置属性，控制请求主账本日志是否写入数据库
 * @author: yesterday'jam
 * @date: 2026/06/10
 * @注意: DB 日志默认关闭，避免未建表环境影响本地开发
 */
@Component
@ConfigurationProperties(prefix = "dtools.log.api-db")
public class ApiRequestLogProperties {

    /**
     * 是否启用接口 DB 请求日志。
     */
    private Boolean enabled = false;

    /**
     * 是否异步写入 DB。
     */
    private Boolean asyncEnabled = true;

    /**
     * 异步队列容量。
     */
    private Integer queueCapacity = 10000;

    /**
     * 请求体缓存最大字节数。
     */
    private Integer maxRequestCacheLength = 20000;

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

    public Integer getQueueCapacity() {
        return queueCapacity;
    }

    public void setQueueCapacity(Integer queueCapacity) {
        this.queueCapacity = queueCapacity;
    }

    public Integer getMaxRequestCacheLength() {
        return maxRequestCacheLength;
    }

    public void setMaxRequestCacheLength(Integer maxRequestCacheLength) {
        this.maxRequestCacheLength = maxRequestCacheLength;
    }
}

