package com.workbuddy.service;

import com.workbuddy.utils.AuxUtils;
import com.workbuddy.utils.QrcodeUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 辅助工具 Service（无状态）
 * 封装 UUID 生成、二维码生成/解析。
 */
@Service
@RequiredArgsConstructor
public class AuxService {

    private final OperationLogService logService;

    @Value("${mytools.qrcode.width:300}")
    private int qrWidth;
    @Value("${mytools.qrcode.height:300}")
    private int qrHeight;

    public String uuid(boolean noDash) {
        logService.record("aux.uuid", "辅助工具");
        return noDash ? AuxUtils.uuidNoDash() : AuxUtils.uuid();
    }

    public List<String> uuidBatch(int count) {
        logService.record("aux.uuid", "辅助工具");
        return AuxUtils.uuidBatch(count);
    }

    public String qrcodeGenerate(String content, Integer width, Integer height) {
        logService.record("aux.qrcode", "辅助工具");
        int w = width == null || width <= 0 ? qrWidth : width;
        int h = height == null || height <= 0 ? qrHeight : height;
        return QrcodeUtils.generateBase64(content, w, h);
    }

    public String qrcodeParse(MultipartFile file) {
        logService.record("aux.qrcode", "辅助工具");
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("请上传二维码图片");
        }
        try {
            return QrcodeUtils.parse(file.getInputStream());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
