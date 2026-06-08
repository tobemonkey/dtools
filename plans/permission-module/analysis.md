# 权限模块阶段 1 分析

## 背景

本阶段以 `docs/architecture/auth-framework-research.md` 作为冻结设计输入，目标是落地应用内账号登录闭环。当前仓库已有统一响应、异常和 TraceID，但尚未接入 Spring Security，也没有 `dtools-auth` 模块、登录接口或前端登录态。

## 当前状态

- 后端模块：`dtools-common`、`dtools-tools`、`dtools-bootstrap`。
- 前端结构：`api / types / stores / views / components / utils` 分层已开始形成，但认证相关目录缺失。
- 已有响应码：`SUCCESS`、`PARAM_ERROR`、`CLIENT_ERROR`、`APPLICATION_ERROR`、`UNAUTHORIZED`、`FORBIDDEN`、`SYSTEM_ERROR`。
- 已有异常转换：`AuthenticationException` 返回 401，`AccessDeniedException` 返回 403。
- 基线验证：`mvn -q -DskipTests compile` 通过；`npm --prefix frontend run typecheck` 通过。

## 阶段 1 范围

- 新增 `dtools-auth` 后端模块。
- 新增静态角色、权限码、数据范围枚举和角色到权限映射。
- 新增登录、刷新、退出、当前用户查询 Service 能力。
- 新增 Spring Security 配置，公开 `/api/health`、`/api/auth/login`、`/api/auth/refresh`，保护其他 `/api/**`。
- 新增统一 401 / 403 JSON 响应处理。
- 新增正式 schema SQL 文档，包含 `sys_user`、`sys_user_role`、`auth_refresh_token`、`auth_login_audit`。
- 新增前端登录页、403 页、`authApi`、`authStore`、auth 类型、路由守卫和 HTTP Token 注入。
- 工作台接入当前用户展示、退出入口和 `tool:execute` 前端体验层控制。

## 本阶段不做

- 不引入 Keycloak、Spring Authorization Server、Sa-Token。
- 不创建动态角色、权限、组织、租户、部门、ACL 或权限管理后台。
- 不引入 Flyway / Liquibase；当前只提供正式 schema SQL 文档，迁移工具在数据库治理阶段单独处理。
- 不实现历史账本、用户配置、审计后台和 Web 安全加固。
- 不把长期凭证写入普通配置文件；前端第一阶段只保留运行时 access token，refresh token 由后端响应给前端用于开发闭环，并在交付风险中标注为临时策略。

## 关键决策

- 登录失败、Refresh Token 失效统一返回 HTTP 401 + `ApiResponse` 中 `UNAUTHORIZED`。
- 已认证但缺少权限统一返回 HTTP 403 + `FORBIDDEN`。
- Refresh Token 只存哈希，不明文落库。
- 默认健康检查公开；工作台首页 `/` 需要登录。
- 权限码以后端枚举为准，前端只消费 `/api/auth/me` 返回的 `permissions`。
- 后端接口优先使用权限码，不把角色名作为业务放行依据。

## Subagent 分工

- 主 agent：维护范围、计划、接口契约、冲突合并、最终验证和交付说明。
- 后端 explorer：只读盘点后端模块、依赖和安全配置落点，已完成。
- 前端 explorer：只读盘点前端分层、登录态和路由接入点，已完成。
- 后端 worker：负责 `dtools-auth`、Security 配置、Controller、Mapper、schema 文档和后端测试。
- 前端 worker：负责 auth 类型、API、store、路由守卫、登录/403 页面、工作台权限体验和前端验证。
- Review subagent：按切片分别做 spec compliance review 和 code quality review。

## 新会话边界

- 当前会话适合完成“阶段 1 编码实现”。
- 阶段 1 完成并产生代码、测试结果、schema 文档和截图后，建议切换到新会话进入“攻击测试 / QA 阶段”。
- QA 新会话输入清单：根 `AGENTS.md`、`docs/architecture/auth-framework-research.md`、本目录计划文件、实现后的代码、验证命令和截图。
- QA 新会话不要携带产品探索聊天过程或本会话中的中间推理，只读取冻结产物和代码。
