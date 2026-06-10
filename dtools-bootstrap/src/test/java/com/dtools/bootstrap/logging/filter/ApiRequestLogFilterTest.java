package com.dtools.bootstrap.logging.filter;

import com.dtools.bootstrap.logging.event.ApiRequestLogEvent;
import com.dtools.bootstrap.logging.event.ApiRequestLogEventFactory;
import com.dtools.bootstrap.logging.properties.ApiRequestLogProperties;
import com.dtools.bootstrap.logging.sanitize.RequestErrorParamSnapshotBuilder;
import com.dtools.bootstrap.logging.writer.ApiRequestLogDbWriter;
import com.dtools.bootstrap.mapper.ApiRequestLogMapper;
import com.dtools.common.trace.TraceContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * @description: 接口 DB 请求日志过滤器测试，验证响应体包装、耗时采集和事件投递
 * @author: yesterday'jam
 * @date: 2026/06/10
 * @注意: 测试不连接真实数据库，只验证 Filter 采集结果
 */
class ApiRequestLogFilterTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @AfterEach
    void tearDown() {
        TraceContext.clear();
    }

    /**
     * @description: 验证 Filter 从 ApiResponse JSON 中解析 responseCode 并投递事件
     * @author: yesterday'jam
     * @date: 2026/06/10
     * @注意: responseCode 为 0 时不记录失败参数快照
     */
    @Test
    void filterShouldPublishDbLogEventWithResponseCode() throws Exception {
        ApiRequestLogProperties properties = new ApiRequestLogProperties();
        properties.setEnabled(true);
        properties.setAsyncEnabled(false);
        CapturingApiRequestLogDbWriter writer = new CapturingApiRequestLogDbWriter(properties);
        ApiRequestLogFilter filter = new ApiRequestLogFilter(
                properties,
                new ApiRequestLogEventFactory(objectMapper, new RequestErrorParamSnapshotBuilder(objectMapper)),
                writer
        );
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/health");
        request.setRemoteAddr("127.0.0.1");
        request.addHeader("User-Agent", "JUnit");
        MockHttpServletResponse response = new MockHttpServletResponse();
        TraceContext.setTraceId("trace-filter");

        filter.doFilter(request, response, (wrappedRequest, wrappedResponse) -> {
            wrappedResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
            wrappedResponse.getWriter().write("{\"code\":0,\"message\":\"成功\"}");
        });

        assertThat(response.getContentAsString()).isEqualTo("{\"code\":0,\"message\":\"成功\"}");
        assertThat(writer.event.getTraceId()).isEqualTo("trace-filter");
        assertThat(writer.event.getResponseCode()).isEqualTo(0);
        assertThat(writer.event.getHttpStatus()).isEqualTo(200);
        assertThat(writer.event.getRequestParamsOnError()).isNull();
    }

    private static class CapturingApiRequestLogDbWriter extends ApiRequestLogDbWriter {

        private ApiRequestLogEvent event;

        CapturingApiRequestLogDbWriter(ApiRequestLogProperties properties) {
            super(properties, mock(ApiRequestLogMapper.class));
        }

        @Override
        public void write(ApiRequestLogEvent event) {
            this.event = event;
        }
    }
}
