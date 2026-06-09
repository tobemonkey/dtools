package com.dtools.auth.token;

import com.dtools.auth.config.AuthProperties;
import com.dtools.auth.model.dto.CurrentUserDTO;
import com.dtools.common.exception.ApplicationException;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * @description: JWT Token 签发服务，生成可由 Spring Security Resource Server 校验的 HMAC JWT
 * @author: yesterday'jam
 * @date: 2026/06/08
 * @注意: claims 只放身份摘要和权限，不放密码、配置等敏感或高频变化数据
 */
@Service
public class JwtTokenService {

    private final AuthProperties authProperties;

    private final JwtEncoder jwtEncoder;

    public JwtTokenService(AuthProperties authProperties, JwtEncoder jwtEncoder) {
        this.authProperties = authProperties;
        this.jwtEncoder = jwtEncoder;
    }

    /**
     * @description: 签发 Access Token
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: sub 使用用户 ID 字符串，权限码放入 permissions claim 供资源服务器转换
     */
    public String issueAccessToken(CurrentUserDTO currentUser) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plusSeconds(authProperties.getAccessTokenSeconds());
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(authProperties.getIssuer())
                .subject(String.valueOf(currentUser.getId()))
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .id(UUID.randomUUID().toString())
                .claim("username", currentUser.getUsername())
                .claim("roles", currentUser.getRoles())
                .claim("permissions", currentUser.getPermissions())
                .claim("dataScope", currentUser.getDataScope())
                .build();
        try {
            return jwtEncoder.encode(JwtEncoderParameters.from(
                    JwsHeader.with(MacAlgorithm.HS256).build(),
                    claims
            )).getTokenValue();
        } catch (RuntimeException exception) {
            throw new ApplicationException("签发访问令牌失败", exception);
        }
    }

    /**
     * @description: 获取 Access Token 有效秒数
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: Controller 返回给前端用于刷新调度
     */
    public Long accessTokenSeconds() {
        return authProperties.getAccessTokenSeconds();
    }
}
