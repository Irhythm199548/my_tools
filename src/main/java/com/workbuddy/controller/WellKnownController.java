package com.workbuddy.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 静默处理 Chrome DevTools / 其他客户端对 /.well-known/** 的自动探测
 * （如 /.well-known/appspecific/com.chrome.devtools.json）。
 * 直接返回 404 空响应，避免触发自定义错误页（error.html）造成「服务器开小差」的误报。
 */
@RestController
@RequestMapping("/.well-known")
public class WellKnownController {

    @GetMapping("/**")
    public ResponseEntity<Void> handleProbe() {
        return ResponseEntity.notFound().build();
    }
}
