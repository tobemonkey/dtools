package com.dtools.bootstrap.mapper;

import com.dtools.bootstrap.logging.event.ApiRequestLogEvent;
import org.apache.ibatis.annotations.Mapper;

/**
 * @description: 接口请求日志 Mapper，负责将 API 请求运行快照写入 MySQL 主账本
 * @author: yesterday'jam
 * @date: 2026/06/10
 * @注意: 仅服务当前请求日志写入用例，不预置通用 CRUD
 */
@Mapper
public interface ApiRequestLogMapper {

    /**
     * @description: 插入接口请求日志
     * @author: yesterday'jam
     * @date: 2026/06/10
     * @注意: 写入失败必须由调用方吞掉，不能影响接口主流程
     */
    int insert(ApiRequestLogEvent event);
}
