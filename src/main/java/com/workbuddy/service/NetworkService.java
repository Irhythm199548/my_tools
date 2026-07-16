package com.workbuddy.service;

import com.workbuddy.utils.NetworkUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 网络与安全 Service（无状态）
 * 封装 IP 归属地、UA/Header 解析、随机密码、假数据、端口扫描。
 */
@Service
@RequiredArgsConstructor
public class NetworkService {

    private final OperationLogService logService;

    @Value("${mytools.portscan.threads:64}")
    private int scanThreads;
    @Value("${mytools.portscan.timeout:300}")
    private int scanTimeout;

    public Map<String, Object> ipLocation(String ip) {
        logService.record("net.ip", "网络与安全");
        return NetworkUtils.ipLocation(ip);
    }

    public Map<String, Object> parseUserAgent(String ua) {
        logService.record("net.ua", "网络与安全");
        return NetworkUtils.parseUserAgent(ua);
    }

    public Map<String, Object> parseHeaders(String raw) {
        logService.record("net.headers", "网络与安全");
        return NetworkUtils.parseHeaders(raw);
    }

    public String generatePassword(int length, boolean upper, boolean lower, boolean digit, boolean special) {
        logService.record("net.password", "网络与安全");
        return NetworkUtils.generatePassword(length, upper, lower, digit, special);
    }

    public Map<String, Object> mockPerson(String locale) {
        logService.record("net.mock", "网络与安全");
        return NetworkUtils.mockPerson(locale);
    }

    public Map<String, Object> portScan(String host, int from, int to) {
        logService.record("net.portscan", "网络与安全");
        return NetworkUtils.portScan(host, from, to, scanThreads, scanTimeout);
    }

    public boolean isValidIpv4(String ip) {
        return NetworkUtils.isValidIpv4(ip);
    }
}
