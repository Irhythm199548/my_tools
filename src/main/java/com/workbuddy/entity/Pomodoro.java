package com.workbuddy.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

/**
 * 番茄钟记录
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "pomodoro")
public class Pomodoro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 所属用户（多账号隔离） */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 专注标签，如 "coding"、"reading" */
    @Column(length = 100)
    private String tag;

    /** 专注时长（秒） */
    @Column(nullable = false)
    private Integer duration = 0;

    @Temporal(TemporalType.TIMESTAMP)
    private Date startTime;

    @Temporal(TemporalType.TIMESTAMP)
    private Date endTime;

    @Column(length = 500)
    private String note;

    @PrePersist
    public void prePersist() {
        Date now = new Date();
        if (startTime == null) startTime = now;
        if (endTime == null) endTime = now;
        if (duration == null) duration = 0;
    }
}
