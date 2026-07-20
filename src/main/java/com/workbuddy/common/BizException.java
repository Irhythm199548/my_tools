package com.workbuddy.common;

/**
 * 业务异常：携带可安全回显给客户端的提示信息（区别于内部系统异常）。
 * 由 GlobalExceptionHandler 以 VALIDATE_FAILED(400) 返回 message。
 */
public class BizException extends RuntimeException {

    public BizException(String message) {
        super(message);
    }

    public BizException(String message, Throwable cause) {
        super(message, cause);
    }
}
