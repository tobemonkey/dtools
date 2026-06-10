package com.dtools.bootstrap.monitor.service;

import com.dtools.bootstrap.mapper.ApiRequestLogMapper;
import com.dtools.bootstrap.monitor.model.ApiMonitorErrorTypeItemDTO;
import com.dtools.bootstrap.monitor.model.ApiMonitorInterfaceDetailDTO;
import com.dtools.bootstrap.monitor.model.ApiMonitorInterfaceSummaryDTO;
import com.dtools.bootstrap.monitor.model.ApiMonitorLatencyBucketDTO;
import com.dtools.bootstrap.monitor.model.ApiMonitorOverviewDTO;
import com.dtools.bootstrap.monitor.model.ApiMonitorQuery;
import com.dtools.bootstrap.monitor.model.ApiMonitorRecentErrorDTO;
import com.dtools.bootstrap.monitor.model.ApiMonitorStatusItemDTO;
import com.dtools.bootstrap.monitor.model.ApiMonitorTrendPointDTO;
import com.dtools.bootstrap.monitor.service.impl.ApiMonitorServiceImpl;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @description: 接口调用统计服务测试，验证统计聚合和详情组装逻辑
 * @author: yesterday'jam
 * @date: 2026/06/10
 * @注意: 仅 mock Mapper 查询结果，Service 负责空数据兜底和接口响应 DTO 组装
 */
class ApiMonitorServiceImplTest {

    /**
     * @description: 验证概览无数据时返回零值和空列表
     * @author: yesterday'jam
     * @date: 2026/06/10
     * @注意: 空数据不能抛异常，前端可直接渲染空态
     */
    @Test
    void getOverviewShouldReturnZeroValuesWhenNoLogExists() {
        ApiRequestLogMapper mapper = mock(ApiRequestLogMapper.class);
        ApiMonitorService service = new ApiMonitorServiceImpl(mapper);
        ApiMonitorQuery query = new ApiMonitorQuery();

        ApiMonitorOverviewDTO overview = service.getOverview(query);

        assertThat(overview.getTotalCount()).isZero();
        assertThat(overview.getSuccessCount()).isZero();
        assertThat(overview.getErrorCount()).isZero();
        assertThat(overview.getErrorRate()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(overview.getAvgCostMs()).isZero();
        assertThat(overview.getT95CostMs()).isZero();
        assertThat(overview.getT99CostMs()).isZero();
        assertThat(overview.getMaxCostMs()).isZero();
        assertThat(overview.getStatusSegments()).isEmpty();
        assertThat(overview.getTrendPoints()).isEmpty();
    }

    /**
     * @description: 验证概览会补充分位值、错误率和状态趋势列表
     * @author: yesterday'jam
     * @date: 2026/06/10
     * @注意: 错误率由 Service 按 errorCount / totalCount 统一计算
     */
    @Test
    void getOverviewShouldAssembleAggregateMetrics() {
        ApiRequestLogMapper mapper = mock(ApiRequestLogMapper.class);
        ApiMonitorService service = new ApiMonitorServiceImpl(mapper);
        ApiMonitorQuery query = new ApiMonitorQuery();
        ApiMonitorOverviewDTO mapperOverview = new ApiMonitorOverviewDTO();
        mapperOverview.setTotalCount(10L);
        mapperOverview.setSuccessCount(7L);
        mapperOverview.setErrorCount(3L);
        mapperOverview.setAvgCostMs(123L);
        mapperOverview.setMaxCostMs(900L);
        mapperOverview.setSlowestMethod("POST");
        mapperOverview.setSlowestUri("/api/tools/run");
        mapperOverview.setSlowestCostMs(900L);
        ApiMonitorStatusItemDTO statusItem = new ApiMonitorStatusItemDTO();
        statusItem.setStatus("500");
        statusItem.setCount(3L);
        ApiMonitorTrendPointDTO trendPoint = new ApiMonitorTrendPointDTO();
        trendPoint.setBucketTime(LocalDateTime.of(2026, 6, 10, 10, 0));
        trendPoint.setTotalCount(10L);
        trendPoint.setErrorCount(3L);
        trendPoint.setAvgCostMs(120L);
        when(mapper.selectOverview(query)).thenReturn(mapperOverview);
        when(mapper.selectOverviewT95CostMs(query)).thenReturn(700L);
        when(mapper.selectOverviewT99CostMs(query)).thenReturn(900L);
        when(mapper.selectOverviewStatusSegments(query)).thenReturn(List.of(statusItem));
        when(mapper.selectOverviewTrendPoints(query)).thenReturn(List.of(trendPoint));

        ApiMonitorOverviewDTO overview = service.getOverview(query);

        assertThat(overview.getErrorRate()).isEqualByComparingTo("0.3000");
        assertThat(overview.getT95CostMs()).isEqualTo(700L);
        assertThat(overview.getT99CostMs()).isEqualTo(900L);
        assertThat(overview.getSlowestMethod()).isEqualTo("POST");
        assertThat(overview.getStatusSegments()).containsExactly(statusItem);
        assertThat(overview.getTrendPoints()).containsExactly(trendPoint);
    }

    /**
     * @description: 验证接口详情会按 method 和 uri 组装摘要、趋势、状态、异常和耗时桶
     * @author: yesterday'jam
     * @date: 2026/06/10
     * @注意: Controller 只透传查询条件，详情聚合边界集中在 Service
     */
    @Test
    void getInterfaceDetailShouldAssembleAllDetailSections() {
        ApiRequestLogMapper mapper = mock(ApiRequestLogMapper.class);
        ApiMonitorService service = new ApiMonitorServiceImpl(mapper);
        ApiMonitorQuery query = new ApiMonitorQuery();
        query.setMethod("GET");
        query.setUri("/api/health");
        ApiMonitorInterfaceSummaryDTO summary = new ApiMonitorInterfaceSummaryDTO();
        summary.setMethod("GET");
        summary.setUri("/api/health");
        summary.setRequestCount(20L);
        summary.setErrorCount(1L);
        ApiMonitorTrendPointDTO trendPoint = new ApiMonitorTrendPointDTO();
        trendPoint.setTotalCount(20L);
        ApiMonitorStatusItemDTO statusItem = new ApiMonitorStatusItemDTO();
        statusItem.setStatus("200");
        statusItem.setCount(19L);
        ApiMonitorErrorTypeItemDTO errorTypeItem = new ApiMonitorErrorTypeItemDTO();
        errorTypeItem.setErrorType("java.lang.IllegalStateException");
        errorTypeItem.setCount(1L);
        ApiMonitorLatencyBucketDTO latencyBucket = new ApiMonitorLatencyBucketDTO();
        latencyBucket.setBucketName("0-100ms");
        latencyBucket.setCount(15L);
        ApiMonitorRecentErrorDTO recentError = new ApiMonitorRecentErrorDTO();
        recentError.setTraceId("trace-error");
        recentError.setMethod("GET");
        recentError.setUri("/api/health");
        when(mapper.selectInterfaceDetailSummary(query)).thenReturn(summary);
        when(mapper.selectInterfaceDetailTrendPoints(query)).thenReturn(List.of(trendPoint));
        when(mapper.selectInterfaceDetailStatusItems(query)).thenReturn(List.of(statusItem));
        when(mapper.selectInterfaceDetailErrorTypeItems(query)).thenReturn(List.of(errorTypeItem));
        when(mapper.selectInterfaceDetailLatencyBuckets(query)).thenReturn(List.of(latencyBucket));
        when(mapper.selectInterfaceDetailRecentErrors(query)).thenReturn(List.of(recentError));

        ApiMonitorInterfaceDetailDTO detail = service.getInterfaceDetail(query);

        assertThat(detail.getSummary().getErrorRate()).isEqualByComparingTo("0.0500");
        assertThat(detail.getTrendPoints()).containsExactly(trendPoint);
        assertThat(detail.getStatusItems()).containsExactly(statusItem);
        assertThat(detail.getErrorTypeItems()).containsExactly(errorTypeItem);
        assertThat(detail.getLatencyBuckets()).containsExactly(latencyBucket);
        assertThat(detail.getRecentErrors()).containsExactly(recentError);
    }
}
