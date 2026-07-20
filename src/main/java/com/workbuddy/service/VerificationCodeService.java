package com.workbuddy.service;

import com.workbuddy.common.BizException;
import com.workbuddy.entity.VerificationCode;
import com.workbuddy.repository.UserRepository;
import com.workbuddy.repository.VerificationCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Date;

/**
 * 邮箱验证码：生成、发送、校验。
 */
@Service
@RequiredArgsConstructor
public class VerificationCodeService {

    private final VerificationCodeRepository codeRepository;
    private final UserRepository userRepository;
    private final MailService mailService;

    @Value("${mytools.mail.code-ttl:300}")
    private int ttlSeconds;

    private static final SecureRandom RND = new SecureRandom();

    /** 生成 6 位数字验证码 */
    private String generateCode() {
        int n = 100000 + RND.nextInt(900000);
        return String.valueOf(n);
    }

    /** 发送验证码（注册/找回）。REGISTER 时校验邮箱未被占用。 */
    @Transactional
    public void send(String email, VerificationCode.Type type) {
        if (email == null || !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new BizException("邮箱格式不正确");
        }
        if (type == VerificationCode.Type.REGISTER && userRepository.existsByEmail(email)) {
            throw new BizException("该邮箱已被注册");
        }
        // 作废该邮箱该类型的旧码
        codeRepository.findByEmailAndTypeOrderByCreatedAtDesc(email, type)
                .forEach(c -> {
                    c.setUsed(true);
                    codeRepository.save(c);
                });
        // 清理过期码
        codeRepository.deleteByExpiresAtBefore(new Date());

        VerificationCode vc = new VerificationCode();
        vc.setEmail(email);
        vc.setCode(generateCode());
        vc.setType(type);
        vc.setExpiresAt(new Date(System.currentTimeMillis() + (long) ttlSeconds * 1000));
        vc.setUsed(false);
        codeRepository.save(vc);

        String purpose = type == VerificationCode.Type.REGISTER ? "注册账号" : "重置密码";
        mailService.sendVerificationCode(email, vc.getCode(), purpose);
    }

    /** 校验验证码，通过则置为已用。失败抛出 BizException。 */
    @Transactional
    public void verify(String email, String code, VerificationCode.Type type) {
        VerificationCode vc = codeRepository.findTopByEmailAndTypeAndUsedFalseOrderByCreatedAtDesc(email, type)
                .orElseThrow(() -> new BizException("验证码不存在或已使用，请重新获取"));
        if (vc.getExpiresAt().before(new Date())) {
            throw new BizException("验证码已过期，请重新获取");
        }
        if (!vc.getCode().equals(code)) {
            throw new BizException("验证码错误");
        }
        vc.setUsed(true);
        codeRepository.save(vc);
    }
}
