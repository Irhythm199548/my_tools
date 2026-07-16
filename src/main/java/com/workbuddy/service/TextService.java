package com.workbuddy.service;

import com.workbuddy.utils.TextUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 文本处理 Service（无状态）
 * 封装 Diff / 正则 / 命名风格 / 字数统计 / YAML·Properties·JSON 互转。
 */
@Service
@RequiredArgsConstructor
public class TextService {

    private final OperationLogService logService;

    public String diff(String text1, String text2) {
        logService.record("text.diff", "文本处理");
        return TextUtils.diffToHtml(text1, text2);
    }

    public Map<String, Object> regexTest(String pattern, String text) {
        logService.record("text.regex", "文本处理");
        return TextUtils.regexTest(pattern, text);
    }

    public String caseConvert(String target, String input) {
        logService.record("text.case", "文本处理");
        switch (target.toLowerCase()) {
            case "camel":
                return TextUtils.toCamel(input);
            case "pascal":
                return TextUtils.toPascal(input);
            case "snake":
                return TextUtils.toSnake(input);
            case "kebab":
                return TextUtils.toKebab(input);
            default:
                throw new IllegalArgumentException("不支持的转换目标: " + target);
        }
    }

    public Map<String, Object> wordCount(String text) {
        logService.record("text.wordcount", "文本处理");
        return TextUtils.wordCount(text);
    }

    /** 格式互转：from -> to（yaml/properties/json） */
    public String convert(String from, String to, String content) {
        logService.record("text.convert", "文本处理");
        String lowerFrom = from.toLowerCase();
        String lowerTo = to.toLowerCase();
        if ("yaml".equals(lowerFrom)) {
            if ("json".equals(lowerTo)) return TextUtils.yamlToJson(content);
            if ("properties".equals(lowerTo)) return TextUtils.yamlToProperties(content);
        } else if ("json".equals(lowerFrom)) {
            if ("yaml".equals(lowerTo)) return TextUtils.jsonToYaml(content);
            if ("properties".equals(lowerTo)) return TextUtils.jsonToProperties(content);
        } else if ("properties".equals(lowerFrom)) {
            if ("yaml".equals(lowerTo)) return TextUtils.propertiesToYaml(content);
            if ("json".equals(lowerTo)) return TextUtils.propertiesToJson(content);
        }
        throw new IllegalArgumentException("不支持的转换: " + from + " -> " + to);
    }
}
