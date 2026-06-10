package com.dtools.bootstrap.logging.writer;

import com.dtools.bootstrap.mapper.ApiRequestLogMapper;
import com.dtools.bootstrap.logging.event.ApiRequestLogEvent;
import com.dtools.bootstrap.logging.properties.ApiRequestLogProperties;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @description: 接口 DB 请求日志写入器，负责异步写入 api_request_log
 * @author: yesterday'jam
 * @date: 2026/06/10
 * @注意: 队列满或写库失败时丢弃日志，不阻断业务请求
 */
@Component
public class ApiRequestLogDbWriter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiRequestLogDbWriter.class);

    private final ApiRequestLogProperties properties;

    private final ApiRequestLogMapper apiRequestLogMapper;

    private BlockingQueue<ApiRequestLogEvent> queue;

    private Thread worker;

    private volatile boolean running = true;

    public ApiRequestLogDbWriter(ApiRequestLogProperties properties, ApiRequestLogMapper apiRequestLogMapper) {
        this.properties = properties;
        this.apiRequestLogMapper = apiRequestLogMapper;
    }

    /**
     * @description: 初始化异步写库队列
     * @author: yesterday'jam
     * @date: 2026/06/10
     * @注意: 即使配置关闭也初始化轻量队列，便于运行时通过配置开启
     */
    @PostConstruct
    public void start() {
        int capacity = Math.max(1, properties.getQueueCapacity());
        queue = new ArrayBlockingQueue<>(capacity);
        worker = new Thread(this::drainQueue, "api-request-log-db-writer");
        worker.setDaemon(true);
        worker.start();
    }

    /**
     * @description: 停止异步写库线程
     * @author: yesterday'jam
     * @date: 2026/06/10
     * @注意: 应用退出最多等待 1 秒，避免日志线程拖慢关闭
     */
    @PreDestroy
    public void stop() throws InterruptedException {
        running = false;
        if (worker != null) {
            worker.interrupt();
            worker.join(1000L);
        }
    }

    /**
     * @description: 投递接口请求 DB 日志事件
     * @author: yesterday'jam
     * @date: 2026/06/10
     * @注意: 同步或异步写入异常都只记录警告，不向外抛出
     */
    public void write(ApiRequestLogEvent event) {
        if (!Boolean.TRUE.equals(properties.getEnabled()) || event == null) {
            return;
        }
        try {
            if (Boolean.TRUE.equals(properties.getAsyncEnabled())) {
                boolean offered = queue.offer(event);
                if (!offered) {
                    LOGGER.warn("接口 DB 请求日志队列已满，丢弃本次日志, traceId={}", event.getTraceId());
                }
                return;
            }
            writeNow(event);
        } catch (RuntimeException exception) {
            LOGGER.warn("接口 DB 请求日志投递失败, traceId={}, reason={}", event.getTraceId(), exception.getMessage());
        }
    }

    private void drainQueue() {
        while (running || !queue.isEmpty()) {
            try {
                ApiRequestLogEvent event = queue.poll(500L, TimeUnit.MILLISECONDS);
                if (event != null) {
                    writeNow(event);
                }
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                if (!running) {
                    return;
                }
            } catch (RuntimeException exception) {
                LOGGER.warn("接口 DB 请求日志异步写入失败, reason={}", exception.getMessage());
            }
        }
    }

    private void writeNow(ApiRequestLogEvent event) {
        try {
            apiRequestLogMapper.insert(event);
        } catch (RuntimeException exception) {
            LOGGER.warn("接口 DB 请求日志写入失败, traceId={}, reason={}", event.getTraceId(), exception.getMessage());
        }
    }
}
