package com.workbuddy.utils;

import org.apache.commons.codec.binary.Base64;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 格式转换工具类
 * 包含：Unix 时间戳转换（秒/毫秒，时区感知）、图片转 Base64。
 */
public class ConvertUtils {

    /** 当前时间信息 */
    public static Map<String, Object> now(String timeZone) {
        Map<String, Object> m = new HashMap<>();
        long millis = System.currentTimeMillis();
        m.put("epochMillis", millis);
        m.put("epochSeconds", millis / 1000);
        m.put("formatted", formatMillis(millis, "yyyy-MM-dd HH:mm:ss.SSS", timeZone));
        m.put("timeZone", resolveTz(timeZone));
        return m;
    }

    /** 时间戳 -> 日期 */
    public static Map<String, Object> timestampToDate(long timestamp, boolean millis, String timeZone) {
        long ms = millis ? timestamp : timestamp * 1000;
        Map<String, Object> m = new HashMap<>();
        m.put("epochMillis", ms);
        m.put("epochSeconds", ms / 1000);
        m.put("formatted", formatMillis(ms, "yyyy-MM-dd HH:mm:ss.SSS", timeZone));
        m.put("timeZone", resolveTz(timeZone));
        return m;
    }

    /** 日期 -> 时间戳 */
    public static long dateToTimestamp(String dateStr, String pattern, String timeZone) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(pattern);
            sdf.setTimeZone(TimeZone.getTimeZone(resolveTz(timeZone)));
            Date d = sdf.parse(dateStr);
            return d.getTime();
        } catch (Exception e) {
            throw new RuntimeException("日期解析失败: " + e.getMessage(), e);
        }
    }

    private static String formatMillis(long ms, String pattern, String tz) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        sdf.setTimeZone(TimeZone.getTimeZone(resolveTz(tz)));
        return sdf.format(new Date(ms));
    }

    private static String resolveTz(String tz) {
        if (tz == null || tz.trim().isEmpty()) return "Asia/Shanghai";
        return tz.trim();
    }

    /** 图片转 Base64（返回 data URI） */
    public static String imageToBase64(MultipartFile file) {
        try {
            byte[] bytes = file.getBytes();
            String base64 = Base64.encodeBase64String(bytes);
            String mime = file.getContentType() == null ? "application/octet-stream" : file.getContentType();
            return "data:" + mime + ";base64," + base64;
        } catch (Exception e) {
            throw new RuntimeException("图片转 Base64 失败: " + e.getMessage(), e);
        }
    }
}
