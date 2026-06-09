package com.dtools.common.enums;

/**
 * @description: 受控异常原因接口，约束业务异常必须携带稳定响应码和可控文案
 * @author: yesterday'jam
 * @date: 2026/06/09
 * @注意: 各业务模块可定义自己的异常原因枚举，但必须实现该接口
 */
public interface ErrorReason {

    /**
     * @description: 获取异常原因对应的统一响应码
     * @author: yesterday'jam
     * @date: 2026/06/09
     * @注意: 接口返回 code 仍以 ResponseCode 为唯一稳定来源
     */
    ResponseCode getResponseCode();

    /**
     * @description: 获取异常原因默认文案
     * @author: yesterday'jam
     * @date: 2026/06/09
     * @注意: 客户端可见异常不得包含内部敏感信息
     */
    String getMessage();

    /**
     * @description: 使用受控模板格式化异常文案
     * @author: yesterday'jam
     * @date: 2026/06/09
     * @注意: 仅用于补充字段名、code 等必要定位信息，不拼接敏感上下文
     */
    default String formatMessage(Object... args) {
        return String.format(getMessage(), args);
    }
}
