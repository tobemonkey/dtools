package com.dtools.tools.service;

import com.dtools.tools.model.dto.ToolHealthDTO;

/**
 * @description: 工具应用服务接口，向 Controller 暴露工具平台基础能力
 * @author: yesterday'jam
 * @date: 2026/06/08
 * @注意: Controller 只依赖该门面，不直接调用 Mapper 或 BizService
 */
public interface ToolService {

    /**
     * @description: 获取工具模块健康状态
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: 当前用于框架联通验证，不代表真实工具执行能力
     */
    ToolHealthDTO getHealth();
}
