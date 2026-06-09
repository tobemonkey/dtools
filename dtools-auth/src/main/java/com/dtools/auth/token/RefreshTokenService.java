package com.dtools.auth.token;

import com.dtools.auth.enums.AuthErrorReason;
import com.dtools.common.exception.ApplicationException;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

/**
 * @description: Refresh Token 明文生成与 SHA-256 哈希服务
 * @author: yesterday'jam
 * @date: 2026/06/08
 * @注意: 服务端只保存 hashToken 返回值，禁止保存 generateToken 返回的明文
 */
@Service
public class RefreshTokenService {

    private static final int TOKEN_BYTES = 48;

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * @description: 生成新的刷新凭证明文
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: 明文仅返回客户端，落库前必须调用 hashToken
     */
    public String generateToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * @description: 对刷新凭证明文计算 SHA-256 哈希
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: 该哈希用于查询、撤销和轮换 refresh token
     */
    public String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new ApplicationException(AuthErrorReason.SHA256_UNSUPPORTED, exception);
        }
    }
}
