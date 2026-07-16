package com.workbuddy.controller;

import com.workbuddy.common.ApiResponse;
import com.workbuddy.service.EncodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 编码工具 REST API
 */
@RestController
@RequestMapping("/api/encode")
@RequiredArgsConstructor
public class EncodeController {

    private final EncodeService encodeService;

    @PostMapping("/base64")
    public ApiResponse<String> base64(@RequestParam String action, @RequestParam String text) {
        return ApiResponse.success(encodeService.base64(action, text));
    }

    @PostMapping("/url")
    public ApiResponse<String> url(@RequestParam String action, @RequestParam String text) {
        return ApiResponse.success(encodeService.url(action, text));
    }

    @PostMapping("/md5")
    public ApiResponse<String> md5(@RequestParam String text) {
        return ApiResponse.success(encodeService.md5(text));
    }

    @PostMapping("/sha")
    public ApiResponse<String> sha(@RequestParam(defaultValue = "sha256") String algorithm,
                                   @RequestParam String text) {
        return ApiResponse.success(encodeService.sha(algorithm, text));
    }

    @PostMapping("/aes")
    public ApiResponse<String> aes(@RequestParam String action,
                                   @RequestParam String content,
                                   @RequestParam String secret) {
        return ApiResponse.success(encodeService.aes(action, content, secret));
    }
}
