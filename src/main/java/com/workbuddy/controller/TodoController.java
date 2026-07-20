package com.workbuddy.controller;

import com.workbuddy.common.ApiResponse;
import com.workbuddy.config.CurrentUser;
import com.workbuddy.entity.Todo;
import com.workbuddy.service.TodoService;
import com.workbuddy.utils.TimeParseUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 待办事项 REST API（按当前登录用户隔离）
 */
@RestController
@RequestMapping("/api/todos")
@RequiredArgsConstructor
public class TodoController {

    private final TodoService todoService;
    private final CurrentUser currentUser;

    @GetMapping
    public ApiResponse<List<Todo>> list(@RequestParam(required = false) Boolean completed) {
        return ApiResponse.success(todoService.list(currentUser.getUserId(), completed));
    }

    @GetMapping("/{id}")
    public ApiResponse<Todo> get(@PathVariable Long id) {
        return ApiResponse.success(todoService.get(currentUser.getUserId(), id));
    }

    @PostMapping
    public ApiResponse<Todo> create(@RequestBody Todo todo) {
        return ApiResponse.success("创建成功", todoService.create(currentUser.getUserId(), todo));
    }

    @PutMapping("/{id}")
    public ApiResponse<Todo> update(@PathVariable Long id, @RequestBody Todo todo) {
        return ApiResponse.success("更新成功", todoService.update(currentUser.getUserId(), id, todo));
    }

    @PatchMapping("/{id}/toggle")
    public ApiResponse<Todo> toggle(@PathVariable Long id) {
        return ApiResponse.success(todoService.toggle(currentUser.getUserId(), id));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        todoService.delete(currentUser.getUserId(), id);
        return ApiResponse.success("删除成功", null);
    }

    /**
     * 根据标题文本自动识别时间（年月日时分秒）。
     * 请求体：{"text": "明天晚上拿快递"}
     * 返回：{"matched": true, "dueDate": "2026-07-21 20:00:00"}（未识别 matched=false, dueDate=null）
     */
    @PostMapping("/parse-time")
    public ApiResponse<Map<String, Object>> parseTime(@RequestBody(required = false) Map<String, String> body) {
        String text = (body == null ? null : body.get("text"));
        Optional<String> parsed = TimeParseUtils.parseToText(text);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("matched", parsed.isPresent());
        data.put("dueDate", parsed.orElse(null));
        return ApiResponse.success(data);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ApiResponse<Void> notFound(EntityNotFoundException e) {
        return ApiResponse.error(404, e.getMessage());
    }
}
