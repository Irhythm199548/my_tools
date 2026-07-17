package com.workbuddy.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

import java.security.SecureRandom;

/**
 * 安全配置：公网暴露下的鉴权网关。
 * - 单用户（账号/密码来自 application.yml，未配置密码时启动随机生成并打印到日志）
 * - 表单登录 + 登出
 * - 开启 CSRF（前端通过 meta 标签注入令牌）
 * - 安全响应头：防点击劫持、防 MIME 嗅探、Referrer 策略、HSTS、基础 CSP
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    @Value("${mytools.security.user:admin}")
    private String secUser;

    @Value("${mytools.security.password:}")
    private String secPassword;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeRequests(auth -> auth
                .antMatchers("/login", "/logout", "/error").permitAll()
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
        // 个人工具，密码以明文配置在 application.yml（仅本机/自管服务器可读），避免 BCrypt 无法手动设置
        return NoOpPasswordEncoder.getInstance();
    }

    @Bean
    public InMemoryUserDetailsManager userDetailsService() {
        String pw = (secPassword == null || secPassword.isEmpty()) ? generatePassword() : secPassword;
        if (secPassword == null || secPassword.isEmpty()) {
            log.warn("==================================================================");
            log.warn("[安全] 未配置 mytools.security.password，已自动生成随机登录密码：");
            log.warn("[安全]   用户名: {}    密码: {}", secUser, pw);
            log.warn("[安全] 请尽快在 application.yml 设置固定密码，或妥善保存以上随机密码。");
            log.warn("==================================================================");
        } else {
            log.info("[安全] 已使用 application.yml 中配置的账号 '{}' 启用登录鉴权。", secUser);
        }
        UserDetails user = User.withUsername(secUser)
                .password(pw)
                .roles("USER")
                .build();
        return new InMemoryUserDetailsManager(user);
    }

    private String generatePassword() {
        final String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789!@#$%";
        SecureRandom rnd = new SecureRandom();
        StringBuilder sb = new StringBuilder(16);
        for (int i = 0; i < 16; i++) sb.append(chars.charAt(rnd.nextInt(chars.length())));
        return sb.toString();
    }
}
