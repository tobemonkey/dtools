package com.dtools.bootstrap.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @description: 安全配置测试，验证本地前端开发域名可以通过 CORS 预检
 * @author: yesterday'jam
 * @date: 2026/06/09
 * @注意: CORS 在 Spring Security 过滤链前处理，避免浏览器预检被拦截为 403
 */
class SecurityConfigTest {

    /**
     * @description: 验证 Vite 本地开发源允许访问登录接口
     * @author: yesterday'jam
     * @date: 2026/06/09
     * @注意: 前端默认使用 localhost:5173，也兼容 127.0.0.1:5173
     */
    @Test
    void corsShouldAllowLocalViteOriginsForAuthLogin() {
        SecurityConfig securityConfig = new SecurityConfig(null, null, null);
        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/api/auth/login");
        request.addHeader("Origin", "http://localhost:5173");
        request.addHeader("Access-Control-Request-Method", "POST");
        request.addHeader("Access-Control-Request-Headers", "content-type");

        CorsConfiguration configuration = securityConfig.corsConfigurationSource().getCorsConfiguration(request);

        assertThat(configuration).isNotNull();
        assertThat(configuration.checkOrigin("http://localhost:5173")).isEqualTo("http://localhost:5173");
        assertThat(configuration.checkOrigin("http://127.0.0.1:5173")).isEqualTo("http://127.0.0.1:5173");
        assertThat(configuration.checkHttpMethod(org.springframework.http.HttpMethod.POST)).isNotNull();
        assertThat(configuration.checkHeaders(java.util.List.of("content-type", "authorization")))
                .contains("content-type", "authorization");
    }
}
