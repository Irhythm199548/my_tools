package com.workbuddy.service;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 工具目录：集中定义所有工具的 key / 名称 / 路由 / 图标 / 分类。
 * 个人中心按此列表渲染开关；导航与仪表盘据此过滤「已启用」的工具。
 * enabledTools 为 null/空表示全部启用。
 */
@Component
public class ToolCatalog {

    /** 单个工具元信息 */
    public static class ToolMeta {
        public final String key;
        public final String label;
        public final String url;
        public final String icon;
        public final String category;

        public ToolMeta(String key, String label, String url, String icon, String category) {
            this.key = key;
            this.label = label;
            this.url = url;
            this.icon = icon;
            this.category = category;
        }
    }

    private final List<ToolMeta> ALL = Arrays.asList(
            new ToolMeta("todo", "待办事项", "/todo", "✅", "数据存储"),
            new ToolMeta("encode", "编码工具", "/encode", "🔐", "开发工具"),
            new ToolMeta("text", "文本处理", "/text", "📝", "开发工具"),
            new ToolMeta("convert", "格式转换", "/convert", "🔄", "开发工具"),
            new ToolMeta("network", "网络与安全", "/network", "🌐", "开发工具"),
            new ToolMeta("pomodoro", "番茄时间", "/pomodoro", "🍅", "数据存储"),
            new ToolMeta("aux", "辅助工具", "/auxs", "🧰", "开发工具"),
            new ToolMeta("cron", "Cron 表达式", "/cron", "⏱️", "开发工具"),
            new ToolMeta("logs", "使用统计", "/logs", "📊", "数据存储")
    );

    public List<ToolMeta> all() {
        return new ArrayList<>(ALL);
    }

    public List<String> allKeys() {
        List<String> keys = new ArrayList<>();
        for (ToolMeta t : ALL) keys.add(t.key);
        return keys;
    }

    /** 解析启用的工具 key：null/空表示全部启用 */
    public List<String> resolveEnabled(String enabledTools) {
        Set<String> valid = new LinkedHashSet<>();
        for (ToolMeta t : ALL) valid.add(t.key);
        if (enabledTools == null || enabledTools.trim().isEmpty()) {
            return new ArrayList<>(valid);
        }
        Set<String> result = new LinkedHashSet<>();
        for (String k : enabledTools.split(",")) {
            String key = k.trim();
            if (valid.contains(key)) {
                result.add(key);
            }
        }
        return new ArrayList<>(result);
    }

    /** 是否启用某工具（用于接口级兜底校验） */
    public boolean isEnabled(String enabledTools, String key) {
        return resolveEnabled(enabledTools).contains(key);
    }
}
