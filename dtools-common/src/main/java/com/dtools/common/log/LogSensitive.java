package com.dtools.common.log;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @description: 请求详情日志敏感字段注解，声明字段在日志中的脱敏方式
 * @author: yesterday'jam
 * @date: 2026/06/09
 * @注意: 请求详情日志只识别该注解，不按字段名做运行时兜底脱敏
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface LogSensitive {

    /**
     * @description: 字段脱敏策略
     * @author: yesterday'jam
     * @date: 2026/06/09
     * @注意: 高风险凭证优先使用 HIDDEN 或 TOKEN
     */
    SensitiveStrategy strategy() default SensitiveStrategy.HIDDEN;
}

