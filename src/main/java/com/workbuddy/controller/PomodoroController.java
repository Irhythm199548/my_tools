package com.workbuddy.controller;

import com.workbuddy.common.ApiResponse;
import com.workbuddy.config.CurrentUser;
import com.workbuddy.entity.Pomodoro;
import com.workbuddy.service.PomodoroService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 番茄钟 REST API（按当前登录用户隔离）
 */
@RestController
@RequestMapping("/api/pomodoros")
@RequiredArgsConstructor
public class PomodoroController {

    private final PomodoroService pomodoroService;
    private final CurrentUser currentUser;

    @GetMapping
    public ApiResponse<List<Pomodoro>> recent() {
        return ApiResponse.success(pomodoroService.recent(currentUser.getUserId()));
    }

    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> stats() {
        Long uid = currentUser.getUserId();
        Map<String, Object> m = new HashMap<>();
        m.put("totalSeconds", pomodoroService.totalSeconds(uid));
        m.put("todaySeconds", pomodoroService.todaySeconds(uid));
        m.put("totalCount", pomodoroService.totalCount(uid));
        m.put("todayCount", pomodoroService.todayCount(uid));
        return ApiResponse.success(m);
    }

    @PostMapping
    public ApiResponse<Pomodoro> save(@RequestBody Pomodoro pomodoro) {
        return ApiResponse.success("保存成功", pomodoroService.save(currentUser.getUserId(), pomodoro));
    }
}
