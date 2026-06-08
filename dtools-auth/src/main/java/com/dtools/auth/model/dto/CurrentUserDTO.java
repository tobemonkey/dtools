package com.dtools.auth.model.dto;

import java.util.List;

/**
 * @description: 当前登录用户摘要，作为前端权限体验的可信输入
 * @author: yesterday'jam
 * @date: 2026/06/08
 * @注意: roles、permissions、dataScope 均由后端计算，前端不得自行推导
 */
public class CurrentUserDTO {

    /**
     * 用户主键 ID。
     */
    private Long id;

    /**
     * 登录用户名。
     */
    private String username;

    /**
     * 前端展示名称。
     */
    private String displayName;

    /**
     * 用户角色稳定 code 集合。
     */
    private List<String> roles;

    /**
     * 用户权限码集合。
     */
    private List<String> permissions;

    /**
     * 用户数据范围 code。
     */
    private String dataScope;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }

    public String getDataScope() {
        return dataScope;
    }

    public void setDataScope(String dataScope) {
        this.dataScope = dataScope;
    }
}
