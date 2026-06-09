package com.dtools.auth.token;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @description: Refresh Token 服务测试，验证明文生成和 SHA-256 哈希行为
 * @author: yesterday'jam
 * @date: 2026/06/08
 * @注意: 服务端只允许保存 hashToken 返回值
 */
class RefreshTokenServiceTest {

    /**
     * @description: 验证刷新凭证哈希稳定且不会等于明文
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: SHA-256 十六进制结果长度应为 64
     */
    @Test
    void hashTokenShouldBeStableAndOpaque() {
        RefreshTokenService service = new RefreshTokenService();
        String token = service.generateToken();
        String firstHash = service.hashToken(token);
        String secondHash = service.hashToken(token);

        assertThat(token).isNotBlank();
        assertThat(firstHash).hasSize(64);
        assertThat(firstHash).isEqualTo(secondHash);
        assertThat(firstHash).isNotEqualTo(token);
    }
}
