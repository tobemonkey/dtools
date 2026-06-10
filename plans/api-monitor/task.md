# 接口调用统计页面任务说明

## 背景

当前项目已经具备 `api_request_log` 主账本表设计和请求日志写入链路。用户需要从首页点击接口监控图标进入统计页面，查看接口耗时、慢接口、异常请求和接口明细。

## 本阶段目标

- 新增后端接口调用统计查询 API，数据来源限定为 `api_request_log`。
- 新增前端接口监控页面，视觉风格延续首页 bento / widget 个人工具空间。
- 页面包含四个 tab：总体统计、慢接口、异常请求、接口明细。
- 总体统计支持起止时间、默认范围 `5min / 1h / 24h` 和刷新。
- 慢接口展示专业耗时指标，包括 `avg / t90 / t95 / t99 / max / timeoutRate`。
- 异常请求展示异常类型、状态码、最近异常请求和 TraceID。
- 接口明细支持搜索接口，展示聚合统计；点击接口后展示更完整的接口详情图表。

## 非目标

- 不新增告警、通知、定时任务或自动诊断。
- 不新增正式数据库表；本阶段只查询已有 `api_request_log`。
- 不实现分页详情钻取到单条异常堆栈编辑或重放。
- 不引入第三方图表库，先使用 Vue + CSS / SVG 完成轻量图表。

## 验收命令

- `mvn -q -DskipTests compile`
- `mvn -q test`
- `npm --prefix frontend run typecheck`
- `npm --prefix frontend run build`

