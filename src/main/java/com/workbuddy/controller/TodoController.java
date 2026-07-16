package com.workbuddy.controller;

import com.workbuddy.common.ApiResponse;
import com.workbuddy.entity.Todo;
import com.workbuddy.service.TodoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import java.util.List;

/**
 * 待办事项 REST API
 */
@RestController
@RequestMapping("/api/todos")
@RequiredArgsConstructor
public class TodoController {

    private final TodoService todoService;

    @GetMapping
    public ApiResponse<List<Todo>> list(@RequestParam(required = false) Boolean completed) {
        return ApiResponse.success(todoService.list(completed));
    }

    @GetMapping("/{id}")
    public ApiResponse<Todo> get(@PathVariable Long id) {
        return ApiResponse.success(todoService.get(id));
    }

    @PostMapping
    public ApiResponse<Todo> create(@RequestBody Todo todo) {
        return ApiResponse.success("创建成功", todoService.create(todo));
    }

    @PutMapping("/{id}")
    public ApiResponse<Todo> update(@PathVariable Long id, @RequestBody Todo todo) {
        return ApiResponse.success("更新成功", todoService.update(id, todo));
    }

    @PatchMapping("/{id}/toggle")
    public ApiResponse<Todo> toggle(@PathVariable Long id) {
        return ApiResponse.success(todoService.toggle(id));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        todoService.delete(id);
        return ApiResponse.success("删除成功", null);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ApiResponse<Void> notFound(EntityNotFoundException e) {
        return ApiResponse.error(404, e.getMessage());
    }
}
