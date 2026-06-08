package com.dtools.tools.executor;

/**
 * @description: 工具执行器接口，定义工具执行能力的统一入口
 * @author: yesterday'jam
 * @date: 2026/06/08
 * @注意: 新增真实工具时先补充工具定义，再实现对应执行器
 */
public interface ToolExecutor {

    /**
     * @description: 判断当前执行器是否支持指定工具
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: toolId 必须使用工具定义中的稳定标识
     */
    boolean supports(String toolId);
}
