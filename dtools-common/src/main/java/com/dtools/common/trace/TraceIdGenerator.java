package com.dtools.common.trace;

import java.util.UUID;

/**
 * @description: TraceID 生成工具，负责为请求链路生成稳定追踪标识
 * @author: yesterday'jam
 * @date: 2026/06/08
 * @注意: 后续如接入网关或日志系统，可在此统一调整格式
 */
public final class TraceIdGenerator {

    private TraceIdGenerator() {
    }

    /**
     * @description: 生成新的 TraceID
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: 当前使用 UUID 去中划线格式，便于日志检索
     */
    public static String nextTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
