# dtools 模块拆分与目录树

## 1. 总体分层

dtools 采用「前后端分离 + Tauri 2 macOS 前端壳 + Spring Boot 独立后端服务 + MySQL 主账本」结构。

核心原则：

- 前端只理解公开 API 和工具协议，不直接理解数据库结构。
- 前端和后端通过 HTTP REST API 通信，后端独立启动、独立部署、独立验证。
- Tauri 只承接 macOS 桌面能力，不承接业务主账本，也不直接访问数据库。
- 后端负责工具协议、工具执行编排、历史记录、配置和审计。
- MySQL 是可审计主账本，本地缓存和 UI 状态都是可重建派生数据。

运行时边界：

```text
macOS App(Tauri + Vue)
  ├─ HTTP REST -> Spring Boot API
  └─ Tauri invoke -> macOS system capabilities only

Spring Boot API
  └─ MyBatis -> MySQL main ledger
```

这意味着 Tauri 不是后端容器，不能把工具业务、历史账本、Mapper 或数据库连接放进 `src-tauri`。即使未来为了离线能力启动本地后端进程，也必须保持“前端进程 / 后端服务 / 数据库或本地派生缓存”的职责分离。

## 2. 目标目录树

```text
dtools/
├── AGENTS.md
├── PLANS.md
├── pom.xml
├── dtools-common/
│   ├── pom.xml
│   └── src/main/java/com/dtools/common/
│       ├── response/
│       ├── exception/
│       ├── trace/
│       ├── enums/
│       └── util/
├── dtools-tools/
│   ├── pom.xml
│   └── src/main/java/com/dtools/tools/
│       ├── definition/
│       ├── executor/
│       ├── history/
│       ├── service/
│       │   ├── impl/
│       │   └── biz/
│       ├── mapper/
│       ├── model/
│       │   ├── command/
│       │   ├── query/
│       │   ├── dto/
│       │   └── entity/
│       └── config/
├── dtools-bootstrap/
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/dtools/bootstrap/
│       │   ├── DtoolsApplication.java
│       │   ├── controller/
│       │   ├── config/
│       │   └── advice/
│       └── resources/
│           ├── application.yml
│           ├── application-local.yml
│           └── mapper/
├── frontend/
│   ├── package.json
│   ├── vite.config.ts
│   ├── tsconfig.json
│   ├── index.html
│   ├── src/
│   │   ├── main.ts
│   │   ├── App.vue
│   │   ├── api/
│   │   ├── types/
│   │   ├── stores/
│   │   ├── views/
│   │   ├── components/
│   │   │   ├── workspace/
│   │   │   ├── tool/
│   │   │   ├── settings/
│   │   │   └── ui/
│   │   ├── router/
│   │   ├── utils/
│   │   └── styles/
│   └── src-tauri/
│       ├── tauri.conf.json
│       ├── Cargo.toml
│       ├── build.rs
│       └── src/
│           ├── main.rs
│           ├── commands/
│           ├── window/
│           ├── shortcuts/
│           └── platform/
│               └── macos.rs
├── docs/
│   ├── architecture/
│   ├── previews/
│   └── agent/
└── plans/
    └── <task-name>/
        ├── task.md
        ├── analysis.md
        └── todo.md
```

说明：当前采用 monorepo 管理前端和后端代码，但架构上仍是前后端分离。`dtools-common / dtools-tools / dtools-bootstrap` 组成后端服务；`frontend` 组成前端应用和 Tauri 壳。两者通过 API Contract 对接，而不是通过代码直接互相调用。

## 3. 后端模块

### 3.1 `dtools-common`

通用基础模块，不依赖任何业务模块。

职责：

- 统一响应结构，例如 `ApiResponse<Integer code>`。
- 统一响应码枚举，响应码必须有稳定 `code` 和中文 `desc`。
- 统一异常和异常转换。
- TraceID 生成、传递和日志辅助。
- 通用工具类。

不放：

- 工具定义。
- 工具执行逻辑。
- Controller。
- Mapper。
- 数据库实体。

### 3.2 `dtools-tools`

工具协议和工具业务核心模块。

职责：

- `ToolDefinition` 工具定义模型。
- 工具注册、工具查询、工具执行编排。
- 工具执行器接口和内置执行器实现。
- 工具执行历史主账本适配。
- 面向用例的 Service、BizService、Mapper。
- 工具相关 Command / Query / DTO / Entity。

建议包职责：

| 包 | 作用 |
| --- | --- |
| `definition` | 工具协议、工具元信息、入参/出参定义 |
| `executor` | 工具执行器接口、执行上下文、执行结果 |
| `history` | 执行历史、摘要、耗时、TraceID 记录 |
| `service` | Controller 面向的应用服务接口 |
| `service/impl` | 应用服务实现、事务边界、DTO 组装 |
| `service/biz` | 业务规则、跨 Mapper 编排、状态判断 |
| `mapper` | MyBatis Mapper，按当前 Service 用例设计 |
| `model/command` | 写操作入参，例如创建工具、执行工具 |
| `model/query` | 查询入参，例如工具列表、历史筛选 |
| `model/dto` | API 返回 DTO |
| `model/entity` | 数据库实体 |

### 3.3 `dtools-bootstrap`

Spring Boot 启动和接口入口模块。

职责：

- 应用启动类。
- REST Controller。
- 全局异常处理。
- Web 配置、跨域、序列化、TraceID Filter。
- Spring Boot 配置文件。
- MyBatis XML Mapper 资源。

依赖方向：

```text
dtools-bootstrap -> dtools-tools -> dtools-common
```

## 4. 前端模块

### 4.1 `frontend/src/api`

只封装后端公开接口调用。前端访问业务能力必须经过这里或基于这里封装的 store action，不允许组件直接拼接后端 URL。

示例：

```text
api/
├── http.ts
├── apiBaseUrl.ts
├── toolApi.ts
├── historyApi.ts
├── settingsApi.ts
└── tauriApi.ts
```

职责：

- HTTP client 封装。
- API 路径和请求方法。
- 请求/响应错误的基础转换。
- 区分后端 REST API 与 Tauri invoke。`tauriApi.ts` 只封装系统能力，不封装业务主账本。

不放：

- 页面状态。
- 组件逻辑。
- 数据库字段假设。
- Tauri 原生命令散落调用。

### 4.2 `frontend/src/types`

定义前后端协议类型和前端领域类型。

示例：

```text
types/
├── api.ts
├── tool.ts
├── history.ts
├── settings.ts
└── tauri.ts
```

职责：

- `ApiResponse<T>`。
- `ToolDefinition` 前端协议类型。
- 工具执行入参和结果类型。
- 设置项类型。
- Tauri 命令返回类型。

### 4.3 `frontend/src/stores`

Pinia 状态层。

示例：

```text
stores/
├── toolStore.ts
├── commandStore.ts
├── historyStore.ts
└── settingsStore.ts
```

职责：

- 当前工具。
- 命令输入和执行状态。
- 最近执行摘要。
- 偏好设置。
- 前端与后端同步状态。

### 4.4 `frontend/src/views`

页面级视图，只负责组合布局和分发用户动作。

示例：

```text
views/
├── WorkspaceView.vue
├── ToolRunView.vue
└── SettingsView.vue
```

当前 macOS v1 可以只有一个 `WorkspaceView.vue`，但仍然不能把 API、状态、工具函数长期堆在页面里。

### 4.5 `frontend/src/components`

业务组件和基础 UI 组件。

示例：

```text
components/
├── workspace/
│   ├── CommandWidget.vue
│   ├── ToolSpaceBoard.vue
│   ├── RecentRunWidget.vue
│   └── ToolCalendarWidget.vue
├── tool/
│   ├── ToolPill.vue
│   ├── ToolResultPanel.vue
│   └── ToolInputPanel.vue
├── settings/
│   ├── SettingsDialog.vue
│   └── ShortcutSettingRow.vue
└── ui/
    ├── GlassPanel.vue
    ├── IconButton.vue
    └── AppDialog.vue
```

### 4.6 `frontend/src/utils`

无副作用纯函数。

示例：

```text
utils/
├── formatDuration.ts
├── formatTraceId.ts
├── toolInput.ts
└── date.ts
```

## 5. Tauri 2 模块

Tauri 位于 `frontend/src-tauri`，只承接 macOS 壳和系统能力。

```text
src-tauri/
├── tauri.conf.json
├── Cargo.toml
├── build.rs
└── src/
    ├── main.rs
    ├── commands/
    │   ├── mod.rs
    │   ├── clipboard.rs
    │   ├── file.rs
    │   └── settings.rs
    ├── window/
    │   ├── mod.rs
    │   └── preferences.rs
    ├── shortcuts/
    │   ├── mod.rs
    │   └── preferences_shortcut.rs
    └── platform/
        ├── mod.rs
        └── macos.rs
```

职责：

- 注册 macOS 应用窗口。
- 处理 `Command + ,` 偏好设置入口。
- 文件选择、剪贴板、窗口控制等系统能力。
- 后续按需处理菜单栏、Dock、系统托盘。
- 读取前端配置中的后端 API 地址，或为前端提供安全的系统级配置读写能力。

不放：

- 工具协议核心业务。
- 历史主账本。
- 数据库访问。
- 大量业务状态。
- Spring Boot 启动逻辑。
- MyBatis Mapper。

前端和 Tauri 通信通过 `invoke` 封装到 `frontend/src/api/tauriApi.ts` 或 `frontend/src/utils/tauriInvoke.ts`，不要散落在组件里。

前端和后端通信通过 HTTP REST API：

```text
frontend/src/api/http.ts
  -> http://localhost:8080/api/*   # 本地开发
  -> https://<server-domain>/api/* # 后续 Web / 远程服务
```

macOS App 首版可以先连接本机或局域网后端服务；是否由 App 附带启动本地后端进程属于后续打包策略，不影响前后端分离边界。

## 6. API 模块建议

初期 REST API 可按用户路径拆：

```text
/api/tools
/api/tools/{toolId}
/api/tools/{toolId}/run
/api/history
/api/history/{historyId}
/api/settings
```

Controller 建议：

```text
controller/
├── ToolController.java
├── ToolRunController.java
├── ToolHistoryController.java
└── UserSettingsController.java
```

Service 建议：

```text
service/
├── ToolService.java
├── ToolRunService.java
├── ToolHistoryService.java
└── UserSettingsService.java
```

BizService 建议：

```text
service/biz/
├── ToolBizService.java
├── ToolRunBizService.java
└── ToolHistoryBizService.java
```

## 7. 当前阶段不提前创建的内容

为避免过度设计，以下内容不在 macOS v1 初期提前落地：

- Windows 打包目录和配置。
- iOS 工程。
- 复杂权限系统。
- 多用户账号体系。
- WebSocket / SSE 实时通道。
- 提醒、承接、快照等后续阶段专用表。
- 大而全的通用 CRUD Mapper。

这些内容可以先写入候选设计，不进入正式 schema 和代码骨架。
