package com.dtools.bootstrap;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @description: dtools 后端启动入口，负责启动独立 Spring Boot API 服务
 * @author: yesterday'jam
 * @date: 2026/06/08
 * @注意: 前端和 Tauri 通过 HTTP API 调用该服务，不在 Tauri 内承载后端业务
 */
@SpringBootApplication(scanBasePackages = "com.dtools")
@MapperScan("com.dtools.**.mapper")
public class DtoolsApplication {

    /**
     * @description: 启动 dtools 后端服务
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: 本地开发默认监听 application.yml 中配置的端口
     */
    public static void main(String[] args) {
        SpringApplication.run(DtoolsApplication.class, args);
    }
}
