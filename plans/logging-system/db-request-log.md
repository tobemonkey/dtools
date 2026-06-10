# 接口 DB 请求日志切片

## 背景

文件请求详情日志适合本地排障，但接口耗时统计、响应状态、失败原因和异常请求参数需要进入 MySQL 主账本，便于后续查询、审计和 UI 展示。

## 本阶段目标

- 每个 API 请求写入一条 `api_request_log`。
- 记录 traceId、用户、方法、路径、HTTP 状态、`ApiResponse.code`、耗时。
- 请求失败时记录失败摘要、异常类型、按策略截断后的异常栈。
- 请求失败时记录参数快照 `request_params_on_error`，合并 query 和 body。
- `request_params_on_error` 中 `HIDDEN` 注解字段保持隐藏，其余字段在异常诊断模式下保留原值。
- DB 日志异步写入，失败不影响接口主流程。

## 失败判定

- `response_code = 0`：成功，不记录异常参数快照和异常堆栈。
- `response_code != 0`：失败，记录失败摘要，按异常分类决定是否记录完整堆栈。
- `response_code is null` 且 `http_status >= 400`：失败，记录失败摘要，按异常分类决定是否记录完整堆栈。

## 异常栈记录建议

记录完整堆栈：

- 未捕获异常。
- `SystemException`。
- HTTP 5xx。
- 数据库异常。
- JSON 解析、参数绑定或类型转换异常。

只记录 `exception_type` 和 `exception_message`：

- `BizException`。
- `AuthenticationException`。
- `AccessDeniedException`。
- 参数校验失败。
- 明确业务规则拒绝。

## 验收命令

- `mvn -q test`
- `mvn -q -DskipTests compile`

