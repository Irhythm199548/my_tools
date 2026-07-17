package com.workbuddy.controller;

import com.workbuddy.common.ApiResponse;
import com.workbuddy.service.OperationLogService;
import com.workbuddy.service.PomodoroService;
import com.workbuddy.service.TodoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 页面路由：渲染 Thymeleaf 模板
 */
@Controller
@RequiredArgsConstructor
public class PageController {

    private final TodoService todoService;
    private final PomodoroService pomodoroService;
    private final OperationLogService logService;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("todoCount", todoService.countUncompleted());
        model.addAttribute("focusToday", pomodoroService.todaySeconds());
        model.addAttribute("toolUsage", logService.stats());
        return "index";
    }

    @GetMapping("/todo")
    public String todo() { return "todo"; }

    @GetMapping("/pomodoro")
    public String pomodoro() { return "pomodoro"; }

    @GetMapping("/encode")
    public String encode() { return "encode"; }

    @GetMapping("/text")
    public String text() { return "text"; }

    @GetMapping("/convert")
    public String convert() { return "convert"; }

    @GetMapping("/network")
    public String network() { return "network"; }

    @GetMapping("/auxs")
    public String aux() { return "auxs"; }

    @GetMapping("/cron")
    public String cron() { return "cron"; }

    @GetMapping("/logs")
    public String logs(Model model) {
        model.addAttribute("stats", logService.stats());
        return "logs";
    }
}
