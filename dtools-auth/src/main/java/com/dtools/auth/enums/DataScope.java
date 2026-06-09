package com.dtools.auth.enums;

import com.dtools.common.exception.ApplicationException;

import java.util.Arrays;

/**
 * @description: 数据范围枚举，控制用户可访问的数据归属边界
 * @author: yesterday'jam
 * @date: 2026/06/08
 * @注意: 数据范围必须在后端 Service 或 Mapper 收敛，前端只做体验展示
 */
public enum DataScope {

    /**
     * 只能访问自己创建或归属自己的数据。
     */
    SELF("self", "本人数据"),

    /**
     * 可以访问全部用户数据。
     */
    ALL("all", "全部数据");

    private final String code;

    private final String desc;

    DataScope(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * @description: 获取稳定数据范围 code
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: 该值用于接口、JWT claims 和服务层数据范围判断
     */
    public String getCode() {
        return code;
    }

    /**
     * @description: 获取数据范围中文说明
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: desc 只用于展示说明
     */
    public String getDesc() {
        return desc;
    }

    /**
     * @description: 根据稳定数据范围 code 反查枚举
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: 未知数据范围会抛出受控异常，避免扩大数据访问边界
     */
    public static DataScope fromCode(String code) {
        return Arrays.stream(values())
                .filter(scope -> scope.code.equals(code))
                .findFirst()
                .orElseThrow(() -> new ApplicationException(AuthErrorReason.UNKNOWN_DATA_SCOPE, code));
    }
}
