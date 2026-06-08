package com.dtools.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @description: 鉴权模块配置属性，集中管理 JWT 和 refresh token 生命周期参数
 * @author: yesterday'jam
 * @date: 2026/06/08
 * @注意: 生产环境必须替换 jwtSecret，禁止使用默认开发密钥
 */
@ConfigurationProperties(prefix = "dtools.auth")
public class AuthProperties {

    /**
     * HMAC JWT 签名密钥，HS256 至少需要 32 字节。
     */
    private String jwtSecret = "dtools-local-development-secret-change-me-32-bytes";

    /**
     * Access Token 有效秒数。
     */
    private Long accessTokenSeconds = 1800L;

    /**
     * Refresh Token 有效秒数。
     */
    private Long refreshTokenSeconds = 604800L;

    /**
     * JWT 签发方。
     */
    private String issuer = "dtools";

    public String getJwtSecret() {
        return jwtSecret;
    }

    public void setJwtSecret(String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    public Long getAccessTokenSeconds() {
        return accessTokenSeconds;
    }

    public void setAccessTokenSeconds(Long accessTokenSeconds) {
        this.accessTokenSeconds = accessTokenSeconds;
    }

    public Long getRefreshTokenSeconds() {
        return refreshTokenSeconds;
    }

    public void setRefreshTokenSeconds(Long refreshTokenSeconds) {
        this.refreshTokenSeconds = refreshTokenSeconds;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }
}
