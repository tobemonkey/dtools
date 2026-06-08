# 权限模块阶段 1 任务说明

## 目标

用最小可验收切片完成 dtools 应用内账号登录和权限体验闭环，保护后端 `/api/**`，并让前端通过 `/api/auth/me` 驱动路由、按钮和用户展示。

## 后端契约

| 方法 | 路径 | 公开 | 说明 |
| --- | --- | --- | --- |
| `GET` | `/api/health` | 是 | 健康检查 |
| `POST` | `/api/auth/login` | 是 | 账号密码登录，返回 access token 和 refresh token |
| `POST` | `/api/auth/refresh` | 是 | 使用 refresh token 换取新 token，并轮换 refresh token |
| `POST` | `/api/auth/logout` | 否 | 撤销当前 refresh token |
| `GET` | `/api/auth/me` | 否 | 返回当前用户、角色、权限和数据范围 |

### 返回协议

所有接口继续使用 `ApiResponse<T>`。

登录成功数据：

```json
{
  "accessToken": "...",
  "refreshToken": "...",
  "tokenType": "Bearer",
  "expiresInSeconds": 1800,
  "currentUser": {
    "id": 1,
    "username": "owner",
    "displayName": "Owner",
    "roles": ["owner"],
    "permissions": ["tool:def:read", "tool:execute"],
    "dataScope": "all"
  }
}
```

认证失败：HTTP 401，`code = 40100`。

授权失败：HTTP 403，`code = 40300`。

## 前端契约

- `/login` 公开，登录成功后跳转 `redirect` 或 `/`。
- `/403` 公开，用于权限不足展示。
- `/` 工作台需要登录，权限不足时走 `/403`。
- `authStore` 提供 `login`、`logout`、`refresh`、`ensureCurrentUser`、`hasPermission`、`hasAnyPermission`、`hasAllPermissions`、`hasDataScope`。
- HTTP 层自动附带 access token；收到 401 后尝试刷新一次，失败则清理登录态并跳 `/login`。
- 工作台 Run 按钮按 `tool:execute` 做体验层禁用，后端仍是最终权限边界。

## 数据库说明

本阶段新增 `docs/database/auth-schema.sql` 作为正式建表脚本说明，但不引入迁移工具。

## 验收命令

```bash
mvn -q -DskipTests compile
mvn -q test
npm --prefix frontend run typecheck
npm --prefix frontend run build
```

新增或更新 UI 时需要启动前端并生成至少一张截图作为交付说明。
