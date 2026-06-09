-- dtools 权限模块阶段 1 建表脚本
-- @author: yesterday'jam
-- @date: 2026/06/08
-- @注意: 本脚本只覆盖应用内账号登录闭环，不引入动态角色、权限、组织、租户或迁移工具。

create table if not exists sys_user
(
    id            bigint unsigned not null auto_increment comment '用户主键 ID',
    username      varchar(64)     not null comment '登录用户名，系统内唯一',
    password_hash varchar(255)    not null comment 'BCrypt 密码哈希，禁止保存明文密码',
    display_name  varchar(64)     not null comment '前端展示名称',
    status        tinyint         not null comment '用户状态：1 启用，2 禁用',
    created_at    datetime        not null default current_timestamp comment '创建时间',
    updated_at    datetime        not null default current_timestamp on update current_timestamp comment '更新时间',
    primary key (id),
    unique key uk_sys_user_username (username)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_0900_ai_ci
  comment = '系统用户表';

create table if not exists sys_user_role
(
    id         bigint unsigned not null auto_increment comment '用户角色关系主键 ID',
    user_id    bigint unsigned not null comment '用户 ID',
    role_code  varchar(32)     not null comment '角色稳定 code：owner/member/viewer',
    created_at datetime        not null default current_timestamp comment '创建时间',
    primary key (id),
    unique key uk_sys_user_role_user_role (user_id, role_code),
    key idx_sys_user_role_user_id (user_id)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_0900_ai_ci
  comment = '用户角色关系表';

create table if not exists auth_refresh_token
(
    id               bigint unsigned not null auto_increment comment '刷新凭证主键 ID',
    user_id          bigint unsigned not null comment '用户 ID',
    token_hash       char(64)        not null comment 'Refresh Token SHA-256 哈希，禁止保存明文',
    expires_at       datetime        not null comment '过期时间',
    revoked_at       datetime        null comment '撤销时间，非空表示不可再使用',
    replaced_by_hash char(64)        null comment '轮换后的新 Refresh Token 哈希',
    created_at       datetime        not null default current_timestamp comment '创建时间',
    primary key (id),
    unique key uk_auth_refresh_token_hash (token_hash),
    key idx_auth_refresh_token_user_id (user_id),
    key idx_auth_refresh_token_expires_at (expires_at)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_0900_ai_ci
  comment = '刷新凭证表';

create table if not exists auth_login_audit
(
    id             bigint unsigned not null auto_increment comment '登录审计主键 ID',
    user_id        bigint unsigned null comment '用户 ID，账号不存在时为空',
    username       varchar(64)     not null comment '登录用户名',
    success        tinyint(1)      not null comment '是否登录成功',
    failure_reason varchar(255)    null comment '失败原因摘要，不包含密码或凭证明文',
    trace_id       varchar(64)     null comment '请求链路追踪 ID',
    created_at     datetime        not null default current_timestamp comment '创建时间',
    primary key (id),
    key idx_auth_login_audit_user_id (user_id),
    key idx_auth_login_audit_username (username),
    key idx_auth_login_audit_created_at (created_at)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_0900_ai_ci
  comment = '登录审计表';

-- owner 初始化示例：
-- 1. 使用后端 PasswordEncoder 或临时测试代码生成 BCrypt 密码哈希，禁止在脚本中硬编码真实密码。
-- 2. 将下面的 $2a$... 示例替换为你自己生成的哈希后再执行。
--
-- insert into sys_user (username, password_hash, display_name, status)
-- values ('owner', '$2a$10$replace-with-generated-bcrypt-hash', 'Owner', 1);
--
-- insert into sys_user_role (user_id, role_code)
-- select id, 'owner'
-- from sys_user
-- where username = 'owner';
