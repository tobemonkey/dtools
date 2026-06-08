# dtools 前后端技术选型

## 1. 结论

dtools 采用「前后端分离 + Web 技术工作台 + macOS Tauri 壳 + Spring Boot 独立后端服务 + MySQL 主账本」的组合。当前阶段只优先开发 macOS 端，iOS 作为后续同 Apple 生态扩展方向评估，Windows 暂不进入近期实现范围。

| 层级 | 选型 | 定位 |
| --- | --- | --- |
| 前端框架 | Vue 3 + TypeScript + Vite | 构建独立前端应用；macOS Tauri 壳和 Web 部署复用同一套 UI |
| 前端状态 | Pinia | 管理跨组件工具状态、执行结果、历史摘要和用户配置 |
| 前端路由 | Vue Router | Web 部署和桌面多视图复用，初期保持轻量 |
| UI 基础 | 自定义设计系统 + Reka UI / Headless primitives + lucide-vue-next | 保留新式视觉自由度，同时降低弹窗、菜单、快捷键等基础交互风险 |
| 桌面壳 | Tauri 2 | macOS 前端宿主和系统能力桥；不承接后端业务 |
| 后端语言 | Java 21 | 长期维护、稳定并发、生态成熟 |
| 后端框架 | Spring Boot 3.5.x | 工具协议、API、配置、审计、后续扩展的应用服务底座 |
| 后端构建 | Maven 多模块 | 匹配 `common / tools / bootstrap` 边界 |
| ORM / SQL | MyBatis | 面向用例写 SQL，避免工具协议和历史账本被通用 CRUD 牵着走 |
| 数据库 | MySQL 8.4 LTS 优先，兼容 MySQL 8 系列 | 作为可审计主账本；执行历史、重要配置和协议状态优先落库 |
| API 风格 | REST JSON 优先 | 前端/Tauri 通过 HTTP 调用独立后端服务；复杂实时能力后置 |

## 2. 为什么这样选

### 2.1 前端：Vue 3 + TypeScript + Vite

Vue 3 适合这个项目的原因是：界面以个人工具工作台为主，交互密度中等，但视觉自定义要求高。Vue 的单文件组件和组合式 API 能让页面、组件、状态和工具逻辑拆得比较清楚；TypeScript 用来收敛工具协议、执行入参、执行结果和历史摘要的类型边界；Vite 负责快速开发和后续 Web / Tauri 共用构建。

前端是独立应用，不直接链接后端代码，也不访问数据库。所有业务能力通过 REST API 调用 Spring Boot 后端；Tauri invoke 只用于 macOS 系统能力。

不选择 React 不是因为 React 不适合，而是本仓库规范已经明确 Vue 3 技术线。此阶段保持技术一致性比换栈更重要。

### 2.2 UI：不直接套重型组件库

当前产品气质不是后台系统，也不是表格型管理台。重型 UI 组件库容易把界面带回传统 tab、侧栏、卡片堆叠的方向，和当前确认的「个人工具空间 / widget bento」视觉方向冲突。

推荐策略：

- 基础视觉层使用 CSS 变量、少量工具类和项目内组件沉淀。
- 弹窗、菜单、焦点管理、无障碍交互使用 Reka UI 这类 headless primitives。
- 图标使用 `lucide-vue-next`，避免手写散乱 SVG。

这样既能保留视觉自由度，又不用自己从零处理 Dialog、Popover、键盘焦点和无障碍细节。

### 2.3 状态：Pinia

Pinia 适合承接这些跨组件状态：

- 当前选中的工具定义
- 命令输入和执行中状态
- 最近执行摘要
- 用户设置和快捷键配置
- 桌面端与后端同步状态

局部输入态和单个 widget 的展开态可以留在组件内部，避免全局 store 膨胀。

### 2.4 桌面：Tauri 2，macOS 优先

Tauri 2 适合当前 macOS 优先阶段的原因：

- 使用系统 WebView，包体通常比 Electron 更轻。
- Rust 命令层适合承接 macOS 桌面能力，例如文件选择、剪贴板、窗口、快捷键、菜单栏、Dock、系统托盘等。
- 前端仍然是 Vue 3，后续 Web 版本或 iOS 评估时可复用主要界面和协议类型。
- Tauri 2 已经提供移动端方向，后续 iOS 可以优先评估复用同一套前端和部分命令层能力。

边界要清楚：Tauri 是前端壳和系统能力桥，不是后端容器。工具定义、执行编排、历史记录和配置主账本仍由 Spring Boot 后端提供。当前不为 Windows 提前设计交互和打包流程，避免牺牲 macOS 体验。iOS 后期也不能默认等同于“无成本复用”。如果后续 iOS 需要大量原生系统能力、复杂后台任务或深度平台交互，需要单独评估 SwiftUI 或 Tauri plugin 能力，不在当前阶段提前承诺。

### 2.5 后端：Java 21 + Spring Boot 3.5.x

后端不是简单给页面做 CRUD，而是承担工具协议、工具注册、工具执行编排、历史记录和后续扩展 API。Spring Boot 更适合作为长期可维护的应用服务底座：

- Controller / Service / BizService / Mapper 分层清楚。
- 适合统一响应码、异常处理、TraceID 和审计。
- 和 MyBatis、MySQL、Maven 多模块配合成熟。
- Java 21 是当前长期维护 Java 版本之一，适合个人工具平台长期演进。

### 2.6 SQL：MyBatis

本项目强调 Service 用例优先和 Mapper 按用例设计。MyBatis 比 JPA 更适合当前方向：

- SQL 可控，便于审计工具执行历史和查询条件。
- 避免为每张表提前铺通用 CRUD。
- 面向具体用例写 Mapper 方法，符合当前项目规范。
- 后续复杂筛选、历史检索、批量写入更容易把性能和字段边界说清楚。

### 2.7 数据库：MySQL 8.4 LTS 优先

仓库规范写的是 MySQL 8。落地时建议优先使用 MySQL 8.4 LTS，原因是 LTS 版本更适合做长期主账本。若本地环境暂时只有 MySQL 8.0，可以短期兼容，但正式文档、建表和部署说明应按 MySQL 8.4 LTS 校准。

MySQL 在本项目中的定位不是缓存，而是可审计主账本：

- 工具定义
- 工具执行历史
- 输入输出摘要
- TraceID
- 重要用户配置
- 后续同步状态

桌面本地状态、搜索索引、缓存和临时 UI 状态都应视为可重建派生数据。

## 3. 暂不选择的方案

| 方案 | 暂不选择原因 |
| --- | --- |
| Electron | 跨平台成熟，但包体和资源占用偏重；当前 macOS 个人工具 App 更适合轻壳 |
| Flutter | 跨端 UI 能力强，但会脱离现有 Vue/Web 技术线，Web 工作台复用成本变高 |
| SwiftUI only | macOS / iOS 体验好，但会削弱 Web 技术工作台复用；可作为后续 iOS 或 macOS 原生增强的备选 |
| React / Next.js | 生态成熟，但与仓库既定 Vue 3 方向不一致 |
| JPA / Hibernate | 对通用实体建模友好，但当前更需要可控 SQL 和用例导向 Mapper |
| SQLite 作为主账本 | 适合本地单机，但与“后端主账本、可审计、后续 Web 部署”目标不一致 |
| GraphQL | 当前工具协议和执行 API 用 REST 足够清晰，引入 GraphQL 会增加治理成本 |
| WebSocket / SSE 默认引入 | 工具执行先按短任务 REST 处理；只有出现长任务进度推送时再引入 |

## 4. 分阶段落地

### 阶段 1：Web 预览和基础前端工程

- Vue 3 + TypeScript + Vite
- Pinia
- Vue Router
- 自定义视觉组件
- Reka UI / lucide-vue-next

验收命令：

```bash
npm --prefix frontend run typecheck
npm --prefix frontend run build
```

### 阶段 2：后端协议和执行闭环

- JDK 21
- Spring Boot 3.5.x
- Maven 多模块
- MyBatis
- MySQL 8.4 LTS
- 统一 `ApiResponse<Integer code>`
- TraceID

验收命令：

```bash
mvn -q -DskipTests compile
mvn -q test
```

### 阶段 3：Tauri 2 macOS 桌面壳

- macOS 打包
- `Command + ,` 配置弹窗入口
- 文件、剪贴板、窗口、快捷键、菜单栏、Dock 等 macOS 桌面能力按需接入
- 通过配置的 API Base URL 调用独立 Spring Boot 后端
- 本地派生状态只做缓存，不替代 MySQL 主账本

### 阶段 4：iOS / Web 扩展评估

- Web 版本优先复用 Vue 前端和后端 REST API。
- iOS 作为 Apple 生态内的优先后续方向，先评估 Tauri 2 移动能力、插件生态和 App Store 约束。
- 如果 iOS 需要大量原生交互，再单独评估 SwiftUI 局部或独立客户端。
- Windows 暂不进入近期路线；只有当用户明确需要 Windows 桌面端时，再新增独立评估和打包计划。

## 5. 当前定稿

当前阶段定稿：

- 前端：Vue 3 + TypeScript + Vite + Pinia + Vue Router
- UI：项目自定义设计系统 + Reka UI + lucide-vue-next
- 桌面：Tauri 2，macOS 优先；Windows 后置
- 后端：Java 21 + Spring Boot 3.5.x + Maven 多模块
- SQL：MyBatis
- 数据库：MySQL 8.4 LTS 优先，兼容 MySQL 8 系列
- API：前后端分离 REST JSON 优先，实时通道后置

## 6. 参考

- Spring Boot 3.5 System Requirements: https://docs.spring.io/spring-boot/3.5-SNAPSHOT/system-requirements.html
- Vue TypeScript Guide: https://vuejs.org/guide/typescript/overview
- Tauri 2 Prerequisites: https://v2.tauri.app/start/prerequisites/
- MySQL 8.4 Reference Manual: https://dev.mysql.com/doc/refman/en/index.html
