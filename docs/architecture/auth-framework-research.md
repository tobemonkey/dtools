# dtools 鉴权框架调研

## 1. 文档目的

本文用于评估 dtools 在 macOS 优先、后续 Web 化部署场景下适合采用的鉴权框架，并给出当前阶段推荐方案、模块边界和后续演进路线。

当前结论：后端以 **Spring Security + JWT Resource Server 能力** 作为鉴权底座；第一阶段不引入 Keycloak、Spring Authorization Server 或 Sa-Token 作为主框架。登录、刷新、退出和当前用户查询由 dtools 后端按最小可验收切片实现，接口和 Token 语义对齐 OAuth2 / OIDC 习惯，保留后续迁移到标准身份提供方的空间。

## 2. 项目约束

- 当前优先开发 macOS Tauri 2 客户端，iOS 后续评估，Web 后续会更加重要。
- 前端是 Vue 3 + TypeScript + Vite + Pinia，必须通过公开 REST API 调用后端。
- 后端是 Spring Boot 3.5.x + JDK 21 + Maven 多模块 + MyBatis + MySQL 8。
- MySQL 作为可审计主账本，用户、登录审计、刷新凭证等重要状态后续应优先落库。
- 当前阶段不应提前落地过大的用户中心、组织、租户、第三方登录或完整 OAuth2 授权服务器。
- 鉴权必须能同时适配桌面 App 和未来 Web，不应只围绕本地单机做一次性方案。

## 3. 评估维度

| 维度 | 关注点 |
| --- | --- |
| Spring Boot 适配 | 是否适配 Spring Boot 3.x、Jakarta、JDK 21 生态 |
| 前后端分离 | 是否适合 REST API、SPA、Tauri 客户端调用 |
| Web 演进 | 是否方便后续支持 Web 登录、Cookie、安全响应头、CSRF 策略 |
| 标准化 | 是否靠近 OAuth2 / OIDC / JWT 等通用协议 |
| 复杂度 | 是否适合个人工具平台第一阶段，不引入过重运维组件 |
| 可替换性 | 后续是否能迁移到外部 IdP、SSO 或独立授权服务 |
| 审计能力 | 是否方便记录 TraceID、登录审计、Token 生命周期和权限判断 |

## 4. 候选方案对比

| 方案 | 优点 | 风险 / 不足 | 结论 |
| --- | --- | --- | --- |
| Spring Security + JWT | Spring Boot 官方生态；适配 Spring Boot 3.x；能保护 REST API；可使用 Resource Server 校验 Bearer Token；后续能对接 Keycloak、Auth0、OIDC Provider | 配置心智成本高；登录发 Token、刷新 Token 和用户体系仍需项目自己设计 | **推荐作为当前主方案** |
| Sa-Token | 中文文档完善；API 简洁；支持 Spring Boot 3.x、前后端分离、权限认证、SSO、OAuth2 等能力；上手快 | 标准化和国际生态弱于 Spring Security；与未来 OIDC Provider、企业 SSO、Spring 官方安全生态衔接不如 Spring Security 自然 | 可作为备选，不作为当前主框架 |
| Keycloak | 成熟 IdP；支持 OIDC、OAuth2、SAML；自带用户、Realm、客户端、管理后台和细粒度权限能力 | 运维和概念复杂度较高；对个人工具 App 第一阶段偏重；本地开发和桌面场景成本更高 | 后续多用户、SSO、第三方登录阶段再评估 |
| Spring Authorization Server | Spring 官方授权服务器框架；支持 OAuth2 / OIDC；可高度定制 | 需要自己运营授权服务器；授权码、客户端、同意页、JWK、Token Endpoint 等复杂度超出当前阶段 | 不在第一阶段引入，可作为后续自建 IdP 方案 |
| Apache Shiro | 认证授权模型清楚；历史项目使用多 | 与 Spring Boot 3 / Spring Security 生态贴合度不足；Web 安全默认能力和 OAuth2 / OIDC 演进不占优 | 不推荐作为新项目主方案 |

## 5. 推荐方案

### 5.1 当前阶段推荐

采用 **Spring Security 作为唯一鉴权底座**，后端自行实现最小登录闭环：

- 用户登录：校验账号和密码，签发短期 Access Token。
- Token 校验：API 请求使用 `Authorization: Bearer <access_token>`。
- Token 刷新：使用长期 Refresh Token 换取新的 Access Token。
- 用户信息：提供 `/api/auth/me` 返回当前登录用户和权限摘要。
- 权限模型：先支持最小角色或权限编码，例如 `admin`、`tool:execute`，不提前做复杂 RBAC 表。

Spring Security 在这里承担：

- 请求认证过滤链。
- 受保护路径和公开路径配置。
- JWT 解析、签名校验和过期校验。
- `Authentication` 上下文承载当前用户身份。
- 方法级或路径级权限扩展能力。

dtools 自己承担：

- 用户表和密码校验。
- Token 签发策略。
- Refresh Token 存储、撤销和轮换。
- 登录审计、TraceID 关联。
- 统一异常和响应码转换。

### 5.2 为什么不直接上 Keycloak

Keycloak 很适合多系统 SSO、第三方登录、组织级身份管理和标准 OIDC Provider 场景，但 dtools 当前是个人工具平台，第一阶段更需要轻量、可控、容易调试的后端内置鉴权。

如果现在直接引入 Keycloak，会提前增加这些成本：

- 本地开发需要额外身份服务。
- 需要维护 Realm、Client、Redirect URI、用户和角色映射。
- macOS Tauri 登录流需要额外处理外部浏览器、回调和深链。
- 当前尚未确认 Web 多用户、组织、第三方登录等需求。

因此 Keycloak 应作为后续升级项，而不是第一阶段默认依赖。

### 5.3 为什么不直接上 Spring Authorization Server

Spring Authorization Server 适合构建 OAuth2 / OIDC 授权服务器。如果 dtools 后续发展成多客户端平台，需要让 Web、macOS、iOS、第三方插件或其他服务统一登录，可以再引入。

当前阶段只需要 dtools 自己保护自己的 API，不需要完整授权码流程、客户端注册、用户同意页、OIDC Discovery 和 JWK 轮换管理。直接引入授权服务器会让第一阶段实现变重。

### 5.4 为什么不优先用 Sa-Token

Sa-Token 的优点是简单、中文生态友好、前后端分离场景上手快，也已经支持 Spring Boot 3.x。它适合快速做业务系统鉴权。

但 dtools 的长期方向包括 Web 化部署、可能的 iOS 客户端、未来外部身份提供方和标准协议接入。Spring Security 与 Spring Boot、OAuth2 Resource Server、OIDC Provider 的衔接更自然，长期可替换性更好。

所以 Sa-Token 可以保留为“快速交付优先”的备选，不作为当前推荐主线。

## 6. 建议模块边界

后续实现鉴权时，建议新增 `dtools-auth` 后端模块，不把鉴权代码堆进 `dtools-bootstrap` 或 `dtools-tools`。

```text
dtools/
├── dtools-common/
│   ├── response/
│   ├── exception/
│   └── trace/
├── dtools-auth/
│   └── src/main/java/com/dtools/auth/
│       ├── model/
│       │   ├── command/
│       │   ├── dto/
│       │   └── entity/
│       ├── service/
│       │   ├── AuthService.java
│       │   └── impl/
│       ├── token/
│       ├── security/
│       ├── mapper/
│       └── config/
├── dtools-tools/
└── dtools-bootstrap/
    └── src/main/java/com/dtools/bootstrap/
        ├── controller/
        │   └── AuthController.java
        ├── config/
        │   └── SecurityConfig.java
        └── advice/
```

建议依赖方向：

```text
dtools-bootstrap -> dtools-auth -> dtools-common
dtools-bootstrap -> dtools-tools -> dtools-common
dtools-auth 不依赖 dtools-tools
dtools-tools 不依赖 dtools-auth
```

边界说明：

- `dtools-auth` 负责认证领域、Token 生命周期、当前用户上下文、登录审计和 Mapper。
- `dtools-bootstrap` 负责暴露 `AuthController`、装配 Spring Security FilterChain、跨域和异常转换。
- `dtools-tools` 不直接依赖鉴权模块。需要当前用户时，由 Controller 或应用服务把 `CurrentUser` 作为入参传入工具用例。
- `dtools-common` 可以沉淀 `UnauthorizedException`、`AccessDeniedException`、统一响应码和 TraceID。

## 7. API 候选契约

当前文档只定义候选契约，正式 DTO 和 Controller 应在鉴权实现阶段再落地。

| 方法 | 路径 | 说明 | 是否公开 |
| --- | --- | --- | --- |
| `POST` | `/api/auth/login` | 账号密码登录，返回 Access Token，设置或返回 Refresh Token | 是 |
| `POST` | `/api/auth/refresh` | 使用 Refresh Token 换取新的 Access Token | 是 |
| `POST` | `/api/auth/logout` | 撤销当前 Refresh Token，清理登录态 | 否 |
| `GET` | `/api/auth/me` | 查询当前用户、角色和权限摘要 | 否 |
| `GET` | `/api/health` | 健康检查 | 是 |

其他 `/api/**` 默认要求认证。

响应仍使用现有 `ApiResponse<T>`，未登录和无权限应落到统一响应码：

- `401 Unauthorized`：未登录、Access Token 缺失、Token 过期或非法。
- `403 Forbidden`：已登录但权限不足。

## 8. Token 与存储策略

### 8.1 Access Token

- 使用短有效期，建议第一阶段 15 到 30 分钟。
- 通过 `Authorization: Bearer <access_token>` 调用后端 API。
- Access Token 不落 MySQL 主账本，只通过签名和过期时间校验。
- Token 中只放稳定且必要的身份摘要，例如 `sub`、`roles`、`permissions`、`iat`、`exp`、`jti`。
- 不把密码、邮箱、昵称、配置等可变资料放进 Token。

### 8.2 Refresh Token

- 使用较长有效期，建议第一阶段 7 到 30 天。
- Refresh Token 必须服务端可撤销，建议只存哈希值，不明文落库。
- 每次刷新后建议轮换 Refresh Token，降低泄漏后的可用窗口。
- 退出登录、修改密码、手动踢下线时撤销 Refresh Token。

### 8.3 Web 存储

Web 版本优先推荐：

- Access Token 放内存，页面刷新后通过 Refresh Token 换新。
- Refresh Token 使用 `HttpOnly`、`Secure`、`SameSite` Cookie。
- 如果采用 Cookie 承载刷新凭证，刷新和退出接口需要认真处理 CSRF 策略。

不建议把长期 Token 放在 `localStorage` 中作为默认方案。

### 8.4 macOS Tauri 存储

macOS 客户端推荐：

- Access Token 放前端运行时内存。
- Refresh Token 存入 macOS Keychain，后续通过 Tauri 插件或自定义命令封装。
- 不把长期凭证写入普通配置文件、日志或可读缓存。

如果第一阶段还未接入 Keychain，可以先在开发环境使用临时存储，但必须在实现说明中标注为临时方案。

## 9. 数据库候选设计

以下只作为后续实现候选，不在当前调研阶段落入正式 schema。

### 9.1 `sys_user`

用于保存登录用户主数据。

候选字段：

- `id`
- `username`
- `password_hash`
- `display_name`
- `status`
- `created_at`
- `updated_at`

### 9.2 `auth_refresh_token`

用于保存可撤销刷新凭证。

候选字段：

- `id`
- `user_id`
- `token_hash`
- `device_id`
- `client_type`
- `expires_at`
- `revoked_at`
- `created_at`
- `last_used_at`

### 9.3 `auth_login_audit`

用于记录登录和鉴权关键事件。

候选字段：

- `id`
- `user_id`
- `event_type`
- `client_type`
- `ip`
- `user_agent`
- `trace_id`
- `created_at`

## 10. 前端集成边界

后续前端按现有分层新增：

```text
frontend/src/
├── api/
│   └── authApi.ts
├── types/
│   └── auth.ts
├── stores/
│   └── authStore.ts
├── router/
│   └── index.ts
└── components/
    └── auth/
```

职责划分：

- `authApi.ts` 只封装登录、刷新、退出、当前用户接口。
- `auth.ts` 定义登录命令、Token 响应、当前用户 DTO。
- `authStore.ts` 管理登录态、当前用户、刷新中状态和退出动作。
- 路由守卫只判断页面是否需要登录，不直接处理 HTTP 细节。
- 组件只处理输入、提交和错误展示，不直接拼接后端 URL。

macOS 的 `Command + ,` 配置弹窗仍属于偏好设置入口，不应混入登录凭证编辑。后续可以在设置弹窗展示当前登录用户、退出登录和 API Base URL，但长期凭证存储应由安全存储层处理。

## 11. 分阶段落地建议

### 阶段 1：应用内账号登录

目标：用最小切片保护 `/api/**`，让 macOS 客户端和未来 Web 都能登录。

范围：

- 新增 `dtools-auth` 模块。
- 新增 Spring Security 配置。
- 新增登录、刷新、退出、当前用户接口。
- 新增最小用户表和刷新凭证候选表。
- 新增前端 `authApi / authStore / auth types`。
- 保护工具执行接口，保留健康检查公开。

验收命令：

```bash
mvn -q test
npm --prefix frontend run typecheck
npm --prefix frontend run build
```

### 阶段 2：权限与审计

目标：让工具执行、历史查看和设置修改拥有明确权限边界。

范围：

- 引入权限编码，例如 `tool:read`、`tool:execute`、`history:read`、`settings:write`。
- 在工具执行历史中关联 `user_id` 和 `trace_id`。
- 补充登录审计和异常审计。
- 统一 401 / 403 响应码和前端处理。

### 阶段 3：Web 安全加固

目标：为 Web 部署补齐浏览器安全边界。

范围：

- Refresh Token 使用 HttpOnly Cookie。
- 配置 CORS 白名单。
- 配置安全响应头。
- 明确 CSRF 策略。
- 增加登录失败限流或锁定策略。

### 阶段 4：外部身份提供方评估

触发条件：

- 需要第三方登录。
- 需要多个客户端统一登录。
- 需要 SSO。
- 需要组织、团队、角色管理后台。
- 需要把 dtools API 开放给其他应用调用。

候选升级：

- 外部 IdP：Keycloak。
- 自建轻量授权服务：Spring Authorization Server。
- 后端继续作为 OAuth2 Resource Server，主要负责校验外部签发的 Access Token。

## 12. 风险与注意事项

- 不要手写绕过 Spring Security FilterChain 的自定义鉴权拦截器，否则后续 Web 安全、方法权限和资源服务器能力会变得割裂。
- 不要把 `enum.name()` 作为角色、权限或数据库稳定值。角色和权限编码应是明确字符串或数字 code，并写清含义。
- 不要把 Refresh Token 明文落库。
- 不要把长期凭证存入 Web `localStorage` 或普通 Tauri 配置文件。
- 不要在 Access Token 中放过多用户资料，权限变化后短期 Token 仍可能保留旧权限。
- 不要在第一阶段提前实现组织、租户、第三方登录、OAuth2 Client 注册等未确认能力。

## 13. 参考资料

- Spring Security OAuth2 Resource Server JWT：https://docs.spring.io/spring-security/reference/6.5/servlet/oauth2/resource-server/jwt.html
- Spring Security OAuth2 overview：https://docs.spring.io/spring-security/reference/6.5/servlet/oauth2/index.html
- Spring Security password storage：https://docs.spring.io/spring-security/reference/6.5/features/authentication/password-storage.html
- Spring Authorization Server overview：https://docs.spring.io/spring-authorization-server/reference/overview.html
- Keycloak documentation：https://www.keycloak.org/documentation
- Keycloak supported specifications：https://www.keycloak.org/securing-apps/specifications
- Sa-Token 官方文档：https://www.sa-token.cc/doc.html
- Apache Shiro authorization documentation：https://shiro.apache.org/authorization.html
