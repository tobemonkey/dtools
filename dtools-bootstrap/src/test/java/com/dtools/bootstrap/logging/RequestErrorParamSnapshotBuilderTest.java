package com.dtools.bootstrap.logging;

import com.dtools.common.log.LogSensitive;
import com.dtools.common.log.SensitiveStrategy;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @description: 失败请求参数快照构造器测试，验证 query 与 body 合并以及异常诊断脱敏规则
 * @author: yesterday'jam
 * @date: 2026/06/10
 * @注意: 异常诊断模式下仅 HIDDEN 字段隐藏，其余注解字段保留原值
 */
class RequestErrorParamSnapshotBuilderTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final RequestErrorParamSnapshotBuilder builder = new RequestErrorParamSnapshotBuilder(objectMapper);

    /**
     * @description: 验证失败参数快照合并 query 和 body
     * @author: yesterday'jam
     * @date: 2026/06/10
     * @注意: password 使用 MASK 注解但异常诊断模式保留原值，refreshToken 使用 HIDDEN 必须隐藏
     */
    @Test
    void buildShouldMergeQueryAndBodyWithOnlyHiddenFieldsMasked() throws Exception {
        String body = """
                {
                  "username": "owner",
                  "password": "plain-password",
                  "refreshToken": "refresh-token-value"
                }
                """;

        String snapshot = builder.build("debug=true&page=1", body, SampleCommand.class, 20000);

        JsonNode root = objectMapper.readTree(snapshot);
        assertThat(root.get("query").get("debug").asText()).isEqualTo("true");
        assertThat(root.get("query").get("page").asText()).isEqualTo("1");
        assertThat(root.get("body").get("username").asText()).isEqualTo("owner");
        assertThat(root.get("body").get("password").asText()).isEqualTo("plain-password");
        assertThat(root.get("body").get("refreshToken").asText()).isEqualTo("[HIDDEN]");
    }

    /**
     * @description: 验证失败参数快照按最大长度截断
     * @author: yesterday'jam
     * @date: 2026/06/10
     * @注意: 截断用于保护数据库字段体积，不作为查询条件
     */
    @Test
    void buildShouldLimitSnapshotLength() {
        String snapshot = builder.build("keyword=1234567890", null, null, 12);

        assertThat(snapshot).isEqualTo("{\"query\":{\"k...");
    }

    private static class SampleCommand {

        @LogSensitive(strategy = SensitiveStrategy.MASK)
        private String password;

        @LogSensitive(strategy = SensitiveStrategy.HIDDEN)
        private String refreshToken;
    }
}

