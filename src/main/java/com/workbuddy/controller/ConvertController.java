package com.workbuddy.controller;

import com.workbuddy.common.ApiResponse;
import com.workbuddy.service.ConvertService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 格式转换 REST API
 */
@RestController
@RequestMapping("/api/convert")
@RequiredArgsConstructor
public class ConvertController {

    private final ConvertService convertService;

    @GetMapping("/timestamp/now")
    public ApiResponse<Map<String, Object>> now(@RequestParam(defaultValue = "Asia/Shanghai") String timeZone) {
        return ApiResponse.success(convertService.now(timeZone));
    }

    @PostMapping("/timestamp/toDate")
    public ApiResponse<Map<String, Object>> toDate(@RequestParam long timestamp,
                                                   @RequestParam(defaultValue = "false") boolean millis,
                                                   @RequestParam(defaultValue = "Asia/Shanghai") String timeZone) {
        return ApiResponse.success(convertService.timestampToDate(timestamp, millis, timeZone));
    }

    @PostMapping("/timestamp/fromDate")
    public ApiResponse<Long> fromDate(@RequestParam String dateStr,
                                      @RequestParam(defaultValue = "yyyy-MM-dd HH:mm:ss") String pattern,
                                      @RequestParam(defaultValue = "Asia/Shanghai") String timeZone) {
        return ApiResponse.success(convertService.dateToTimestamp(dateStr, pattern, timeZone));
    }

    @PostMapping("/image")
    public ApiResponse<String> imageToBase64(@RequestParam("file") MultipartFile file) {
        return ApiResponse.success(convertService.imageToBase64(file));
    }
}
