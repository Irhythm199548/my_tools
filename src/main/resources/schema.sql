-- ============================================================
-- my tools 个人工作台 - MySQL 8.0 建表脚本
-- 字符集：utf8mb4 / 排序规则：utf8mb4_general_ci
-- 说明：项目已开启 spring.jpa.hibernate.ddl-auto=update，
--       启动时 JPA 会自动建表；本文件供手动建库或评审参考。
-- ============================================================

CREATE DATABASE IF NOT EXISTS mytools
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_general_ci;

USE mytools;

-- 待办事项
CREATE TABLE IF NOT EXISTS todo (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    title       VARCHAR(200) NOT NULL,
    description VARCHAR(1000),
    priority    VARCHAR(10)  NOT NULL DEFAULT 'MEDIUM',
    completed   BIT(1)       NOT NULL DEFAULT 0,
    due_date    DATETIME,
    created_at  DATETIME,
    updated_at  DATETIME,
    PRIMARY KEY (id),
    INDEX idx_todo_completed (completed),
    INDEX idx_todo_priority (priority)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 番茄钟记录
CREATE TABLE IF NOT EXISTS pomodoro (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    tag        VARCHAR(100),
    duration   INT          NOT NULL DEFAULT 0,
    start_time DATETIME,
    end_time   DATETIME,
    note       VARCHAR(500),
    PRIMARY KEY (id),
    INDEX idx_pomodoro_start (start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 用户偏好（键值对）
CREATE TABLE IF NOT EXISTS user_preference (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    pref_key    VARCHAR(100) NOT NULL,
    pref_value  VARCHAR(500),
    category    VARCHAR(100),
    description VARCHAR(200),
    updated_at  DATETIME,
    PRIMARY KEY (id),
    UNIQUE KEY uk_pref_key (pref_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 操作日志（工具使用次数统计）
CREATE TABLE IF NOT EXISTS operation_log (
    id            BIGINT      NOT NULL AUTO_INCREMENT,
    user_id       BIGINT      NOT NULL DEFAULT 0,
    tool_name     VARCHAR(100) NOT NULL,
    tool_category VARCHAR(50),
    use_count     BIGINT      NOT NULL DEFAULT 0,
    last_used_at  DATETIME,
    PRIMARY KEY (id),
    -- 按用户隔离：同一用户同一工具只统计一行（与 OperationLog 实体 @UniqueConstraint 一致）
    UNIQUE KEY uk_user_tool (user_id, tool_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
