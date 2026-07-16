package com.workbuddy.controller;

import com.workbuddy.common.ApiResponse;
import com.workbuddy.service.TextService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 文本处理 REST API
 */
@RestController
@RequestMapping("/api/text")
@RequiredArgsConstructor
public class TextController {

    private final TextService textService;

    @PostMapping("/diff")
    public ApiResponse<String> diff(@RequestParam String text1, @RequestParam String text2) {
        return ApiResponse.success(textService.diff(text1, text2));
    }

    @PostMapping("/regex")
    public ApiResponse<Map<String, Object>> regex(@RequestParam String pattern, @RequestParam String text) {
        return ApiResponse.success(textService.regexTest(pattern, text));
    }

    @PostMapping("/case")
    public ApiResponse<String> convertCase(@RequestParam String target, @RequestParam String input) {
        return ApiResponse.success(textService.caseConvert(target, input));
    }

    @PostMapping("/wordcount")
    public ApiResponse<Map<String, Object>> wordCount(@RequestParam String text) {
        return ApiResponse.success(textService.wordCount(text));
    }

    @PostMapping("/convert")
    public ApiResponse<String> convert(@RequestParam String from,
                                       @RequestParam String to,
                                       @RequestParam String content) {
        return ApiResponse.success(textService.convert(from, to, content));
    }
}
