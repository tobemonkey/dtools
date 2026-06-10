package com.dtools.bootstrap.controller;

import com.dtools.bootstrap.monitor.model.ApiMonitorErrorSummaryDTO;
import com.dtools.bootstrap.monitor.model.ApiMonitorInterfaceDetailDTO;
import com.dtools.bootstrap.monitor.model.ApiMonitorInterfaceSummaryDTO;
import com.dtools.bootstrap.monitor.model.ApiMonitorOverviewDTO;
import com.dtools.bootstrap.monitor.model.ApiMonitorQuery;
import com.dtools.bootstrap.monitor.model.ApiMonitorSlowEndpointDTO;
import com.dtools.bootstrap.monitor.service.ApiMonitorService;
import com.dtools.common.response.ApiResponse;
import com.dtools.common.trace.TraceContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @description: 接口调用统计控制器，提供监控页面后端查询 API
 * @author: yesterday'jam
 * @date: 2026/06/10
 * @注意: 该控制器位于 /api/** 受保护安全链下，调用方必须携带有效 Bearer Token
 */
@RestController
@RequestMapping("/api/monitor/api")
public class ApiMonitorController {

    private final ApiMonitorService apiMonitorService;

    public ApiMonitorController(ApiMonitorService apiMonitorService) {
        this.apiMonitorService = apiMonitorService;
    }

    /**
     * @description: 查询接口调用统计概览
     * @author: yesterday'jam
     * @date: 2026/06/10
     * @注意: 支持 startTime 和 endTime 时间范围过滤
     */
    @GetMapping("/overview")
    public ApiResponse<ApiMonitorOverviewDTO> overview(@ModelAttribute ApiMonitorQuery query) {
        return ApiResponse.success(apiMonitorService.getOverview(query), TraceContext.getTraceId());
    }

    /**
     * @description: 查询慢接口排行
     * @author: yesterday'jam
     * @date: 2026/06/10
     * @注意: 支持 startTime 和 endTime 时间范围过滤
     */
    @GetMapping("/slow")
    public ApiResponse<List<ApiMonitorSlowEndpointDTO>> slow(@ModelAttribute ApiMonitorQuery query) {
        return ApiResponse.success(apiMonitorService.listSlowEndpoints(query), TraceContext.getTraceId());
    }

    /**
     * @description: 查询接口错误统计汇总
     * @author: yesterday'jam
     * @date: 2026/06/10
     * @注意: 错误判定与 api_request_log 文档保持一致
     */
    @GetMapping("/errors")
    public ApiResponse<ApiMonitorErrorSummaryDTO> errors(@ModelAttribute ApiMonitorQuery query) {
        return ApiResponse.success(apiMonitorService.getErrors(query), TraceContext.getTraceId());
    }

    /**
     * @description: 查询接口维度统计列表
     * @author: yesterday'jam
     * @date: 2026/06/10
     * @注意: 支持 keyword 同时匹配 HTTP 方法和接口路径
     */
    @GetMapping("/interfaces")
    public ApiResponse<List<ApiMonitorInterfaceSummaryDTO>> interfaces(@ModelAttribute ApiMonitorQuery query) {
        return ApiResponse.success(apiMonitorService.listInterfaces(query), TraceContext.getTraceId());
    }

    /**
     * @description: 查询单接口调用详情
     * @author: yesterday'jam
     * @date: 2026/06/10
     * @注意: method 和 uri 共同定位接口，缺失时返回空详情
     */
    @GetMapping("/interfaces/detail")
    public ApiResponse<ApiMonitorInterfaceDetailDTO> interfaceDetail(@ModelAttribute ApiMonitorQuery query) {
        return ApiResponse.success(apiMonitorService.getInterfaceDetail(query), TraceContext.getTraceId());
    }
}
