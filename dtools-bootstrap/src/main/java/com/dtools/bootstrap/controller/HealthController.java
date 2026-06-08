package com.dtools.bootstrap.controller;

import com.dtools.common.response.ApiResponse;
import com.dtools.common.trace.TraceContext;
import com.dtools.tools.model.dto.ToolHealthDTO;
import com.dtools.tools.service.ToolService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description: 健康检查接口，验证前后端分离 API 服务和工具模块链路
 * @author: yesterday'jam
 * @date: 2026/06/08
 * @注意: 该接口只用于框架验收，不代表业务工具能力已完成
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final ToolService toolService;

    public HealthController(ToolService toolService) {
        this.toolService = toolService;
    }

    /**
     * @description: 查询后端健康状态
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: 返回统一 ApiResponse，方便前端验证 REST API 调用链路
     */
    @GetMapping
    public ApiResponse<ToolHealthDTO> health() {
        return ApiResponse.success(toolService.getHealth(), TraceContext.getTraceId());
    }
}
