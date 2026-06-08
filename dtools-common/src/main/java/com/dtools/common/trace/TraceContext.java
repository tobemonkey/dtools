package com.dtools.common.trace;

/**
 * @description: TraceID 线程上下文，负责在单次请求链路内传递追踪标识
 * @author: yesterday'jam
 * @date: 2026/06/08
 * @注意: 请求结束必须清理，避免线程复用导致 TraceID 串线
 */
public final class TraceContext {

    private static final ThreadLocal<String> TRACE_ID_HOLDER = new ThreadLocal<>();

    private TraceContext() {
    }

    /**
     * @description: 设置当前线程 TraceID
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: 通常由 Filter 在请求入口设置
     */
    public static void setTraceId(String traceId) {
        TRACE_ID_HOLDER.set(traceId);
    }

    /**
     * @description: 获取当前线程 TraceID
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: 如果未设置则返回 null，调用方可按需兜底
     */
    public static String getTraceId() {
        return TRACE_ID_HOLDER.get();
    }

    /**
     * @description: 清理当前线程 TraceID
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: 请求完成后必须调用
     */
    public static void clear() {
        TRACE_ID_HOLDER.remove();
    }
}
