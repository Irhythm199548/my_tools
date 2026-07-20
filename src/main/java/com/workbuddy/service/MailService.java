package com.workbuddy.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * 邮件发送：注册 / 找回密码 验证码。
 * - 配置了 mytools.mail.host 时真实发送；
 * - 未配置（开发模式）则仅打印到服务端控制台，便于本机调试。
 */
@Slf4j
@Service
public class MailService {

    private final JavaMailSender mailSender;

    @Value("${mytools.mail.host:}")
    private String host;

    @Value("${mytools.mail.from:}")
    private String from;

    @Value("${mytools.mail.username:}")
    private String username;

    @Value("${mytools.mail.password:}")
    private String password;

    /**
     * 解析发件人地址。
     * 163/QQ 等服务商要求 From 必须与登录账号(authorized user)完全一致，
     * 因此 from 留空或仍为占位符时，自动回退为 username，避免 553 错误。
     */
    private String resolveFrom() {
        if (from != null && !from.trim().isEmpty() && !"my-tools@localhost".equals(from.trim())) {
            return from.trim();
        }
        return (username != null && !username.trim().isEmpty()) ? username.trim() : "my-tools@localhost";
    }

    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /** 是否处于「开发模式」（未配置 SMTP host 或 password 为空，不真实发信） */
    public boolean isDevMode() {
        return isBlank(host) || isBlank(password);
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    /** 发送验证码邮件 */
    public void sendVerificationCode(String to, String code, String purpose) {
        if (isDevMode()) {
            log.warn("========== [开发模式·邮件未真实发送] ==========");
            log.warn("[邮件验证码] 收件人: {}  用途: {}  验证码: {}", to, purpose, code);
            log.warn("（配置 mytools.mail.host/username/password 后可真实发信）");
            log.warn("==============================================");
            return;
        }
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(resolveFrom());
            msg.setTo(to);
            msg.setSubject("my-tools 验证码");
            msg.setText("您正在" + purpose + "，验证码为：" + code + "，5 分钟内有效。如非本人操作请忽略。");
            mailSender.send(msg);
            log.info("[邮件] 已向 {} 发送验证码邮件（用途: {}）", to, purpose);
        } catch (Exception e) {
            log.error("[邮件] 发送失败 to={} purpose={}", to, purpose, e);
            throw new RuntimeException("邮件发送失败，请稍后重试或联系管理员");
        }
    }
}
