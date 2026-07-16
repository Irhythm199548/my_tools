package com.workbuddy.controller;

import com.workbuddy.common.ApiResponse;
import com.workbuddy.entity.OperationLog;
import com.workbuddy.service.OperationLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 操作日志 / 使用统计 REST API
 */
@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
public class LogController {

    private final OperationLogService logService;

    @GetMapping("/stats")
    public ApiResponse<List<OperationLog>> stats() {
        return ApiResponse.success(logService.stats());
    }
}
