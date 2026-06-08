package com.dtools.auth.model.entity;

import java.time.LocalDateTime;

/**
 * @description: 刷新凭证实体，映射 auth_refresh_token 生命周期数据
 * @author: yesterday'jam
 * @date: 2026/06/08
 * @注意: tokenHash 是唯一可落库凭证，不保存 refresh token 明文
 */
public class AuthRefreshTokenEntity {

    private Long id;

    private Long userId;

    private String tokenHash;

    private LocalDateTime expiresAt;

    private LocalDateTime revokedAt;

    private String replacedByHash;

    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getRevokedAt() {
        return revokedAt;
    }

    public void setRevokedAt(LocalDateTime revokedAt) {
        this.revokedAt = revokedAt;
    }

    public String getReplacedByHash() {
        return replacedByHash;
    }

    public void setReplacedByHash(String replacedByHash) {
        this.replacedByHash = replacedByHash;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
