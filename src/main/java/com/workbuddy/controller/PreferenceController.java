package com.workbuddy.controller;

import com.workbuddy.common.ApiResponse;
import com.workbuddy.service.PreferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户偏好 REST API
 */
@RestController
@RequestMapping("/api/preferences")
@RequiredArgsConstructor
public class PreferenceController {

    private final PreferenceService preferenceService;

    @GetMapping
    public ApiResponse<Map<String, String>> all() {
        return ApiResponse.success(preferenceService.all());
    }

    @GetMapping("/{key}")
    public ApiResponse<String> get(@PathVariable String key) {
        return ApiResponse.success(preferenceService.get(key));
    }

    @PostMapping
    public ApiResponse<Void> set(@RequestParam String key,
                                 @RequestParam String value,
                                 @RequestParam(required = false) String category,
                                 @RequestParam(required = false) String description) {
        preferenceService.set(key, value, category, description);
        return ApiResponse.success("保存成功", null);
    }
}
