package com.dtools.auth.mapper;

import com.dtools.auth.model.entity.AuthRefreshTokenEntity;
import com.dtools.auth.model.entity.AuthUserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @description: 鉴权 Mapper，承载登录、角色、刷新凭证和登录审计的阶段 1 SQL
 * @author: yesterday'jam
 * @date: 2026/06/08
 * @注意: Mapper 方法按登录用例设计，不预置无关通用 CRUD
 */
@Mapper
public interface AuthMapper {

    /**
     * @description: 根据用户名查询登录用户
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: 用于登录校验，不返回给接口调用方
     */
    AuthUserEntity findUserByUsername(@Param("username") String username);

    /**
     * @description: 根据用户 ID 查询登录用户
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: 用于 refresh token 换新和 me 兜底查询
     */
    AuthUserEntity findUserById(@Param("userId") Long userId);

    /**
     * @description: 查询用户拥有的角色 code 集合
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: 角色 code 必须来自 AuthRole.getCode()
     */
    List<String> findRoleCodesByUserId(@Param("userId") Long userId);

    /**
     * @description: 保存新的刷新凭证哈希
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: tokenHash 是唯一落库凭证，禁止保存明文 refresh token
     */
    int insertRefreshToken(AuthRefreshTokenEntity refreshToken);

    /**
     * @description: 根据刷新凭证哈希查询可用 Token 记录
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: SQL 已排除被撤销或已过期记录
     */
    AuthRefreshTokenEntity findActiveRefreshTokenByHash(@Param("tokenHash") String tokenHash,
                                                        @Param("now") LocalDateTime now);

    /**
     * @description: 撤销刷新凭证并记录轮换后的新哈希
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: refresh token 每次刷新必须轮换，降低泄漏窗口
     */
    int revokeRefreshToken(@Param("tokenHash") String tokenHash,
                           @Param("revokedAt") LocalDateTime revokedAt,
                           @Param("now") LocalDateTime now,
                           @Param("replacedByHash") String replacedByHash);

    /**
     * @description: 记录登录审计事件
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: 失败场景不要记录密码或 refresh token 明文
     */
    int insertLoginAudit(@Param("userId") Long userId,
                         @Param("username") String username,
                         @Param("success") Boolean success,
                         @Param("failureReason") String failureReason,
                         @Param("traceId") String traceId,
                         @Param("createdAt") LocalDateTime createdAt);
}
