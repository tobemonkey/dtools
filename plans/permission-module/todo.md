# 权限模块阶段 1 Todo

- [x] 创建独立分支 `feature/permission-260608`
- [x] 读取现有权限架构文档和前后端代码结构
- [x] 派发后端 explorer 盘点模块和安全配置落点
- [x] 派发前端 explorer 盘点登录态、路由和 HTTP 接入点
- [x] 运行基线验证命令
- [x] 新增后端 `dtools-auth` 模块、POM 依赖和 schema 文档
- [x] 新增后端角色、权限、数据范围、用户状态枚举
- [x] 新增后端 Auth Service、Token Service、Refresh Token 服务和 Mapper
- [x] 新增后端 Auth Controller 与 Spring Security 配置
- [ ] 新增或更新后端测试，验证登录、刷新、当前用户、401 和 403
- [x] 新增前端 auth 类型、API、权限工具和 Pinia store
- [x] 更新前端 HTTP 封装、路由守卫、登录页和 403 页
- [x] 更新工作台用户展示、退出入口和 Run 按钮权限态
- [ ] 运行后端与前端验证命令
- [ ] 启动前端并截图验证 UI
- [ ] 更新交付说明，记录临时风险和建议切换 QA 新会话的输入清单

## 后续安全演进待办

- [ ] 将 Refresh Token 从 `AuthTokenDTO` 响应体迁移到环境化写出策略：Web 使用 `HttpOnly + Secure + SameSite` Cookie，Tauri 使用 Keychain / Windows Credential Manager 等系统安全存储。
- [ ] 调整前端刷新流程：Web 模式不读取 refresh token 明文，只依赖 Cookie；Tauri 模式通过安全存储桥接 refresh token。
- [ ] 评估 Cookie 模式下的 CSRF 策略、CORS 白名单和退出登录 Cookie 清理语义。
