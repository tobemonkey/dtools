package com.dtools.bootstrap.monitor.service;

import com.dtools.bootstrap.monitor.model.ApiMonitorErrorSummaryDTO;
import com.dtools.bootstrap.monitor.model.ApiMonitorInterfaceDetailDTO;
import com.dtools.bootstrap.monitor.model.ApiMonitorInterfaceSummaryDTO;
import com.dtools.bootstrap.monitor.model.ApiMonitorOverviewDTO;
import com.dtools.bootstrap.monitor.model.ApiMonitorQuery;
import com.dtools.bootstrap.monitor.model.ApiMonitorSlowEndpointDTO;

import java.util.List;

/**
 * @description: 接口调用统计应用服务，面向监控页面提供查询用例
 * @author: yesterday'jam
 * @date: 2026/06/10
 * @注意: 仅查询 api_request_log 主账本，不修改日志数据
 */
public interface ApiMonitorService {

    /**
     * @description: 查询接口调用统计概览
     * @author: yesterday'jam
     * @date: 2026/06/10
     * @注意: 无数据时返回零值和空列表，避免前端额外处理 null
     */
    ApiMonitorOverviewDTO getOverview(ApiMonitorQuery query);

    /**
     * @description: 查询慢接口列表
     * @author: yesterday'jam
     * @date: 2026/06/10
     * @注意: 按 95 分位耗时和最大耗时倒序返回
     */
    List<ApiMonitorSlowEndpointDTO> listSlowEndpoints(ApiMonitorQuery query);

    /**
     * @description: 查询接口错误统计汇总
     * @author: yesterday'jam
     * @date: 2026/06/10
     * @注意: 错误判定必须与请求日志文档保持一致
     */
    ApiMonitorErrorSummaryDTO getErrors(ApiMonitorQuery query);

    /**
     * @description: 查询接口维度统计列表
     * @author: yesterday'jam
     * @date: 2026/06/10
     * @注意: keyword 同时匹配 HTTP 方法和接口路径
     */
    List<ApiMonitorInterfaceSummaryDTO> listInterfaces(ApiMonitorQuery query);

    /**
     * @description: 查询单接口调用统计详情
     * @author: yesterday'jam
     * @date: 2026/06/10
     * @注意: method 和 uri 共同定位接口，缺失时返回空详情
     */
    ApiMonitorInterfaceDetailDTO getInterfaceDetail(ApiMonitorQuery query);
}
