package com.dtools.common.exception;

import com.dtools.common.enums.ErrorReason;
import com.dtools.common.enums.ResponseCode;

import java.time.LocalDateTime;

/**
 * @description: 受控业务异常，携带统一响应码供全局异常处理转换
 * @author: yesterday'jam
 * @date: 2026/06/08
 * @注意: 业务可预期失败应抛出该异常，不应直接返回散乱错误结构
 */
public class BizException extends RuntimeException {

    private final ResponseCode responseCode;

    private final ErrorReason errorReason;

    private final LocalDateTime occurredAt;

    public BizException(ResponseCode responseCode, String message) {
        super(message);
        this.responseCode = responseCode;
        this.errorReason = null;
        this.occurredAt = LocalDateTime.now();
    }

    public BizException(ResponseCode responseCode, String message, Throwable cause) {
        super(message, cause);
        this.responseCode = responseCode;
        this.errorReason = null;
        this.occurredAt = LocalDateTime.now();
    }

    public BizException(ErrorReason errorReason) {
        super(errorReason.getMessage());
        this.responseCode = errorReason.getResponseCode();
        this.errorReason = errorReason;
        this.occurredAt = LocalDateTime.now();
    }

    public BizException(ErrorReason errorReason, Object... messageArgs) {
        super(errorReason.formatMessage(messageArgs));
        this.responseCode = errorReason.getResponseCode();
        this.errorReason = errorReason;
        this.occurredAt = LocalDateTime.now();
    }

    public BizException(ErrorReason errorReason, Throwable cause) {
        super(errorReason.getMessage(), cause);
        this.responseCode = errorReason.getResponseCode();
        this.errorReason = errorReason;
        this.occurredAt = LocalDateTime.now();
    }

    /**
     * @description: 获取业务异常对应响应码
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: 全局异常处理会使用该响应码构造接口返回
     */
    public ResponseCode getResponseCode() {
        return responseCode;
    }

    /**
     * @description: 获取异常对应的受控原因枚举
     * @author: yesterday'jam
     * @date: 2026/06/09
     * @注意: 兼容旧构造器时可能为空，新业务应优先使用异常原因枚举构造
     */
    public ErrorReason getErrorReason() {
        return errorReason;
    }

    /**
     * @description: 获取未拼接响应码的原始异常说明
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: 仅客户端可见异常可直接返回该信息
     */
    public String getRawMessage() {
        return super.getMessage();
    }

    /**
     * @description: 获取异常发生时间
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: 该时间主要用于日志和审计，不作为业务排序依据
     */
    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    /**
     * @description: 获取带响应码的日志消息
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: 日志消息可包含内部细节，接口返回需由异常处理器决定是否暴露
     */
    @Override
    public String getMessage() {
        return "[ResponseCode: " + responseCode.getCode() + "] " + super.getMessage();
    }
}
