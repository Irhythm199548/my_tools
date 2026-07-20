package com.workbuddy.config;

import com.workbuddy.entity.User;
import com.workbuddy.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 * 首次启动且库中无用户时，播种一个默认管理员账号（来自 mytools.admin.* 配置）。
 * 若配置中未设置密码，则随机生成并打印到日志，确保管理员一定能登入。
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${mytools.admin.username:admin}")
    private String adminUsername;

    @Value("${mytools.admin.email:admin@localhost}")
    private String adminEmail;

    @Value("${mytools.admin.password:}")
    private String adminPassword;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }
        String pw = (adminPassword == null || adminPassword.isEmpty())
                ? generatePassword() : adminPassword;
        User admin = new User();
        admin.setUsername(adminUsername);
        admin.setEmail(adminEmail);
        admin.setPassword(passwordEncoder.encode(pw));
        admin.setNickname("管理员");
        admin.setRole(User.Role.ADMIN);
        admin.setEnabled(true);
        admin.setEmailVerified(true);
        userRepository.save(admin);

        if (adminPassword == null || adminPassword.isEmpty()) {
            log.warn("==================================================================");
            log.warn("[初始化] 已创建默认管理员账号：");
            log.warn("[初始化]   用户名: {}    密码: {}", adminUsername, pw);
            log.warn("[初始化] 请尽快登录后在「个人中心」修改密码，或删除后自行注册。");
            log.warn("==================================================================");
        } else {
            log.info("[初始化] 已使用配置创建默认管理员账号 '{}'。", adminUsername);
        }
    }

    private String generatePassword() {
        final String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789!@#$%";
        SecureRandom rnd = new SecureRandom();
        StringBuilder sb = new StringBuilder(16);
        for (int i = 0; i < 16; i++) sb.append(chars.charAt(rnd.nextInt(chars.length())));
        return sb.toString();
    }
}
