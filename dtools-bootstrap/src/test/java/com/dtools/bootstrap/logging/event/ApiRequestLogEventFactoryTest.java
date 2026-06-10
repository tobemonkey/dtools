package com.dtools.bootstrap.logging.event;

import com.dtools.bootstrap.logging.sanitize.RequestErrorParamSnapshotBuilder;
import com.dtools.common.exception.SystemException;
import com.dtools.common.trace.TraceContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @description: 接口 DB 请求日志事件工厂测试，验证响应码、失败判断和异常信息提取
 * @author: yesterday'jam
 * @date: 2026/06/10
 * @注意: responseCode 是业务协议码，失败请求不一定都有完整异常堆栈
 */
class ApiRequestLogEventFactoryTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final ApiRequestLogEventFactory factory = new ApiRequestLogEventFactory(
            objectMapper,
            new RequestErrorParamSnapshotBuilder(objectMapper)
    );

    @AfterEach
    void tearDown() {
        TraceContext.clear();
    }

    /**
     * @description: 验证成功响应只记录基础字段，不记录失败参数和异常信息
     * @author: yesterday'jam
     * @date: 2026/06/10
     * @注意: response_code 为 0 时视为成功
     */
    @Test
    void createShouldTreatResponseCodeZeroAsSuccess() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/health");
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(200);
        TraceContext.setTraceId("trace-ok");

        ApiRequestLogEvent event = factory.create(request, response, "{\"code\":0,\"message\":\"成功\"}", null, 12L);

        assertThat(event.getTraceId()).isEqualTo("trace-ok");
        assertThat(event.getResponseCode()).isEqualTo(0);
        assertThat(event.isFailure()).isFalse();
        assertThat(event.getExceptionType()).isNull();
        assertThat(event.getRequestParamsOnError()).isNull();
    }

    /**
     * @description: 验证非 0 响应码记录失败摘要但不强制记录完整堆栈
     * @author: yesterday'jam
     * @date: 2026/06/10
     * @注意: 业务失败对排查有帮助，但不一定代表系统异常
     */
    @Test
    void createShouldRecordFailureMessageWhenResponseCodeIsNonZero() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");
        request.setQueryString("debug=true");
        request.setContentType("application/json");
        request.setContent("{\"username\":\"owner\"}".getBytes());
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(401);
        TraceContext.setTraceId("trace-fail");

        ApiRequestLogEvent event = factory.create(
                request,
                response,
                "{\"code\":40100,\"message\":\"未认证\"}",
                null,
                30L
        );

        assertThat(event.getResponseCode()).isEqualTo(40100);
        assertThat(event.isFailure()).isTrue();
        assertThat(event.getExceptionType()).isEqualTo("API_RESPONSE_FAILURE");
        assertThat(event.getExceptionMessage()).isEqualTo("未认证");
        assertThat(event.getExceptionStack()).isNull();
        assertThat(event.getRequestParamsOnError()).contains("\"query\"");
    }

    /**
     * @description: 验证系统异常会记录异常类型、消息和堆栈
     * @author: yesterday'jam
     * @date: 2026/06/10
     * @注意: SystemException 属于需要完整堆栈的异常类型
     */
    @Test
    void createShouldRecordStackForSystemException() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/tools/run");
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(500);
        TraceContext.setTraceId("trace-error");
        SystemException exception = new SystemException("数据库不可用");

        ApiRequestLogEvent event = factory.create(
                request,
                response,
                "{\"code\":50000,\"message\":\"系统异常\"}",
                exception,
                50L
        );

        assertThat(event.getExceptionType()).isEqualTo("com.dtools.common.exception.SystemException");
        assertThat(event.getExceptionMessage()).isEqualTo("数据库不可用");
        assertThat(event.getExceptionStack()).contains("SystemException");
    }
}
