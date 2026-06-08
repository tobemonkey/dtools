package com.dtools.common.util;

/**
 * @description: 字符串基础工具，提供项目内无业务含义的轻量判断
 * @author: yesterday'jam
 * @date: 2026/06/08
 * @注意: 不在该类放置具体业务格式化逻辑
 */
public final class StringUtils {

    private StringUtils() {
    }

    /**
     * @description: 判断字符串是否为空白
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: null、空字符串、纯空白都视为空白
     */
    public static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
