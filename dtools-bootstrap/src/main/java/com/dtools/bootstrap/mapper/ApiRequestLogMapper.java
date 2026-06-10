package com.dtools.bootstrap.mapper;

import com.dtools.bootstrap.logging.event.ApiRequestLogEvent;
import com.dtools.bootstrap.monitor.model.ApiMonitorErrorTypeItemDTO;
import com.dtools.bootstrap.monitor.model.ApiMonitorInterfaceSummaryDTO;
import com.dtools.bootstrap.monitor.model.ApiMonitorLatencyBucketDTO;
import com.dtools.bootstrap.monitor.model.ApiMonitorOverviewDTO;
import com.dtools.bootstrap.monitor.model.ApiMonitorQuery;
import com.dtools.bootstrap.monitor.model.ApiMonitorRecentErrorDTO;
import com.dtools.bootstrap.monitor.model.ApiMonitorSlowEndpointDTO;
import com.dtools.bootstrap.monitor.model.ApiMonitorStatusItemDTO;
import com.dtools.bootstrap.monitor.model.ApiMonitorTrendPointDTO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @description: 接口请求日志 Mapper，负责将 API 请求运行快照写入 MySQL 主账本
 * @author: yesterday'jam
 * @date: 2026/06/10
 * @注意: 仅服务当前请求日志写入用例，不预置通用 CRUD
 */
@Mapper
public interface ApiRequestLogMapper {

    /**
     * @description: 插入接口请求日志
     * @author: yesterday'jam
     * @date: 2026/06/10
     * @注意: 写入失败必须由调用方吞掉，不能影响接口主流程
     */
    @Insert("""
            insert into api_request_log (
                trace_id, user_id, username, method, uri, client_ip, user_agent,
                http_status, response_code, cost_ms, exception_type, exception_message,
                exception_stack, request_params_on_error
            ) values (
                #{traceId}, #{userId}, #{username}, #{method}, #{uri}, #{clientIp}, #{userAgent},
                #{httpStatus}, #{responseCode}, #{costMs}, #{exceptionType}, #{exceptionMessage},
                #{exceptionStack}, #{requestParamsOnError}
            )
            """)
    int insert(ApiRequestLogEvent event);

    /**
     * @description: 查询接口调用概览基础聚合数据
     * @author: yesterday'jam
     * @date: 2026/06/10
     * @注意: 错误判定与 api_request_log 文档保持一致
     */
    @Select("""
            <script>
            select
                agg.totalCount,
                agg.successCount,
                agg.errorCount,
                agg.avgCostMs,
                agg.maxCostMs,
                slow.method as slowestMethod,
                slow.uri as slowestUri,
                coalesce(slow.cost_ms, 0) as slowestCostMs
            from (
                select
                    count(*) as totalCount,
                    coalesce(sum(case when not (response_code != 0 or (response_code is null and http_status &gt;= 400)) then 1 else 0 end), 0) as successCount,
                    coalesce(sum(case when response_code != 0 or (response_code is null and http_status &gt;= 400) then 1 else 0 end), 0) as errorCount,
                    coalesce(round(avg(cost_ms)), 0) as avgCostMs,
                    coalesce(max(cost_ms), 0) as maxCostMs
                from api_request_log
                where 1 = 1
                <if test="startTime != null">and created_at &gt;= #{startTime}</if>
                <if test="endTime != null">and created_at &lt;= #{endTime}</if>
            ) agg
            left join (
                select method, uri, cost_ms
                from api_request_log
                where 1 = 1
                <if test="startTime != null">and created_at &gt;= #{startTime}</if>
                <if test="endTime != null">and created_at &lt;= #{endTime}</if>
                order by cost_ms desc, created_at desc
                limit 1
            ) slow on true
            </script>
            """)
    ApiMonitorOverviewDTO selectOverview(ApiMonitorQuery query);

    /**
     * @description: 查询概览 95 分位耗时
     * @author: yesterday'jam
     * @date: 2026/06/10
     * @注意: 使用窗口函数按 cost_ms 排序估算分位值
     */
    @Select("""
            <script>
            select coalesce(max(case when rn = ceiling(total_count * 0.95) then cost_ms end), 0)
            from (
                select cost_ms, row_number() over(order by cost_ms) as rn, count(*) over() as total_count
                from api_request_log
                where 1 = 1
                <if test="startTime != null">and created_at &gt;= #{startTime}</if>
                <if test="endTime != null">and created_at &lt;= #{endTime}</if>
            ) ranked
            </script>
            """)
    Long selectOverviewT95CostMs(ApiMonitorQuery query);

    /**
     * @description: 查询概览 99 分位耗时
     * @author: yesterday'jam
     * @date: 2026/06/10
     * @注意: 使用窗口函数按 cost_ms 排序估算分位值
     */
    @Select("""
            <script>
            select coalesce(max(case when rn = ceiling(total_count * 0.99) then cost_ms end), 0)
            from (
                select cost_ms, row_number() over(order by cost_ms) as rn, count(*) over() as total_count
                from api_request_log
                where 1 = 1
                <if test="startTime != null">and created_at &gt;= #{startTime}</if>
                <if test="endTime != null">and created_at &lt;= #{endTime}</if>
            ) ranked
            </script>
            """)
    Long selectOverviewT99CostMs(ApiMonitorQuery query);

    /**
     * @description: 查询概览 HTTP 状态分布
     * @author: yesterday'jam
     * @date: 2026/06/10
     * @注意: 返回具体 HTTP 状态码分布
     */
    @Select("""
            <script>
            select cast(http_status as char) as status, count(*) as count
            from api_request_log
            where 1 = 1
            <if test="startTime != null">and created_at &gt;= #{startTime}</if>
            <if test="endTime != null">and created_at &lt;= #{endTime}</if>
            group by http_status
            order by count desc, http_status asc
            </script>
            """)
    List<ApiMonitorStatusItemDTO> selectOverviewStatusSegments(ApiMonitorQuery query);

    /**
     * @description: 查询概览小时级趋势
     * @author: yesterday'jam
     * @date: 2026/06/10
     * @注意: 按 created_at 所在小时聚合
     */
    @Select("""
            <script>
            select
                str_to_date(date_format(created_at, '%Y-%m-%d %H:00:00'), '%Y-%m-%d %H:%i:%s') as bucketTime,
                count(*) as totalCount,
                coalesce(sum(case when response_code != 0 or (response_code is null and http_status &gt;= 400) then 1 else 0 end), 0) as errorCount,
                coalesce(round(avg(cost_ms)), 0) as avgCostMs
            from api_request_log
            where 1 = 1
            <if test="startTime != null">and created_at &gt;= #{startTime}</if>
            <if test="endTime != null">and created_at &lt;= #{endTime}</if>
            group by bucketTime
            order by bucketTime asc
            </script>
            """)
    List<ApiMonitorTrendPointDTO> selectOverviewTrendPoints(ApiMonitorQuery query);

    /**
     * @description: 查询慢接口排行
     * @author: yesterday'jam
     * @date: 2026/06/10
     * @注意: 超时请求暂按 HTTP 408、异常类型包含 Timeout 或耗时超过 30 秒识别
     */
    @Select("""
            <script>
            with ranked as (
                select
                    method,
                    uri,
                    cost_ms,
                    case when http_status = 408 or exception_type like '%Timeout%' or cost_ms &gt;= 30000 then 1 else 0 end as timeout_flag,
                    row_number() over(partition by method, uri order by cost_ms) as rn,
                    count(*) over(partition by method, uri) as endpoint_count
                from api_request_log
                where 1 = 1
                <if test="startTime != null">and created_at &gt;= #{startTime}</if>
                <if test="endTime != null">and created_at &lt;= #{endTime}</if>
            )
            select
                method,
                uri,
                count(*) as requestCount,
                coalesce(round(avg(cost_ms)), 0) as avgCostMs,
                coalesce(max(case when rn = ceiling(endpoint_count * 0.90) then cost_ms end), 0) as t90CostMs,
                coalesce(max(case when rn = ceiling(endpoint_count * 0.95) then cost_ms end), 0) as t95CostMs,
                coalesce(max(case when rn = ceiling(endpoint_count * 0.99) then cost_ms end), 0) as t99CostMs,
                coalesce(max(cost_ms), 0) as maxCostMs,
                coalesce(sum(timeout_flag), 0) as timeoutCount
            from ranked
            group by method, uri
            order by t95CostMs desc, maxCostMs desc
            limit 20
            </script>
            """)
    List<ApiMonitorSlowEndpointDTO> selectSlowEndpoints(ApiMonitorQuery query);

    /**
     * @description: 查询错误类型分布
     * @author: yesterday'jam
     * @date: 2026/06/10
     * @注意: 仅统计符合错误判定的请求
     */
    @Select("""
            <script>
            select coalesce(exception_type, 'UNKNOWN') as errorType, count(*) as count
            from api_request_log
            where (response_code != 0 or (response_code is null and http_status &gt;= 400))
            <if test="startTime != null">and created_at &gt;= #{startTime}</if>
            <if test="endTime != null">and created_at &lt;= #{endTime}</if>
            group by errorType
            order by count desc
            limit 20
            </script>
            """)
    List<ApiMonitorErrorTypeItemDTO> selectErrorTypeItems(ApiMonitorQuery query);

    /**
     * @description: 查询错误 HTTP 状态分布
     * @author: yesterday'jam
     * @date: 2026/06/10
     * @注意: 仅统计符合错误判定的请求
     */
    @Select("""
            <script>
            select cast(http_status as char) as status, count(*) as count
            from api_request_log
            where (response_code != 0 or (response_code is null and http_status &gt;= 400))
            <if test="startTime != null">and created_at &gt;= #{startTime}</if>
            <if test="endTime != null">and created_at &lt;= #{endTime}</if>
            group by http_status
            order by count desc, http_status asc
            </script>
            """)
    List<ApiMonitorStatusItemDTO> selectErrorStatusItems(ApiMonitorQuery query);

    /**
     * @description: 查询最近错误请求列表
     * @author: yesterday'jam
     * @date: 2026/06/10
     * @注意: 不返回异常堆栈和失败请求参数快照
     */
    @Select("""
            <script>
            select
                trace_id as traceId,
                method,
                uri,
                http_status as httpStatus,
                response_code as responseCode,
                cost_ms as costMs,
                exception_type as exceptionType,
                exception_message as exceptionMessage,
                created_at as createdAt
            from api_request_log
            where (response_code != 0 or (response_code is null and http_status &gt;= 400))
            <if test="startTime != null">and created_at &gt;= #{startTime}</if>
            <if test="endTime != null">and created_at &lt;= #{endTime}</if>
            order by created_at desc
            limit 20
            </script>
            """)
    List<ApiMonitorRecentErrorDTO> selectRecentErrors(ApiMonitorQuery query);

    /**
     * @description: 查询接口维度统计列表
     * @author: yesterday'jam
     * @date: 2026/06/10
     * @注意: keyword 同时匹配 HTTP 方法和接口路径
     */
    @Select("""
            <script>
            with ranked as (
                select
                    method,
                    uri,
                    trace_id,
                    created_at,
                    cost_ms,
                    case when response_code != 0 or (response_code is null and http_status &gt;= 400) then 1 else 0 end as error_flag,
                    row_number() over(partition by method, uri order by cost_ms) as rn,
                    count(*) over(partition by method, uri) as endpoint_count
                from api_request_log
                where 1 = 1
                <if test="startTime != null">and created_at &gt;= #{startTime}</if>
                <if test="endTime != null">and created_at &lt;= #{endTime}</if>
                <if test="keyword != null and keyword != ''">
                    and (method like concat('%', #{keyword}, '%') or uri like concat('%', #{keyword}, '%'))
                </if>
            )
            select
                method,
                uri,
                count(*) as requestCount,
                coalesce(sum(error_flag), 0) as errorCount,
                coalesce(round(avg(cost_ms)), 0) as avgCostMs,
                coalesce(max(case when rn = ceiling(endpoint_count * 0.95) then cost_ms end), 0) as t95CostMs,
                coalesce(max(case when rn = ceiling(endpoint_count * 0.99) then cost_ms end), 0) as t99CostMs,
                coalesce(max(cost_ms), 0) as maxCostMs,
                substring_index(group_concat(trace_id order by created_at desc separator ','), ',', 1) as latestTraceId
            from ranked
            group by method, uri
            order by requestCount desc, t95CostMs desc
            limit 100
            </script>
            """)
    List<ApiMonitorInterfaceSummaryDTO> selectInterfaceSummaries(ApiMonitorQuery query);

    /**
     * @description: 查询单接口详情摘要
     * @author: yesterday'jam
     * @date: 2026/06/10
     * @注意: method 和 uri 必须共同参与过滤
     */
    @Select("""
            <script>
            with ranked as (
                select
                    method,
                    uri,
                    trace_id,
                    created_at,
                    cost_ms,
                    case when response_code != 0 or (response_code is null and http_status &gt;= 400) then 1 else 0 end as error_flag,
                    row_number() over(order by cost_ms) as rn,
                    count(*) over() as total_count
                from api_request_log
                where method = #{method}
                  and uri = #{uri}
                <if test="startTime != null">and created_at &gt;= #{startTime}</if>
                <if test="endTime != null">and created_at &lt;= #{endTime}</if>
            )
            select
                method,
                uri,
                count(*) as requestCount,
                coalesce(sum(error_flag), 0) as errorCount,
                coalesce(round(avg(cost_ms)), 0) as avgCostMs,
                coalesce(max(case when rn = ceiling(total_count * 0.95) then cost_ms end), 0) as t95CostMs,
                coalesce(max(case when rn = ceiling(total_count * 0.99) then cost_ms end), 0) as t99CostMs,
                coalesce(max(cost_ms), 0) as maxCostMs,
                substring_index(group_concat(trace_id order by created_at desc separator ','), ',', 1) as latestTraceId
            from ranked
            group by method, uri
            </script>
            """)
    ApiMonitorInterfaceSummaryDTO selectInterfaceDetailSummary(ApiMonitorQuery query);

    /**
     * @description: 查询单接口详情小时级趋势
     * @author: yesterday'jam
     * @date: 2026/06/10
     * @注意: 仅统计 method 和 uri 指定接口
     */
    @Select("""
            <script>
            select
                str_to_date(date_format(created_at, '%Y-%m-%d %H:00:00'), '%Y-%m-%d %H:%i:%s') as bucketTime,
                count(*) as totalCount,
                coalesce(sum(case when response_code != 0 or (response_code is null and http_status &gt;= 400) then 1 else 0 end), 0) as errorCount,
                coalesce(round(avg(cost_ms)), 0) as avgCostMs
            from api_request_log
            where method = #{method}
              and uri = #{uri}
            <if test="startTime != null">and created_at &gt;= #{startTime}</if>
            <if test="endTime != null">and created_at &lt;= #{endTime}</if>
            group by bucketTime
            order by bucketTime asc
            </script>
            """)
    List<ApiMonitorTrendPointDTO> selectInterfaceDetailTrendPoints(ApiMonitorQuery query);

    /**
     * @description: 查询单接口详情 HTTP 状态分布
     * @author: yesterday'jam
     * @date: 2026/06/10
     * @注意: 仅统计 method 和 uri 指定接口
     */
    @Select("""
            <script>
            select cast(http_status as char) as status, count(*) as count
            from api_request_log
            where method = #{method}
              and uri = #{uri}
            <if test="startTime != null">and created_at &gt;= #{startTime}</if>
            <if test="endTime != null">and created_at &lt;= #{endTime}</if>
            group by http_status
            order by count desc, http_status asc
            </script>
            """)
    List<ApiMonitorStatusItemDTO> selectInterfaceDetailStatusItems(ApiMonitorQuery query);

    /**
     * @description: 查询单接口详情错误类型分布
     * @author: yesterday'jam
     * @date: 2026/06/10
     * @注意: 仅统计 method 和 uri 指定接口的错误请求
     */
    @Select("""
            <script>
            select coalesce(exception_type, 'UNKNOWN') as errorType, count(*) as count
            from api_request_log
            where method = #{method}
              and uri = #{uri}
              and (response_code != 0 or (response_code is null and http_status &gt;= 400))
            <if test="startTime != null">and created_at &gt;= #{startTime}</if>
            <if test="endTime != null">and created_at &lt;= #{endTime}</if>
            group by errorType
            order by count desc
            limit 20
            </script>
            """)
    List<ApiMonitorErrorTypeItemDTO> selectInterfaceDetailErrorTypeItems(ApiMonitorQuery query);

    /**
     * @description: 查询单接口详情耗时桶分布
     * @author: yesterday'jam
     * @date: 2026/06/10
     * @注意: 耗时桶边界固定，便于前端稳定展示
     */
    @Select("""
            <script>
            select bucketName, count(*) as count
            from (
                select
                    case
                        when cost_ms &lt; 100 then '0-100ms'
                        when cost_ms &lt; 500 then '100-500ms'
                        when cost_ms &lt; 1000 then '500ms-1s'
                        when cost_ms &lt; 3000 then '1-3s'
                        else '3s+'
                    end as bucketName,
                    case
                        when cost_ms &lt; 100 then 1
                        when cost_ms &lt; 500 then 2
                        when cost_ms &lt; 1000 then 3
                        when cost_ms &lt; 3000 then 4
                        else 5
                    end as bucketOrder
                from api_request_log
                where method = #{method}
                  and uri = #{uri}
                <if test="startTime != null">and created_at &gt;= #{startTime}</if>
                <if test="endTime != null">and created_at &lt;= #{endTime}</if>
            ) buckets
            group by bucketName, bucketOrder
            order by bucketOrder asc
            </script>
            """)
    List<ApiMonitorLatencyBucketDTO> selectInterfaceDetailLatencyBuckets(ApiMonitorQuery query);

    /**
     * @description: 查询单接口详情最近错误列表
     * @author: yesterday'jam
     * @date: 2026/06/10
     * @注意: 不返回异常堆栈和失败请求参数快照
     */
    @Select("""
            <script>
            select
                trace_id as traceId,
                method,
                uri,
                http_status as httpStatus,
                response_code as responseCode,
                cost_ms as costMs,
                exception_type as exceptionType,
                exception_message as exceptionMessage,
                created_at as createdAt
            from api_request_log
            where method = #{method}
              and uri = #{uri}
              and (response_code != 0 or (response_code is null and http_status &gt;= 400))
            <if test="startTime != null">and created_at &gt;= #{startTime}</if>
            <if test="endTime != null">and created_at &lt;= #{endTime}</if>
            order by created_at desc
            limit 20
            </script>
            """)
    List<ApiMonitorRecentErrorDTO> selectInterfaceDetailRecentErrors(ApiMonitorQuery query);
}
