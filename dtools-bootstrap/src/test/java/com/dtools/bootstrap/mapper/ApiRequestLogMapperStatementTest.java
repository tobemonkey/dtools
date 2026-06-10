package com.dtools.bootstrap.mapper;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @description: 接口请求日志 Mapper 映射注册测试，验证 XML 写入语句和注解查询语句同时存在
 * @author: yesterday'jam
 * @date: 2026/06/10
 * @注意: 本测试不连接真实数据库，仅检查 MyBatis statement 注册，避免 XML 和注解混用时遗漏映射
 */
class ApiRequestLogMapperStatementTest {

    /**
     * @description: 验证请求日志 Mapper 的写入和统计查询 statement 都能注册
     * @author: yesterday'jam
     * @date: 2026/06/10
     * @注意: insert 来自 XML，统计查询来自注解，两类映射都必须存在
     */
    @Test
    void apiRequestLogMapperShouldRegisterInsertAndMonitorQueryStatements() throws Exception {
        Configuration configuration = new Configuration();
        configuration.addMapper(ApiRequestLogMapper.class);
        String resource = "mapper/logging/ApiRequestLogMapper.xml";
        try (InputStream inputStream = Resources.getResourceAsStream(resource)) {
            XMLMapperBuilder mapperBuilder = new XMLMapperBuilder(inputStream, configuration, resource, configuration.getSqlFragments());
            mapperBuilder.parse();
        }

        String namespace = ApiRequestLogMapper.class.getName();
        assertThat(configuration.hasStatement(namespace + ".insert")).isTrue();
        assertThat(configuration.hasStatement(namespace + ".selectOverview")).isTrue();
        assertThat(configuration.hasStatement(namespace + ".selectSlowEndpoints")).isTrue();
        assertThat(configuration.hasStatement(namespace + ".selectInterfaceDetailRecentErrors")).isTrue();
    }
}
