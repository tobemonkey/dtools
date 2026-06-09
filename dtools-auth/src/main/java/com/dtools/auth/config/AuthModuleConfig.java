package com.dtools.auth.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * @description: 鉴权模块基础 Bean 配置
 * @author: yesterday'jam
 * @date: 2026/06/08
 * @注意: PasswordEncoder 统一使用 Spring Security 提供的 BCrypt 实现
 */
@Configuration
@EnableConfigurationProperties(AuthProperties.class)
public class AuthModuleConfig {

    /**
     * @description: 提供密码哈希校验器
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: 新增用户密码必须使用该 Bean 生成哈希，不得明文存储
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
