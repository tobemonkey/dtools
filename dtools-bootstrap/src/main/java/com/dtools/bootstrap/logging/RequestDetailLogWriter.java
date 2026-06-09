package com.dtools.bootstrap.logging;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @description: 请求详情日志写入器，负责异步写入独立 request-detail 日志
 * @author: yesterday'jam
 * @date: 2026/06/09
 * @注意: 队列满或写入失败时丢弃日志，不阻断业务请求
 */
@Component
public class RequestDetailLogWriter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestDetailLogWriter.class);

    private static final Logger REQUEST_DETAIL_LOGGER = LoggerFactory.getLogger("REQUEST_DETAIL");

    private final RequestDetailLogProperties properties;

    private final ObjectMapper objectMapper;

    private BlockingQueue<RequestDetailLogEvent> queue;

    private Thread worker;

    private volatile boolean running = true;

    public RequestDetailLogWriter(RequestDetailLogProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    /**
     * @description: 初始化异步日志队列和后台线程
     * @author: yesterday'jam
     * @date: 2026/06/09
     * @注意: 即使请求详情日志关闭也创建轻量队列，便于后续通过配置开启
     */
    @PostConstruct
    public void start() {
        int capacity = Math.max(1, properties.getQueueCapacity());
        queue = new ArrayBlockingQueue<>(capacity);
        worker = new Thread(this::drainQueue, "request-detail-log-writer");
        worker.setDaemon(true);
        worker.start();
    }

    /**
     * @description: 停止异步日志线程
     * @author: yesterday'jam
     * @date: 2026/06/09
     * @注意: 关闭阶段最多等待 1 秒，避免影响应用退出
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
     * @description: 投递请求详情日志事件
     * @author: yesterday'jam
     * @date: 2026/06/09
     * @注意: 异步队列满时直接丢弃，主请求线程不得阻塞等待
     */
    public void write(RequestDetailLogEvent event) {
        if (!Boolean.TRUE.equals(properties.getEnabled()) || event == null) {
            return;
        }
        try {
            if (Boolean.TRUE.equals(properties.getAsyncEnabled())) {
                boolean offered = queue.offer(event);
                if (!offered) {
                    LOGGER.warn("请求详情日志队列已满，丢弃本次日志, traceId={}", event.getTraceId());
                }
                return;
            }
            writeNow(event);
        } catch (RuntimeException exception) {
            LOGGER.warn("请求详情日志投递失败, traceId={}, reason={}", event.getTraceId(), exception.getMessage());
        }
    }

    private void drainQueue() {
        while (running || !queue.isEmpty()) {
            try {
                RequestDetailLogEvent event = queue.poll(500L, TimeUnit.MILLISECONDS);
                if (event != null) {
                    writeNow(event);
                }
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                if (!running) {
                    return;
                }
            } catch (RuntimeException exception) {
                LOGGER.warn("请求详情日志异步写入失败, reason={}", exception.getMessage());
            }
        }
    }

    private void writeNow(RequestDetailLogEvent event) {
        try {
            REQUEST_DETAIL_LOGGER.info(objectMapper.writeValueAsString(event));
        } catch (Exception exception) {
            LOGGER.warn("请求详情日志序列化失败, traceId={}, reason={}", event.getTraceId(), exception.getMessage());
        }
    }
}

