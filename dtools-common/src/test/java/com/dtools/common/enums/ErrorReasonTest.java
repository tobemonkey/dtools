package com.dtools.common.enums;

import com.dtools.common.exception.BizException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @description: 异常原因枚举测试，验证业务异常文案和响应码统一收敛
 * @author: yesterday'jam
 * @date: 2026/06/09
 * @注意: 新增可预期业务异常时应优先补充 ErrorReason 枚举
 */
class ErrorReasonTest {

    /**
     * @description: 验证异常原因携带响应码和默认客户端文案
     * @author: yesterday'jam
     * @date: 2026/06/09
     * @注意: 响应码仍由 ResponseCode 维护，ErrorReason 只表达具体失败原因
     */
    @Test
    void errorReasonShouldCarryResponseCodeAndMessage() {
        assertThat(CommonErrorReason.REQUEST_BODY_INVALID.getResponseCode()).isEqualTo(ResponseCode.PARAM_ERROR);
        assertThat(CommonErrorReason.REQUEST_BODY_INVALID.getMessage()).isEqualTo("请求体格式错误");
    }

    /**
     * @description: 验证业务异常可直接通过异常原因构造
     * @author: yesterday'jam
     * @date: 2026/06/09
     * @注意: 业务代码不应再分散传入裸字符串
     */
    @Test
    void bizExceptionShouldUseErrorReason() {
        BizException exception = new BizException(CommonErrorReason.REQUEST_BODY_INVALID);

        assertThat(exception.getResponseCode()).isEqualTo(ResponseCode.PARAM_ERROR);
        assertThat(exception.getErrorReason()).isEqualTo(CommonErrorReason.REQUEST_BODY_INVALID);
        assertThat(exception.getRawMessage()).isEqualTo("请求体格式错误");
    }

    /**
     * @description: 验证异常原因支持受控参数格式化
     * @author: yesterday'jam
     * @date: 2026/06/09
     * @注意: 动态信息只能通过枚举模板补充，不直接散落静态文本
     */
    @Test
    void errorReasonShouldFormatControlledMessage() {
        assertThat(CommonErrorReason.UNKNOWN_RESPONSE_CODE.formatMessage(999))
                .isEqualTo("未知响应码: 999");
    }
}
