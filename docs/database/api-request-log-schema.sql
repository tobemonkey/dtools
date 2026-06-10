-- dtools 接口请求日志表
-- @author: yesterday'jam
-- @date: 2026/06/10
-- @注意: 本脚本只覆盖接口请求主日志单表方案，后续数据量上升后可将异常栈和失败参数垂直拆分到详情表。

create table if not exists api_request_log
(
    id                      bigint unsigned not null auto_increment comment '请求日志主键 ID',
    trace_id                varchar(64)     not null comment '请求链路追踪 ID',
    user_id                 bigint unsigned null comment '当前登录用户 ID，未登录时为空',
    username                varchar(64)     null comment '当前登录用户名快照',
    method                  varchar(16)     not null comment 'HTTP 方法',
    uri                     varchar(512)    not null comment '请求路径，不包含 query string',
    client_ip               varchar(64)     null comment '客户端 IP',
    user_agent              varchar(512)    null comment 'User-Agent 摘要',
    http_status             int             not null comment 'HTTP 响应状态码',
    response_code           int             null comment 'ApiResponse.code，0 表示成功，非 0 表示失败，未解析为空',
    cost_ms                 bigint          not null comment '接口耗时，单位毫秒',
    exception_type          varchar(255)    null comment '异常类名或失败类型，例如 java.lang.NullPointerException',
    exception_message       varchar(2048)   null comment '异常或失败原因摘要，用于列表快速排查',
    exception_stack         mediumtext      null comment '异常堆栈，按配置和异常类型记录',
    request_params_on_error longtext        null comment '失败请求参数快照，JSON 格式，包含 query 和 body；HIDDEN 字段仍隐藏，其余字段按异常诊断策略记录',
    created_at              datetime        not null default current_timestamp comment '创建时间',
    primary key (id),
    unique key uk_api_request_log_trace_id (trace_id),
    key idx_api_request_log_created_at (created_at),
    key idx_api_request_log_uri_created_at (uri, created_at),
    key idx_api_request_log_response_created_at (response_code, created_at),
    key idx_api_request_log_status_created_at (http_status, created_at),
    key idx_api_request_log_user_created_at (user_id, created_at),
    key idx_api_request_log_cost_created_at (cost_ms, created_at)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_0900_ai_ci
  comment = '接口请求日志表';

