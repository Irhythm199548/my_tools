package com.workbuddy.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

/**
 * 安全配置：公网暴露下的鉴权网关（多账号体系）。
 * - 账号来自数据库（UserDetailsServiceImpl），密码 BCrypt 加密
 * - 表单登录 + 登出，开启 CSRF（前端通过 meta 标签注入令牌）
 * - 公开：登录/登出/错误页、注册、发码、验证码校验、找回密码、上传的静态资源
 * - 安全响应头：防点击劫持、防 MIME 嗅探、Referrer 策略、HSTS、基础 CSP
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeRequests(auth -> auth
                .antMatchers(
                    "/login", "/logout", "/error",
                    "/register", "/forgot",
                    "/api/account/register",
                    "/api/account/send-code",
                    "/api/account/verify-code",
                    "/api/account/forgot/**",
                    "/uploads/**",
                    "/.well-known/**"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            )
            .headers(headers -> headers
                .frameOptions(frame -> frame.deny())
                .referrerPolicy(referrer -> referrer
                    .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER))
                .httpStrictTransportSecurity(hsts -> hsts
                    .includeSubDomains(true)
                    .maxAgeInSeconds(31536000))
                .contentSecurityPolicy(csp -> csp
                    .policyDirectives(
                        "default-src 'self'; " +
                        "script-src 'self' https://cdn.jsdelivr.net 'unsafe-inline'; " +
                        "style-src 'self' https://cdn.jsdelivr.net 'unsafe-inline'; " +
                        "img-src 'self' data: https://cdn.jsdelivr.net; " +
                        "font-src 'self' https://cdn.jsdelivr.net; " +
                        "connect-src 'self'; " +
                        "frame-ancestors 'none'")
                )
                .contentTypeOptions()
            );
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
