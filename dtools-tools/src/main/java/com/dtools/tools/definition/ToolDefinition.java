package com.dtools.tools.definition;

/**
 * @description: 工具定义基础模型，用于描述个人工具的稳定协议入口
 * @author: yesterday'jam
 * @date: 2026/06/08
 * @注意: 当前仅作为框架占位，真实字段需跟随首个工具用例逐步扩展
 */
public class ToolDefinition {

    /**
     * 工具稳定标识。
     */
    private String toolId;

    /**
     * 工具展示名称。
     */
    private String name;

    public String getToolId() {
        return toolId;
    }

    public void setToolId(String toolId) {
        this.toolId = toolId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
