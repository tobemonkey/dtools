package com.dtools.tools.service.impl;

import com.dtools.tools.model.dto.ToolHealthDTO;
import com.dtools.tools.service.ToolService;
import org.springframework.stereotype.Service;

/**
 * @description: 工具应用服务实现，负责工具模块对外用例编排
 * @author: yesterday'jam
 * @date: 2026/06/08
 * @注意: 当前仅提供框架健康检查，真实业务规则后续下沉到 BizService
 */
@Service
public class ToolServiceImpl implements ToolService {

    /**
     * @description: 获取工具模块健康状态
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: 用于验证 bootstrap -> tools -> common 依赖链路
     */
    @Override
    public ToolHealthDTO getHealth() {
        return new ToolHealthDTO("UP", "dtools-tools ready");
    }
}
