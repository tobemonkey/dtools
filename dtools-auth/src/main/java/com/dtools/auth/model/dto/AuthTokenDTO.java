package com.dtools.auth.model.dto;

/**
 * @description: 登录或刷新成功后的 Token 响应
 * @author: yesterday'jam
 * @date: 2026/06/08
 * @注意: refreshToken 仅为返回给客户端的明文，服务端只保存哈希
 */
public class AuthTokenDTO {

    /**
     * 短期访问 Token。
     */
    private String accessToken;

    /**
     * 长期刷新 Token。
     */
    private String refreshToken;

    /**
     * Token 类型，当前固定为 Bearer。
     */
    private String tokenType;

    /**
     * Access Token 剩余有效秒数。
     */
    private Long expiresInSeconds;

    /**
     * 当前用户权限摘要。
     */
    private CurrentUserDTO currentUser;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public Long getExpiresInSeconds() {
        return expiresInSeconds;
    }

    public void setExpiresInSeconds(Long expiresInSeconds) {
        this.expiresInSeconds = expiresInSeconds;
    }

    public CurrentUserDTO getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(CurrentUserDTO currentUser) {
        this.currentUser = currentUser;
    }
}
