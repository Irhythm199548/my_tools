package com.workbuddy.common;

import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 全局异常处理：将异常统一包装为 ApiResponse
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);


    /** 参数校验失败 (JSR-303) */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ApiResponse<Void> handleValid(Exception e) {
        String msg;
        if (e instanceof MethodArgumentNotValidException) {
            msg = ((MethodArgumentNotValidException) e).getBindingResult().getFieldErrors().stream()
                    .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                    .collect(Collectors.joining("; "));
        } else {
            msg = ((BindException) e).getBindingResult().getFieldErrors().stream()
                    .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                    .collect(Collectors.joining("; "));
        }
        return ApiResponse.error(ResultCode.VALIDATE_FAILED.getCode(), msg);
    }

    /** 文件过大 */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ApiResponse<Void> handleMaxUpload(MaxUploadSizeExceededException e) {
        return ApiResponse.error("上传文件超过大小限制");
    }

    /** 业务/运行时异常统一兜底：记录详细日志，但仅向客户端返回通用信息，避免泄露内部细节 */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleException(HttpServletRequest request, Exception e) {
        String traceId = UUID.randomUUID().toString().substring(0, 8);
        log.error("[traceId={}] 未处理异常 uri={}", traceId, request.getRequestURI(), e);
        return ApiResponse.error(ResultCode.ERROR.getCode(), "服务器内部错误，请稍后重试（追踪码：" + traceId + "）");
    }
}
