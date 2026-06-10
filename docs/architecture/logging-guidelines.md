# 日志系统规范

## 目标

dtools 日志系统用于支撑本地开发、桌面端排障、Web 部署排障和关键操作追踪。日志能力必须服务诊断，不得反向影响业务主流程。

## 日志分层

| 日志类型 | 文件 | 用途 |
| --- | --- | --- |
| 应用日志 | `application.log` | 应用启动、关键业务事件、异常和运行状态 |
| 请求详情日志 | `request-detail.log` | 可配置记录请求参数诊断信息 |
| 接口 DB 请求日志 | `api_request_log` | 记录接口耗时、状态、响应码、失败摘要、异常堆栈和失败参数快照 |
| 审计记录 | 数据库审计表或后续 `audit.log` | 登录、退出、权限变更、工具执行等关键操作 |

## TraceID

- 每个 HTTP 请求必须具备 traceId。
- traceId 必须写入响应头 `X-Trace-Id`。
- traceId 必须写入 SLF4J MDC，日志格式应输出 traceId。
- 请求结束必须清理 ThreadLocal 和 MDC，避免线程复用串线。

## 日志级别

- `info`：关键成功事件，例如登录成功、退出、Token 刷新、工具执行成功、配置变更。
- `warn`：可预期失败，例如参数错误、认证失败、权限不足、业务规则拒绝、审计写入失败。
- `error`：不可预期系统失败，例如未捕获异常、数据库异常、关键依赖不可用、数据一致性破坏。
- `debug`：开发期诊断细节，生产默认关闭。

## 请求详情日志

请求详情日志是诊断能力，不属于业务主流程。

原则：

- 支持通过 `application.yml`、环境变量或 JVM 启动参数打开关闭。
- 支持配置是否记录 query 和 body。
- 采用异步处理，主请求线程只做轻量快照和投递。
- 队列满、脱敏失败、序列化失败或写入失败时不得影响请求结果。
- 请求体超过配置长度时必须截断。
- 第一阶段只记录请求参数，不记录响应体。
- 文件上传、二进制请求和非文本请求不记录 body 明细。

## 接口 DB 请求日志

接口 DB 请求日志用于把每次 API 请求的运行快照写入 MySQL 主账本，便于后续查询、统计和排障。

原则：

- 默认开启，为接口监控页面提供统计数据来源。
- 支持通过 `application.yml`、环境变量或 JVM 启动参数打开关闭；未建表环境可临时关闭。
- 采用异步写入，队列满或写库失败不得影响请求结果。
- 每个 API 请求写入一条 `api_request_log`。
- 成功请求只记录 traceId、用户、方法、路径、HTTP 状态、`ApiResponse.code` 和耗时。
- 失败请求额外记录 `exception_type`、`exception_message`、按条件截断后的 `exception_stack` 和 `request_params_on_error`。
- `request_params_on_error` 合并 query 和 body，使用 JSON 格式保存。
- 异常诊断模式下，`HIDDEN` 注解字段继续隐藏，其余注解字段保留原值，便于定位失败原因。

失败判定：

- `response_code = 0`：成功。
- `response_code != 0`：失败。
- `response_code is null` 且 `http_status >= 400`：失败。

完整异常栈记录条件：

- 未捕获异常。
- `SystemException`。
- HTTP 5xx。
- 数据库异常。
- JSON 解析、参数绑定或类型转换异常。

业务可预期失败可以只记录异常类型和摘要，不强制记录完整堆栈。

## 脱敏规范

请求详情日志只识别 `@LogSensitive` 注解，不做字段名兜底脱敏。

新增 DTO、Command、Query 时，开发者必须主动判断字段是否敏感，并选择合适策略：

| 策略 | 行为 | 适用字段 |
| --- | --- | --- |
| `HIDDEN` | 输出 `[HIDDEN]` | refresh token、密钥、凭证 |
| `MASK` | 输出 `******` | 密码、验证码 |
| `MOBILE` | 保留前三后四 | 手机号 |
| `EMAIL` | 保留邮箱域名和首字母 | 邮箱 |
| `TOKEN` | 输出 `[TOKEN]` | access token、临时 token |
| `TEXT_LIMIT` | 截断长文本 | 大段文本、备注 |

未加注解的字段按普通字段记录。代码评审必须关注高风险字段是否已声明脱敏注解。

## 配置建议

```yaml
dtools:
  log:
    api-db:
      enabled: true
      async-enabled: true
      queue-capacity: 10000
      max-request-cache-length: 20000
    request-detail:
      enabled: false
      async-enabled: true
      include-query: true
      include-body: false
      max-body-length: 2000
      queue-capacity: 10000
      retention-days: 14
      total-size-cap: 1GB
```

JVM 启动参数示例：

```bash
-Ddtools.log.request-detail.enabled=true
-Ddtools.log.request-detail.include-body=true
-Ddtools.log.request-detail.retention-days=7
-Ddtools.log.api-db.enabled=false
```
