package com.workbuddy.controller;

import com.workbuddy.common.ApiResponse;
import com.workbuddy.common.ResultCode;
import com.workbuddy.service.NetworkService;
import com.workbuddy.utils.NetworkUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 网络与安全 REST API
 */
@RestController
@RequestMapping("/api/net")
@RequiredArgsConstructor
public class NetworkController {

    private final NetworkService networkService;

    @PostMapping("/ip")
    public ApiResponse<Map<String, Object>> ip(@RequestParam String ip) {
        return ApiResponse.success(networkService.ipLocation(ip));
    }

    @PostMapping("/ua")
    public ApiResponse<Map<String, Object>> ua(@RequestParam(required = false) String ua) {
        return ApiResponse.success(networkService.parseUserAgent(ua == null ? "" : ua));
    }

    @PostMapping("/headers")
    public ApiResponse<Map<String, Object>> headers(@RequestParam(required = false) String raw) {
        return ApiResponse.success(networkService.parseHeaders(raw == null ? "" : raw));
    }

    @PostMapping("/password")
    public ApiResponse<String> password(@RequestParam(defaultValue = "16") int length,
                                        @RequestParam(defaultValue = "true") boolean upper,
                                        @RequestParam(defaultValue = "true") boolean lower,
                                        @RequestParam(defaultValue = "true") boolean digit,
                                        @RequestParam(defaultValue = "true") boolean special) {
        return ApiResponse.success(networkService.generatePassword(length, upper, lower, digit, special));
    }

    @PostMapping("/mock")
    public ApiResponse<Map<String, Object>> mock(@RequestParam(defaultValue = "en") String locale) {
        return ApiResponse.success(networkService.mockPerson(locale));
    }

    @PostMapping("/portscan")
    public ApiResponse<Map<String, Object>> portScan(@RequestParam(defaultValue = "127.0.0.1") String host,
                                                     @RequestParam(defaultValue = "1") int from,
                                                     @RequestParam(defaultValue = "1024") int to) {
        if (!NetworkUtils.isScannableHost(host)) {
            return ApiResponse.error(ResultCode.FORBIDDEN.getCode(),
                    "出于安全考虑，仅允许扫描本机或内网地址（127.0.0.1 / localhost / 10.x / 172.16-31.x / 192.168.x / 169.254.x）。");
        }
        return ApiResponse.success(networkService.portScan(host, from, to));
    }
}
