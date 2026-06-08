package com.dtools.tools.model.dto;

/**
 * @description: 工具模块健康检查 DTO，验证后端模块链路是否可用
 * @author: yesterday'jam
 * @date: 2026/06/08
 * @注意: 当前仅用于框架搭建验收，不承载业务含义
 */
public class ToolHealthDTO {

    /**
     * 工具模块状态。
     */
    private String status;

    /**
     * 状态说明。
     */
    private String message;

    public ToolHealthDTO() {
    }

    public ToolHealthDTO(String status, String message) {
        this.status = status;
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
