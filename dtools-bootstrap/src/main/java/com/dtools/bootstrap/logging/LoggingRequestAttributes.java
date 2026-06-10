package com.dtools.bootstrap.logging;

/**
 * @description: 日志系统请求属性常量，统一在 Filter 和异常处理器之间传递诊断上下文
 * @author: yesterday'jam
 * @date: 2026/06/10
 * @注意: 属性值只在单次请求内使用，请求结束后由容器释放
 */
public final class LoggingRequestAttributes {

    public static final String HANDLED_EXCEPTION = LoggingRequestAttributes.class.getName() + ".HANDLED_EXCEPTION";

    public static final String REQUEST_BODY_TYPE = LoggingRequestAttributes.class.getName() + ".REQUEST_BODY_TYPE";

    private LoggingRequestAttributes() {
    }
}

