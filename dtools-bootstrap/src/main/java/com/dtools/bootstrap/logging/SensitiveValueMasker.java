package com.dtools.bootstrap.logging;

import com.dtools.common.log.SensitiveStrategy;
import org.springframework.stereotype.Component;

/**
 * @description: 敏感字段值脱敏器，负责将原始字段值转换为可写入日志的安全展示值
 * @author: yesterday'jam
 * @date: 2026/06/09
 * @注意: 只处理已声明脱敏注解的字段，不根据字段名猜测敏感语义
 */
@Component
public class SensitiveValueMasker {

    private static final String HIDDEN_VALUE = "[HIDDEN]";

    private static final String TOKEN_VALUE = "[TOKEN]";

    private static final String MASK_VALUE = "******";

    private static final int DEFAULT_TEXT_LIMIT = 128;

    /**
     * @description: 根据策略脱敏字段值
     * @author: yesterday'jam
     * @date: 2026/06/09
     * @注意: null 值保持 null，避免改变请求参数结构
     */
    public String mask(String rawValue, SensitiveStrategy strategy) {
        if (rawValue == null) {
            return null;
        }
        if (strategy == null) {
            return rawValue;
        }
        return switch (strategy) {
            case HIDDEN -> HIDDEN_VALUE;
            case MASK -> MASK_VALUE;
            case MOBILE -> maskMobile(rawValue);
            case EMAIL -> maskEmail(rawValue);
            case TOKEN -> TOKEN_VALUE;
            case TEXT_LIMIT -> limitText(rawValue, DEFAULT_TEXT_LIMIT);
        };
    }

    private String maskMobile(String rawValue) {
        if (rawValue.length() < 7) {
            return MASK_VALUE;
        }
        return rawValue.substring(0, 3) + "****" + rawValue.substring(rawValue.length() - 4);
    }

    private String maskEmail(String rawValue) {
        int atIndex = rawValue.indexOf('@');
        if (atIndex <= 0) {
            return MASK_VALUE;
        }
        return rawValue.charAt(0) + "***" + rawValue.substring(atIndex);
    }

    private String limitText(String rawValue, int maxLength) {
        if (rawValue.length() <= maxLength) {
            return rawValue;
        }
        return rawValue.substring(0, maxLength) + "...";
    }
}

