# PLANS.md - dtools 任务索引

## 1. 文件职责

本文件只做任务索引与进度登记，不承载完整流程细节。

详细规则见：

- `AGENTS.md`
- `harness.yaml`
- `docs/agent/WORKFLOW.md`
- `docs/agent/ANALYSIS-WRITING.md`

## 2. 平台分层

- `dtools-common`：通用返回、异常、TraceID、基础工具
- `dtools-tools`：工具定义、工具执行、工具历史、MySQL 主账本适配
- `dtools-bootstrap`：Spring Boot 启动入口、Controller 与配置
- `frontend`：跨平台工作台前端和桌面壳入口

## 3. 任务索引

| 任务 ID | 任务说明 | 当前状态 | 关联文档 |
| --- | --- | --- | --- |
| 暂无 | 等待新任务确认 | Pending | - |

## 4. 当前聚焦任务

暂无。

新任务开始前，必须先完成产品探索或用户确认，再登记到任务索引。

## 5. 状态维护规则

- 新任务先在本文件登记，再创建 `plans/<任务名>/`
- 每完成 `todo.md` 的关键节点，同步更新当前聚焦任务状态
- 规范、产品文档和技术文档必须与代码演进同步
