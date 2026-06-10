package com.dtools.bootstrap.monitor.service.impl;

import com.dtools.bootstrap.mapper.ApiRequestLogMapper;
import com.dtools.bootstrap.monitor.model.ApiMonitorErrorSummaryDTO;
import com.dtools.bootstrap.monitor.model.ApiMonitorInterfaceDetailDTO;
import com.dtools.bootstrap.monitor.model.ApiMonitorInterfaceSummaryDTO;
import com.dtools.bootstrap.monitor.model.ApiMonitorOverviewDTO;
import com.dtools.bootstrap.monitor.model.ApiMonitorQuery;
import com.dtools.bootstrap.monitor.model.ApiMonitorSlowEndpointDTO;
import com.dtools.bootstrap.monitor.service.ApiMonitorService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * @description: 接口调用统计应用服务实现，负责统计查询编排和空数据兜底
 * @author: yesterday'jam
 * @date: 2026/06/10
 * @注意: 复杂聚合由 Mapper 面向用例查询，Service 不直接拼 SQL
 */
@Service
public class ApiMonitorServiceImpl implements ApiMonitorService {

    private final ApiRequestLogMapper apiRequestLogMapper;

    public ApiMonitorServiceImpl(ApiRequestLogMapper apiRequestLogMapper) {
        this.apiRequestLogMapper = apiRequestLogMapper;
    }

    /**
     * @description: 查询接口调用统计概览，并补充分位值、状态分布和趋势数据
     * @author: yesterday'jam
     * @date: 2026/06/10
     * @注意: Mapper 返回空数据时统一兜底为零值 DTO，前端无需处理 null
     */
    @Override
    public ApiMonitorOverviewDTO getOverview(ApiMonitorQuery query) {
        ApiMonitorOverviewDTO overview = apiRequestLogMapper.selectOverview(query);
        if (overview == null) {
            overview = new ApiMonitorOverviewDTO();
        }
        normalizeOverview(overview);
        overview.setErrorRate(rate(overview.getErrorCount(), overview.getTotalCount()));
        overview.setT95CostMs(defaultLong(apiRequestLogMapper.selectOverviewT95CostMs(query)));
        overview.setT99CostMs(defaultLong(apiRequestLogMapper.selectOverviewT99CostMs(query)));
        overview.setStatusSegments(nonNullList(apiRequestLogMapper.selectOverviewStatusSegments(query)));
        overview.setTrendPoints(nonNullList(apiRequestLogMapper.selectOverviewTrendPoints(query)));
        return overview;
    }

    /**
     * @description: 查询慢接口排行并计算超时率
     * @author: yesterday'jam
     * @date: 2026/06/10
     * @注意: timeoutCount 仅作为内部计算字段，不直接暴露给前端
     */
    @Override
    public List<ApiMonitorSlowEndpointDTO> listSlowEndpoints(ApiMonitorQuery query) {
        List<ApiMonitorSlowEndpointDTO> endpoints = nonNullList(apiRequestLogMapper.selectSlowEndpoints(query));
        endpoints.forEach(endpoint -> {
            normalizeSlowEndpoint(endpoint);
            endpoint.setTimeoutRate(rate(endpoint.getTimeoutCount(), endpoint.getRequestCount()));
        });
        return endpoints;
    }

    /**
     * @description: 查询异常请求汇总，包括异常类型、状态码和最近异常列表
     * @author: yesterday'jam
     * @date: 2026/06/10
     * @注意: 最近异常只返回摘要字段，不返回异常堆栈和失败参数快照
     */
    @Override
    public ApiMonitorErrorSummaryDTO getErrors(ApiMonitorQuery query) {
        ApiMonitorErrorSummaryDTO summary = new ApiMonitorErrorSummaryDTO();
        summary.setErrorTypeItems(nonNullList(apiRequestLogMapper.selectErrorTypeItems(query)));
        summary.setStatusItems(nonNullList(apiRequestLogMapper.selectErrorStatusItems(query)));
        summary.setRecentErrors(nonNullList(apiRequestLogMapper.selectRecentErrors(query)));
        return summary;
    }

    /**
     * @description: 查询接口维度统计列表
     * @author: yesterday'jam
     * @date: 2026/06/10
     * @注意: keyword 匹配逻辑在 Mapper 中收敛，Service 只负责空列表兜底
     */
    @Override
    public List<ApiMonitorInterfaceSummaryDTO> listInterfaces(ApiMonitorQuery query) {
        List<ApiMonitorInterfaceSummaryDTO> interfaces = nonNullList(apiRequestLogMapper.selectInterfaceSummaries(query));
        interfaces.forEach(this::normalizeInterfaceSummary);
        return interfaces;
    }

    /**
     * @description: 查询单接口详情并组装趋势、状态、异常类型、耗时桶和最近异常
     * @author: yesterday'jam
     * @date: 2026/06/10
     * @注意: method 和 uri 无匹配数据时返回带零值 summary 的空详情
     */
    @Override
    public ApiMonitorInterfaceDetailDTO getInterfaceDetail(ApiMonitorQuery query) {
        ApiMonitorInterfaceDetailDTO detail = new ApiMonitorInterfaceDetailDTO();
        ApiMonitorInterfaceSummaryDTO summary = apiRequestLogMapper.selectInterfaceDetailSummary(query);
        if (summary == null) {
            summary = new ApiMonitorInterfaceSummaryDTO();
            summary.setMethod(query.getMethod());
            summary.setUri(query.getUri());
        }
        normalizeInterfaceSummary(summary);
        detail.setSummary(summary);
        detail.setTrendPoints(nonNullList(apiRequestLogMapper.selectInterfaceDetailTrendPoints(query)));
        detail.setStatusItems(nonNullList(apiRequestLogMapper.selectInterfaceDetailStatusItems(query)));
        detail.setErrorTypeItems(nonNullList(apiRequestLogMapper.selectInterfaceDetailErrorTypeItems(query)));
        detail.setLatencyBuckets(nonNullList(apiRequestLogMapper.selectInterfaceDetailLatencyBuckets(query)));
        detail.setRecentErrors(nonNullList(apiRequestLogMapper.selectInterfaceDetailRecentErrors(query)));
        return detail;
    }

    private void normalizeOverview(ApiMonitorOverviewDTO overview) {
        overview.setTotalCount(defaultLong(overview.getTotalCount()));
        overview.setSuccessCount(defaultLong(overview.getSuccessCount()));
        overview.setErrorCount(defaultLong(overview.getErrorCount()));
        overview.setAvgCostMs(defaultLong(overview.getAvgCostMs()));
        overview.setMaxCostMs(defaultLong(overview.getMaxCostMs()));
        overview.setSlowestCostMs(defaultLong(overview.getSlowestCostMs()));
    }

    private void normalizeSlowEndpoint(ApiMonitorSlowEndpointDTO endpoint) {
        endpoint.setRequestCount(defaultLong(endpoint.getRequestCount()));
        endpoint.setAvgCostMs(defaultLong(endpoint.getAvgCostMs()));
        endpoint.setT90CostMs(defaultLong(endpoint.getT90CostMs()));
        endpoint.setT95CostMs(defaultLong(endpoint.getT95CostMs()));
        endpoint.setT99CostMs(defaultLong(endpoint.getT99CostMs()));
        endpoint.setMaxCostMs(defaultLong(endpoint.getMaxCostMs()));
        endpoint.setTimeoutCount(defaultLong(endpoint.getTimeoutCount()));
    }

    private void normalizeInterfaceSummary(ApiMonitorInterfaceSummaryDTO summary) {
        summary.setRequestCount(defaultLong(summary.getRequestCount()));
        summary.setErrorCount(defaultLong(summary.getErrorCount()));
        summary.setAvgCostMs(defaultLong(summary.getAvgCostMs()));
        summary.setT95CostMs(defaultLong(summary.getT95CostMs()));
        summary.setT99CostMs(defaultLong(summary.getT99CostMs()));
        summary.setMaxCostMs(defaultLong(summary.getMaxCostMs()));
        summary.setErrorRate(rate(summary.getErrorCount(), summary.getRequestCount()));
    }

    private BigDecimal rate(Long numerator, Long denominator) {
        long safeDenominator = defaultLong(denominator);
        if (safeDenominator == 0L) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(defaultLong(numerator))
                .divide(BigDecimal.valueOf(safeDenominator), 4, RoundingMode.HALF_UP);
    }

    private Long defaultLong(Long value) {
        return value == null ? 0L : value;
    }

    private <T> List<T> nonNullList(List<T> list) {
        return list == null ? new ArrayList<>() : list;
    }
}
