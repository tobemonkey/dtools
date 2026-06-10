package com.dtools.common.log;

/**
 * @description: 日志脱敏策略枚举，定义请求详情日志中敏感字段的输出方式
 * @author: yesterday'jam
 * @date: 2026/06/09
 * @注意: code 用于后续配置、审计和外部协议扩展，禁止直接依赖 enum.name()
 */
public enum SensitiveStrategy {

    /**
     * 完全隐藏字段值。
     */
    HIDDEN(1, "完全隐藏"),

    /**
     * 使用固定星号替代字段值。
     */
    MASK(2, "固定掩码"),

    /**
     * 手机号保留前三位和后四位。
     */
    MOBILE(3, "手机号脱敏"),

    /**
     * 邮箱保留首字母和域名。
     */
    EMAIL(4, "邮箱脱敏"),

    /**
     * Token 类凭证统一隐藏。
     */
    TOKEN(5, "Token 脱敏"),

    /**
     * 长文本按最大长度截断。
     */
    TEXT_LIMIT(6, "长文本截断");

    private final Integer code;

    private final String desc;

    SensitiveStrategy(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    /**
     * @description: 通过稳定 code 反查脱敏策略
     * @author: yesterday'jam
     * @date: 2026/06/09
     * @注意: 找不到时返回 null，调用方应转换为明确业务错误或受控默认策略
     */
    public static SensitiveStrategy fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (SensitiveStrategy strategy : values()) {
            if (strategy.code.equals(code)) {
                return strategy;
            }
        }
        return null;
    }
}

