package com.workbuddy.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

/**
 * 账号（多账号体系核心实体）
 * - username / email 唯一，用于登录与找回
 * - password 存储 BCrypt 哈希
 * - nickname 必填；其余为选填个人资料
 * - enabledTools：逗号分隔的「启用工具」key，null/空表示全部启用
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "user_account", uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_username", columnNames = "username"),
        @UniqueConstraint(name = "uk_user_email", columnNames = "email")
})
public class User {

    public enum Role {
        USER, ADMIN
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(nullable = false, length = 120)
    private String email;

    @Column(nullable = false, length = 100)
    private String password;

    @Column(nullable = false, length = 50)
    private String nickname;

    /** 头像相对 URL（/uploads/avatars/xxx.png）或第三方地址 */
    @Column(length = 300)
    private String avatar;

    @Column(length = 500)
    private String bio;

    @Column(length = 30)
    private String phone;

    @Column(length = 100)
    private String location;

    @Column(length = 200)
    private String website;

    @Column(length = 100)
    private String company;

    @Temporal(TemporalType.DATE)
    private Date birthday;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Role role = Role.USER;

    @Column(nullable = false)
    private Boolean enabled = true;

    @Column(name = "email_verified", nullable = false)
    private Boolean emailVerified = false;

    /** 逗号分隔的启用工具 key；null/空 = 全部启用 */
    @Column(name = "enabled_tools", length = 500)
    private String enabledTools;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    @PrePersist
    public void prePersist() {
        Date now = new Date();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
        if (role == null) role = Role.USER;
        if (enabled == null) enabled = true;
        if (emailVerified == null) emailVerified = false;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = new Date();
    }
}
