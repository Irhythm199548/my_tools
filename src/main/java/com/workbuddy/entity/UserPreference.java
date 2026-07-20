package com.workbuddy.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

/**
 * 用户偏好（键值对存储：主题、默认城市等）
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "user_preference", uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_pref", columnNames = {"user_id", "pref_key"})
})
public class UserPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 所属用户（多账号隔离） */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "pref_key", nullable = false, length = 100)
    private String key;

    @Column(name = "pref_value", length = 500)
    private String value;

    @Column(length = 100)
    private String category;

    @Column(length = 200)
    private String description;

    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    @PreUpdate
    @PrePersist
    public void touch() {
        updatedAt = new Date();
    }
}
