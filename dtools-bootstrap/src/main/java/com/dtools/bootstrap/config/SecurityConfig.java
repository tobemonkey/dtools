package com.dtools.bootstrap.config;

import com.dtools.auth.config.AuthProperties;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @description: Spring Security 配置，保护后端 API 并启用 JWT Resource Server 校验
 * @author: yesterday'jam
 * @date: 2026/06/08
 * @注意: 仅公开健康检查、登录和刷新接口，其他 /api/** 默认要求认证
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final ApiAuthenticationEntryPoint apiAuthenticationEntryPoint;

    private final ApiAccessDeniedHandler apiAccessDeniedHandler;

    private final TraceIdFilter traceIdFilter;

    public SecurityConfig(ApiAuthenticationEntryPoint apiAuthenticationEntryPoint,
                          ApiAccessDeniedHandler apiAccessDeniedHandler,
                          TraceIdFilter traceIdFilter) {
        this.apiAuthenticationEntryPoint = apiAuthenticationEntryPoint;
        this.apiAccessDeniedHandler = apiAccessDeniedHandler;
        this.traceIdFilter = traceIdFilter;
    }

    /**
     * @description: 配置公开 API 安全过滤链
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: 该链不启用 Resource Server，公开路径会忽略调用方携带的非法 Bearer Token
     */
    @Bean
    @Order(1)
    public SecurityFilterChain publicSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/health", "/api/auth/login", "/api/auth/refresh")
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(apiAuthenticationEntryPoint)
                        .accessDeniedHandler(apiAccessDeniedHandler))
                .authorizeHttpRequests(authorize -> authorize
                        .anyRequest().permitAll())
                .addFilterBefore(traceIdFilter, SecurityContextHolderFilter.class);
        return http.build();
    }

    /**
     * @description: 配置受保护 API 安全过滤链
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: 当前采用 Bearer Token，无服务端 Session 状态
     */
    @Bean
    @Order(2)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/**")
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(apiAuthenticationEntryPoint)
                        .accessDeniedHandler(apiAccessDeniedHandler))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll())
                .oauth2ResourceServer(resourceServer -> resourceServer
                        .authenticationEntryPoint(apiAuthenticationEntryPoint)
                        .accessDeniedHandler(apiAccessDeniedHandler)
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
                .addFilterBefore(traceIdFilter, SecurityContextHolderFilter.class);
        return http.build();
    }

    /**
     * @description: JWT 解码器，使用 HMAC 密钥校验 Resource Server Bearer Token
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: 密钥必须与 JwtEncoder 使用同一配置
     */
    @Bean
    public JwtDecoder jwtDecoder(AuthProperties authProperties) {
        return NimbusJwtDecoder.withSecretKey(secretKey(authProperties))
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    /**
     * @description: JWT 编码器，供 dtools-auth 签发 HMAC Access Token
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: 生产环境必须通过配置注入高强度密钥
     */
    @Bean
    public JwtEncoder jwtEncoder(AuthProperties authProperties) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(secretKey(authProperties)));
    }

    /**
     * @description: JWT 权限转换器，将 permissions claim 转换为 Spring Security Authority
     * @author: yesterday'jam
     * @date: 2026/06/08
     * @注意: 权限码不添加前缀，便于 @PreAuthorize 使用稳定权限码
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Object permissionsClaim = jwt.getClaims().get("permissions");
            if (!(permissionsClaim instanceof Collection<?> permissions)) {
                return List.of();
            }
            List<GrantedAuthority> authorities = permissions.stream()
                    .map(String::valueOf)
                    .<GrantedAuthority>map(SimpleGrantedAuthority::new)
                    .toList();
            return authorities;
        });
        return converter;
    }

    /**
     * @description: 配置本地前端开发环境允许跨域访问后端 API
     * @author: yesterday'jam
     * @date: 2026/06/09
     * @注意: CORS 必须接入 Spring Security 过滤链，否则浏览器预检会在安全层被拒绝
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:5173",
                "http://127.0.0.1:5173"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Content-Type", "Authorization", "X-Trace-Id"));
        configuration.setExposedHeaders(List.of("X-Trace-Id"));
        configuration.setAllowCredentials(false);
        configuration.setMaxAge(1800L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }

    private SecretKey secretKey(AuthProperties authProperties) {
        return new SecretKeySpec(authProperties.getJwtSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }
}
