package com.workbuddy.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

/**
 * 邮箱验证码：用于注册 / 找回密码。
 * 同一邮箱同一类型只保留最新一条有效记录（发送时作废旧码）。
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "verification_code", indexes = {
        @Index(name = "idx_vc_email_type", columnList = "email,type"),
        @Index(name = "idx_vc_expire", columnList = "expires_at")
})
public class VerificationCode {

    public enum Type {
        REGISTER, RESET
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String email;

    @Column(nullable = false, length = 10)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Type type;

    @Column(name = "expires_at", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date expiresAt;

    @Column(nullable = false)
    private Boolean used = false;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = new Date();
        if (used == null) used = false;
    }
}
