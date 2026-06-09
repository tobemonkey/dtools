package com.dtools.bootstrap.logging;

import com.dtools.auth.model.command.LoginCommand;
import com.dtools.common.trace.TraceContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletInputStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @description: 请求详情日志过滤器测试，验证请求体缓存、HandlerMethod 类型识别和异步写入投递
 * @author: yesterday'jam
 * @date: 2026/06/09
 * @注意: 测试只验证采集与脱敏结果，不依赖真实文件日志输出
 */
class RequestDetailLogFilterTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @AfterEach
    void tearDown() {
        TraceContext.clear();
    }

    /**
     * @description: 验证 JSON 请求体按 LoginCommand 字段注解脱敏后投递日志事件
     * @author: yesterday'jam
     * @date: 2026/06/09
     * @注意: password 来自 LoginCommand 注解，不依赖字段名兜底规则
     */
    @Test
    void filterShouldPublishSanitizedRequestBody() throws Exception {
        RequestDetailLogProperties properties = new RequestDetailLogProperties();
        properties.setEnabled(true);
        properties.setIncludeBody(true);
        properties.setIncludeQuery(true);
        properties.setAsyncEnabled(false);
        CapturingRequestDetailLogWriter writer = new CapturingRequestDetailLogWriter(properties, objectMapper);
        RequestDetailLogFilter filter = new RequestDetailLogFilter(
                properties,
                new RequestBodySanitizer(objectMapper),
                writer
        );
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");
        request.setContentType(MediaType.APPLICATION_JSON_VALUE);
        request.setQueryString("debug=true");
        request.setRemoteAddr("127.0.0.1");
        request.addHeader("User-Agent", "JUnit");
        request.setContent("""
                {"username":"owner","password":"plain-password"}
                """.getBytes());
        MockHttpServletResponse response = new MockHttpServletResponse();
        TraceContext.setTraceId("trace-test");
        Method loginMethod = SampleController.class.getDeclaredMethod("login", LoginCommand.class);
        HandlerMethod handlerMethod = new HandlerMethod(new SampleController(), loginMethod);

        filter.doFilter(request, response, (wrappedRequest, servletResponse) -> {
            wrappedRequest.setAttribute(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE, handlerMethod);
            try (ServletInputStream inputStream = wrappedRequest.getInputStream()) {
                inputStream.readAllBytes();
            }
            ((MockHttpServletResponse) servletResponse).setStatus(201);
        });

        RequestDetailLogEvent event = writer.event;
        assertThat(event.getTraceId()).isEqualTo("trace-test");
        assertThat(event.getMethod()).isEqualTo("POST");
        assertThat(event.getUri()).isEqualTo("/api/auth/login");
        assertThat(event.getQuery()).isEqualTo("debug=true");
        assertThat(event.getStatus()).isEqualTo(201);
        JsonNode body = objectMapper.readTree(event.getBody());
        assertThat(body.get("username").asText()).isEqualTo("owner");
        assertThat(body.get("password").asText()).isEqualTo("******");
    }

    private static class CapturingRequestDetailLogWriter extends RequestDetailLogWriter {

        private RequestDetailLogEvent event;

        CapturingRequestDetailLogWriter(RequestDetailLogProperties properties, ObjectMapper objectMapper) {
            super(properties, objectMapper);
        }

        @Override
        public void write(RequestDetailLogEvent event) {
            this.event = event;
        }
    }

    private static class SampleController {

        void login(@org.springframework.web.bind.annotation.RequestBody LoginCommand command) {
        }
    }
}

