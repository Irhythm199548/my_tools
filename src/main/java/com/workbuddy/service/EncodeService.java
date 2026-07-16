package com.workbuddy.service;

import com.workbuddy.utils.EncodeUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 编码工具 Service（无状态）
 * 封装 Base64 / URL / MD5 / SHA / AES，并记录使用次数。
 */
@Service
@RequiredArgsConstructor
public class EncodeService {

    private final OperationLogService logService;

    public String base64(String action, String text) {
        logService.record("encode.base64", "编码工具");
        if ("decode".equalsIgnoreCase(action)) {
            return EncodeUtils.base64Decode(text);
        }
        return EncodeUtils.base64Encode(text);
    }

    public String url(String action, String text) {
        logService.record("encode.url", "编码工具");
        if ("decode".equalsIgnoreCase(action)) {
            return EncodeUtils.urlDecode(text);
        }
        return EncodeUtils.urlEncode(text);
    }

    public String md5(String text) {
        logService.record("encode.md5", "编码工具");
        return EncodeUtils.md5(text);
    }

    public String sha(String algorithm, String text) {
        logService.record("encode.sha", "编码工具");
        return EncodeUtils.sha(text, algorithm);
    }

    public String aes(String action, String content, String secret) {
        logService.record("encode.aes", "编码工具");
        if ("decrypt".equalsIgnoreCase(action)) {
            return EncodeUtils.aesDecrypt(content, secret);
        }
        return EncodeUtils.aesEncrypt(content, secret);
    }
}
