package com.workbuddy.controller;

import com.workbuddy.common.ApiResponse;
import com.workbuddy.service.AuxService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 辅助工具 REST API
 */
@RestController
@RequestMapping("/api/aux")
@RequiredArgsConstructor
public class AuxController {

    private final AuxService auxService;

    @GetMapping("/uuid")
    public ApiResponse<String> uuid(@RequestParam(defaultValue = "false") boolean noDash) {
        return ApiResponse.success(auxService.uuid(noDash));
    }

    @GetMapping("/uuid/batch")
    public ApiResponse<List<String>> uuidBatch(@RequestParam(defaultValue = "10") int count) {
        return ApiResponse.success(auxService.uuidBatch(count));
    }

    @PostMapping("/qrcode/generate")
    public ApiResponse<String> qrcodeGenerate(@RequestParam String content,
                                              @RequestParam(required = false) Integer width,
                                              @RequestParam(required = false) Integer height) {
        return ApiResponse.success(auxService.qrcodeGenerate(content, width, height));
    }

    @PostMapping("/qrcode/parse")
    public ApiResponse<String> qrcodeParse(@RequestParam("file") MultipartFile file) {
        return ApiResponse.success(auxService.qrcodeParse(file));
    }
}
