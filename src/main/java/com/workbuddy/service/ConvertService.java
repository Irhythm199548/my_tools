package com.workbuddy.service;

import com.workbuddy.utils.ConvertUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 格式转换 Service（无状态）
 * 封装 Unix 时间戳转换、图片转 Base64。
 */
@Service
@RequiredArgsConstructor
public class ConvertService {

    private final OperationLogService logService;

    public Map<String, Object> now(String timeZone) {
        logService.record("convert.timestamp", "格式转换");
        return ConvertUtils.now(timeZone);
    }

    public Map<String, Object> timestampToDate(long timestamp, boolean millis, String timeZone) {
        logService.record("convert.timestamp", "格式转换");
        return ConvertUtils.timestampToDate(timestamp, millis, timeZone);
    }

    public long dateToTimestamp(String dateStr, String pattern, String timeZone) {
        logService.record("convert.timestamp", "格式转换");
        return ConvertUtils.dateToTimestamp(dateStr, pattern, timeZone);
    }

    public String imageToBase64(MultipartFile file) {
        logService.record("convert.image", "格式转换");
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("请上传图片文件");
        }
        return ConvertUtils.imageToBase64(file);
    }
}
