package com.dtools.bootstrap.logging.sanitize;

import com.dtools.common.log.LogSensitive;
import com.dtools.common.log.SensitiveStrategy;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @description: 请求体脱敏测试，验证请求详情日志只按字段注解处理敏感数据
 * @author: yesterday'jam
 * @date: 2026/06/09
 * @注意: 未加注解字段必须保持原值，避免运行时字段名兜底造成隐形规则
 */
class RequestBodySanitizerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final RequestBodySanitizer sanitizer = new RequestBodySanitizer(objectMapper);

    /**
     * @description: 验证注解字段按各自策略脱敏
     * @author: yesterday'jam
     * @date: 2026/06/09
     * @注意: username 未加注解，应保持原始值方便排障
     */
    @Test
    void sanitizeShouldMaskAnnotatedFields() throws Exception {
        String body = """
                {
                  "username": "owner",
                  "password": "plain-password",
                  "refreshToken": "refresh-token-value",
                  "email": "owner@example.com",
                  "phone": "13812345678"
                }
                """;

        String sanitized = sanitizer.sanitize(body, SampleCommand.class, 2000);

        JsonNode node = objectMapper.readTree(sanitized);
        assertThat(node.get("username").asText()).isEqualTo("owner");
        assertThat(node.get("password").asText()).isEqualTo("******");
        assertThat(node.get("refreshToken").asText()).isEqualTo("[HIDDEN]");
        assertThat(node.get("email").asText()).isEqualTo("o***@example.com");
        assertThat(node.get("phone").asText()).isEqualTo("138****5678");
    }

    /**
     * @description: 验证未加注解字段不按字段名兜底脱敏
     * @author: yesterday'jam
     * @date: 2026/06/09
     * @注意: 后续新增 DTO 必须显式添加注解，运行时不猜测字段语义
     */
    @Test
    void sanitizeShouldKeepUnannotatedSensitiveLikeField() throws Exception {
        String body = """
                {
                  "token": "looks-sensitive-but-not-annotated"
                }
                """;

        String sanitized = sanitizer.sanitize(body, NoAnnotationCommand.class, 2000);

        JsonNode node = objectMapper.readTree(sanitized);
        assertThat(node.get("token").asText()).isEqualTo("looks-sensitive-but-not-annotated");
    }

    /**
     * @description: 验证超长请求体会被截断
     * @author: yesterday'jam
     * @date: 2026/06/09
     * @注意: 截断失败也不能向外抛异常影响请求主链路
     */
    @Test
    void sanitizeShouldLimitBodyLength() {
        String body = "{\"remark\":\"1234567890\"}";

        String sanitized = sanitizer.sanitize(body, NoAnnotationCommand.class, 10);

        assertThat(sanitized).isEqualTo("{\"remark\":...");
    }

    private static class SampleCommand {

        @LogSensitive(strategy = SensitiveStrategy.MASK)
        private String password;

        @LogSensitive(strategy = SensitiveStrategy.HIDDEN)
        private String refreshToken;

        @LogSensitive(strategy = SensitiveStrategy.EMAIL)
        private String email;

        @LogSensitive(strategy = SensitiveStrategy.MOBILE)
        private String phone;
    }

    private static class NoAnnotationCommand {

        private String token;
    }
}

