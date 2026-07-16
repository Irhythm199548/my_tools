package com.workbuddy.entity;

import lombok.Getter;

/**
 * 待办优先级
 */
@Getter
public enum Priority {
    LOW("低"),
    MEDIUM("中"),
    HIGH("高");

    private final String label;

    Priority(String label) {
        this.label = label;
    }
}
