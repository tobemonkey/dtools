package com.dtools.bootstrap.logging;

import com.dtools.bootstrap.mapper.ApiRequestLogMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * @description: 接口 DB 请求日志写入器测试，验证写库失败不影响请求主流程
 * @author: yesterday'jam
 * @date: 2026/06/10
 * @注意: 请求日志属于诊断能力，写库异常只记录日志并吞掉
 */
class ApiRequestLogDbWriterTest {

    /**
     * @description: 验证同步写库异常不会向外抛出
     * @author: yesterday'jam
     * @date: 2026/06/10
     * @注意: asyncEnabled 关闭便于单元测试直接验证失败隔离
     */
    @Test
    void writeShouldSwallowMapperException() {
        ApiRequestLogProperties properties = new ApiRequestLogProperties();
        properties.setEnabled(true);
        properties.setAsyncEnabled(false);
        ApiRequestLogMapper mapper = mock(ApiRequestLogMapper.class);
        ApiRequestLogDbWriter writer = new ApiRequestLogDbWriter(properties, mapper);
        ApiRequestLogEvent event = new ApiRequestLogEvent();
        event.setTraceId("trace-db");
        event.setMethod("GET");
        event.setUri("/api/health");
        event.setHttpStatus(200);
        event.setResponseCode(0);
        event.setCostMs(10L);
        doThrow(new RuntimeException("database unavailable")).when(mapper).insert(event);

        assertThatNoException().isThrownBy(() -> writer.write(event));

        verify(mapper).insert(event);
    }
}

