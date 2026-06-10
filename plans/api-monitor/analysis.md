# 接口调用统计页面分析

## 数据来源

正式数据来源为 `api_request_log`，关键字段包括：

- `trace_id`：单次请求追踪。
- `method` / `uri`：接口聚合维度。
- `http_status` / `response_code`：状态与业务响应码统计。
- `cost_ms`：平均耗时、分位值、慢接口排行。
- `exception_type` / `exception_message`：异常类型排行和最近异常摘要。
- `created_at`：时间范围筛选和趋势分桶。

## 后端接口边界

新增受保护接口路径建议为：

- `GET /api/monitor/api/overview`
- `GET /api/monitor/api/slow`
- `GET /api/monitor/api/errors`
- `GET /api/monitor/api/interfaces`
- `GET /api/monitor/api/interfaces/detail`

查询参数统一使用：

- `startTime`：ISO 本地日期时间字符串。
- `endTime`：ISO 本地日期时间字符串。
- `keyword`：接口明细搜索关键字，仅明细接口需要。
- `uri` / `method`：接口详情定位参数。

## 前端页面边界

- 页面路由：`/api-monitor`。
- 首页左侧导航增加“接口监控”入口。
- 页面独立为 `ApiMonitorView.vue`。
- API 封装放入 `src/api/apiMonitorApi.ts`。
- 类型定义放入 `src/types/apiMonitor.ts`。
- 跨 tab 状态和请求动作放入 `src/stores/apiMonitorStore.ts`。
- 可复用 tab、图表和表格组件放入 `src/components/api-monitor/`。

## 风险

- MySQL 分位值计算需要兼容 MySQL 8，不使用数据库方言以外的扩展函数。
- 日志表数据为空时前端必须展示空状态。
- 当前接口统计页面属于受保护页面，必须通过已有 JWT 请求链路访问。

