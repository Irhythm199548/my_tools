package com.workbuddy.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

/**
 * 邮件发送器配置。
 * 读取自定义前缀 mytools.mail.*（而非 Spring Boot 默认的 spring.mail.*），
 * 显式创建 JavaMailSender  bean，确保无论是否配置 SMTP，MailService 都能注入成功：
 * - host 为空（开发模式）：bean 存在但不会被真正调用，验证码仅打印控制台；
 * - host 已配置：按 SMTP 真实发信。
 */
@Configuration
public class MailConfig {

    @Value("${mytools.mail.host:}")
    private String host;

    @Value("${mytools.mail.port:465}")
    private String port;

    @Value("${mytools.mail.username:}")
    private String username;

    @Value("${mytools.mail.password:}")
    private String password;

    @Value("${mytools.mail.ssl:true}")
    private boolean ssl;

    private static int parsePort(String raw, int fallback) {
        if (raw == null) {
            return fallback;
        }
        String s = raw.trim();
        if (s.isEmpty()) {
            return fallback;
        }
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    @Bean
    public JavaMailSender javaMailSender() {
        int portNum = parsePort(port, 465);
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(host);
        sender.setPort(portNum);
        sender.setUsername(username);
        sender.setPassword(password);
        sender.setDefaultEncoding("UTF-8");

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.ssl.enable", String.valueOf(ssl));
        if (ssl) {
            props.put("mail.smtp.socketFactory.port", String.valueOf(portNum));
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.socketFactory.fallback", "false");
        } else {
            props.put("mail.smtp.starttls.enable", "true");
        }
        props.put("mail.debug", "false");
        sender.setJavaMailProperties(props);
        return sender;
    }
}
