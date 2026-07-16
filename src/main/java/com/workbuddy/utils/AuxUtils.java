package com.workbuddy.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 辅助工具类：UUID 生成
 */
public class AuxUtils {

    /** 生成标准 UUID（带连字符） */
    public static String uuid() {
        return UUID.randomUUID().toString();
    }

    /** 生成无连字符 UUID */
    public static String uuidNoDash() {
        return uuid().replace("-", "");
    }

    /** 批量生成 UUID（最多 1000 个） */
    public static List<String> uuidBatch(int count) {
        if (count <= 0) count = 1;
        if (count > 1000) count = 1000;
        List<String> list = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            list.add(UUID.randomUUID().toString());
        }
        return list;
    }
}
