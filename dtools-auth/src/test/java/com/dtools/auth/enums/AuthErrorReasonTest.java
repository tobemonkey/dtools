package com.dtools.auth.enums;

import com.dtools.common.enums.ResponseCode;
import com.dtools.common.exception.AuthenticationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @description: 认证模块异常原因枚举测试，验证认证失败文案和响应码统一收敛
 * @author: yesterday'jam
 * @date: 2026/06/09
 * @注意: 认证模块新增可预期失败时应先补充 AuthErrorReason
 */
class AuthErrorReasonTest {

    /**
     * @description: 验证登录失败异常原因使用未认证响应码
     * @author: yesterday'jam
     * @date: 2026/06/09
     * @注意: 账号不存在和密码错误必须保持统一文案
     */
    @Test
    void loginFailedReasonShouldUseUnauthorizedCode() {
        assertThat(AuthErrorReason.LOGIN_FAILED.getResponseCode()).isEqualTo(ResponseCode.UNAUTHORIZED);
        assertThat(AuthErrorReason.LOGIN_FAILED.getMessage()).isEqualTo("用户名或密码错误");
    }

    /**
     * @description: 验证认证异常可携带认证模块异常原因
     * @author: yesterday'jam
     * @date: 2026/06/09
     * @注意: 调用方只根据响应码处理，不依赖枚举名称
     */
    @Test
    void authenticationExceptionShouldCarryAuthReason() {
        AuthenticationException exception = new AuthenticationException(AuthErrorReason.ACCOUNT_DISABLED);

        assertThat(exception.getResponseCode()).isEqualTo(ResponseCode.UNAUTHORIZED);
        assertThat(exception.getErrorReason()).isEqualTo(AuthErrorReason.ACCOUNT_DISABLED);
        assertThat(exception.getRawMessage()).isEqualTo("账号已禁用");
    }

    /**
     * @description: 验证未知枚举 code 文案通过受控模板格式化
     * @author: yesterday'jam
     * @date: 2026/06/09
     * @注意: 动态 code 只能作为模板参数进入异常文案
     */
    @Test
    void unknownCodeReasonShouldFormatMessage() {
        assertThat(AuthErrorReason.UNKNOWN_ROLE_CODE.formatMessage("guest"))
                .isEqualTo("未知角色编码: guest");
    }
}
