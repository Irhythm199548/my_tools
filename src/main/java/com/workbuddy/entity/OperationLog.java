package com.workbuddy.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

/**
 * 操作日志：记录各工具的使用次数（用于统计）
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "operation_log", uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_tool", columnNames = {"user_id", "tool_name"})
})
public class OperationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 所属用户（多账号隔离） */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 工具名称 */
    @Column(name = "tool_name", nullable = false, length = 100)
    private String toolName;

    /** 工具分类 */
    @Column(name = "tool_category", length = 50)
    private String toolCategory;

    /** 累计使用次数 */
    @Column(nullable = false)
    private Long useCount = 0L;

    /** 最近使用时间 */
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUsedAt;

    @PrePersist
    public void prePersist() {
        if (useCount == null) useCount = 0L;
        if (lastUsedAt == null) lastUsedAt = new Date();
    }
}
