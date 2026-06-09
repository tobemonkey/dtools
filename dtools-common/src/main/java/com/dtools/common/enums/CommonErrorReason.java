package com.dtools.common.enums;

/**
 * @description: 公共模块异常原因枚举，收敛跨模块通用错误文案和响应码映射
 * @author: yesterday'jam
 * @date: 2026/06/09
 * @注意: 业务模块专属错误应在对应模块定义枚举，不得污染 common 模块边界
 */
public enum CommonErrorReason implements ErrorReason {

    /**
     * 未知响应码。
     */
    UNKNOWN_RESPONSE_CODE(ResponseCode.APPLICATION_ERROR, "未知响应码: %s"),

    /**
     * 参数类型转换失败。
     */
    PARAM_TYPE_MISMATCH(ResponseCode.PARAM_ERROR, "参数类型错误: %s"),

    /**
     * JSON 请求体无法解析。
     */
    REQUEST_BODY_INVALID(ResponseCode.PARAM_ERROR, "请求体格式错误"),

    /**
     * 应用业务处理失败，对客户端隐藏内部细节。
     */
    APPLICATION_PROCESS_FAILED(ResponseCode.APPLICATION_ERROR, "业务处理异常，请稍后重试"),

    /**
     * 系统内部异常，对客户端隐藏内部细节。
     */
    SYSTEM_PROCESS_FAILED(ResponseCode.SYSTEM_ERROR, "系统异常，请稍后重试");

    private final ResponseCode responseCode;

    private final String message;

    CommonErrorReason(ResponseCode responseCode, String message) {
        this.responseCode = responseCode;
        this.message = message;
    }

    /**
     * @description: 获取异常原因对应的统一响应码
     * @author: yesterday'jam
     * @date: 2026/06/09
     * @注意: 调用方应根据 ResponseCode.code 判断协议结果
     */
    @Override
    public ResponseCode getResponseCode() {
        return responseCode;
    }

    /**
     * @description: 获取受控异常文案
     * @author: yesterday'jam
     * @date: 2026/06/09
     * @注意: 文案用于接口展示或日志摘要，不作为协议判断条件
     */
    @Override
    public String getMessage() {
        return message;
    }
}
