package com.workbuddy.common;

import lombok.Getter;

/**
 * 响应码常量
 */
@Getter
public enum ResultCode {

    SUCCESS(0, "success"),
    FAILED(1, "操作失败"),
    VALIDATE_FAILED(400, "参数校验失败"),
    UNAUTHORIZED(401, "未认证"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),
    ERROR(500, "服务器内部错误");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
