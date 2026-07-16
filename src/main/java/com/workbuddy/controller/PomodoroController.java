package com.workbuddy.controller;

import com.workbuddy.common.ApiResponse;
import com.workbuddy.entity.Pomodoro;
import com.workbuddy.service.PomodoroService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 番茄钟 REST API
 */
@RestController
@RequestMapping("/api/pomodoros")
@RequiredArgsConstructor
public class PomodoroController {

    private final PomodoroService pomodoroService;

    @GetMapping
    public ApiResponse<List<Pomodoro>> recent() {
        return ApiResponse.success(pomodoroService.recent());
    }

    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> stats() {
        Map<String, Object> m = new HashMap<>();
        m.put("totalSeconds", pomodoroService.totalSeconds());
        m.put("todaySeconds", pomodoroService.todaySeconds());
        m.put("totalCount", pomodoroService.totalCount());
        m.put("todayCount", pomodoroService.todayCount());
        return ApiResponse.success(m);
    }

    @PostMapping
    public ApiResponse<Pomodoro> save(@RequestBody Pomodoro pomodoro) {
        return ApiResponse.success("保存成功", pomodoroService.save(pomodoro));
    }
}
