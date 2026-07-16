package com.workbuddy.controller;

import com.workbuddy.common.ApiResponse;
import com.workbuddy.service.CronService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Cron 表达式 REST API（合法性校验 + 解释）
 */
@RestController
@RequestMapping("/api/cron")
@RequiredArgsConstructor
public class CronController {

    private final CronService cronService;

    @PostMapping("/validate")
    public ApiResponse<Map<String, Object>> validate(@RequestParam String expression) {
        return ApiResponse.success(cronService.validate(expression));
    }
}
