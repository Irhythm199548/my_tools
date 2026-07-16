package com.workbuddy.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 文本处理工具类
 * 包含：Diff 对比、正则测试、命名风格转换、字数统计、YAML/Properties/JSON 互转。
 */
public class TextUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);
    private static final Yaml YAML = buildYaml();

    private static Yaml buildYaml() {
        DumperOptions opts = new DumperOptions();
        opts.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        opts.setPrettyFlow(true);
        return new Yaml(opts);
    }

    // ============ 1. Diff 对比（行级 LCS，返回差异 HTML） ============

    public static String diffToHtml(String text1, String text2) {
        String[] a = (text1 == null ? "" : text1).split("\n", -1);
        String[] b = (text2 == null ? "" : text2).split("\n", -1);
        int n = a.length, m = b.length;
        int[][] dp = new int[n + 1][m + 1];
        for (int i = n - 1; i >= 0; i--) {
            for (int j = m - 1; j >= 0; j--) {
                dp[i][j] = a[i].equals(b[j]) ? dp[i + 1][j + 1] + 1 : Math.max(dp[i + 1][j], dp[i][j + 1]);
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<table class='table table-sm diff-table'><tbody>");
        int i = 0, j = 0;
        while (i < n && j < m) {
            if (a[i].equals(b[j])) {
                sb.append(row("same", " ", escape(a[i])));
                i++;
                j++;
            } else if (dp[i + 1][j] >= dp[i][j + 1]) {
                sb.append(row("del", "-", escape(a[i])));
                i++;
            } else {
                sb.append(row("ins", "+", escape(b[j])));
                j++;
            }
        }
        while (i < n) sb.append(row("del", "-", escape(a[i++])));
        while (j < m) sb.append(row("ins", "+", escape(b[j++])));
        sb.append("</tbody></table>");
        return sb.toString();
    }

    private static String row(String kind, String sign, String content) {
        return "<tr class='diff-" + kind + "'><td class='diff-sign'>" + sign + "</td><td>" + content + "</td></tr>";
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace(" ", "&nbsp;");
    }

    // ============ 2. 正则测试器 ============

    public static Map<String, Object> regexTest(String pattern, String text) {
        Map<String, Object> result = new HashMap<>();
        if (pattern == null || pattern.isEmpty()) {
            result.put("ok", false);
            result.put("message", "正则表达式不能为空");
            return result;
        }
        try {
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher mt = p.matcher(text == null ? "" : text);
            List<String> matches = new ArrayList<>();
            StringBuffer highlighted = new StringBuffer();
            int count = 0;
            while (mt.find()) {
                count++;
                matches.add(mt.group());
                mt.appendReplacement(highlighted, "<mark>" + escape(mt.group()) + "</mark>");
            }
            mt.appendTail(highlighted);
            result.put("ok", true);
            result.put("count", count);
            result.put("matches", matches);
            result.put("highlighted", highlighted.toString());
        } catch (java.util.regex.PatternSyntaxException e) {
            result.put("ok", false);
            result.put("message", "正则语法错误: " + e.getMessage());
        }
        return result;
    }

    // ============ 3. 命名风格转换 ============

    public static String toCamel(String input) {
        List<String> words = splitWords(input);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < words.size(); i++) {
            String w = words.get(i);
            sb.append(i == 0 ? w.toLowerCase() : capitalize(w.toLowerCase()));
        }
        return sb.toString();
    }

    public static String toPascal(String input) {
        List<String> words = splitWords(input);
        StringBuilder sb = new StringBuilder();
        for (String w : words) sb.append(capitalize(w.toLowerCase()));
        return sb.toString();
    }

    public static String toSnake(String input) {
        return String.join("_", splitWords(input));
    }

    public static String toKebab(String input) {
        return String.join("-", splitWords(input));
    }

    private static List<String> splitWords(String s) {
        List<String> words = new ArrayList<>();
        if (s == null || s.isEmpty()) return words;
        String[] parts = s.split("[_\\-\\s]+");
        for (String part : parts) {
            if (part.isEmpty()) continue;
            StringBuilder cur = new StringBuilder();
            for (int k = 0; k < part.length(); k++) {
                char c = part.charAt(k);
                if (Character.isUpperCase(c) && cur.length() > 0) {
                    words.add(cur.toString());
                    cur = new StringBuilder();
                }
                cur.append(Character.toLowerCase(c));
            }
            if (cur.length() > 0) words.add(cur.toString());
        }
        return words;
    }

    private static String capitalize(String s) {
        if (s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    // ============ 4. 字数统计 ============

    public static Map<String, Object> wordCount(String text) {
        Map<String, Object> result = new HashMap<>();
        if (text == null) text = "";
        int chars = text.length();
        int lines = text.isEmpty() ? 0 : text.split("\n", -1).length;
        int words = text.trim().isEmpty() ? 0 : text.trim().split("\\s+").length;
        int bytes = text.getBytes(StandardCharsets.UTF_8).length;
        int noSpace = text.replaceAll("\\s", "").length();
        result.put("chars", chars);
        result.put("noSpace", noSpace);
        result.put("words", words);
        result.put("lines", lines);
        result.put("bytes", bytes);
        return result;
    }

    // ============ 5. YAML / Properties / JSON 互转 ============

    public static String yamlToJson(String yaml) {
        try {
            return OBJECT_MAPPER.writeValueAsString(loadYamlAsMap(yaml));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("YAML 转 JSON 失败", e);
        }
    }

    public static String jsonToYaml(String json) {
        try {
            Object obj = OBJECT_MAPPER.readValue(json, Object.class);
            return YAML.dump(obj);
        } catch (Exception e) {
            throw new RuntimeException("JSON 转 YAML 失败: " + e.getMessage(), e);
        }
    }

    public static String yamlToProperties(String yaml) {
        Properties props = new Properties();
        flatten("", loadYamlAsMap(yaml), props);
        StringBuilder sb = new StringBuilder();
        for (String key : new TreeSet<>(props.stringPropertyNames())) {
            sb.append(key).append(" = ").append(props.getProperty(key)).append("\n");
        }
        return sb.toString();
    }

    public static String propertiesToYaml(String properties) {
        Properties props = loadProperties(properties);
        Map<String, Object> map = new LinkedHashMap<>();
        for (String key : props.stringPropertyNames()) {
            putNested(map, key, props.getProperty(key));
        }
        return YAML.dump(map);
    }

    public static String jsonToProperties(String json) {
        try {
            Map<String, Object> map = OBJECT_MAPPER.readValue(json, Map.class);
            Properties props = new Properties();
            flatten("", map, props);
            StringBuilder sb = new StringBuilder();
            for (String key : new TreeSet<>(props.stringPropertyNames())) {
                sb.append(key).append(" = ").append(props.getProperty(key)).append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("JSON 转 Properties 失败: " + e.getMessage(), e);
        }
    }

    public static String propertiesToJson(String properties) {
        Properties props = loadProperties(properties);
        Map<String, Object> map = new LinkedHashMap<>();
        for (String key : props.stringPropertyNames()) {
            putNested(map, key, props.getProperty(key));
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Properties 转 JSON 失败", e);
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> loadYamlAsMap(String yaml) {
        Object loaded = YAML.load(yaml);
        if (loaded == null) return new LinkedHashMap<>();
        if (loaded instanceof Map) return (Map<String, Object>) loaded;
        Map<String, Object> wrap = new LinkedHashMap<>();
        wrap.put("value", loaded);
        return wrap;
    }

    private static Properties loadProperties(String properties) {
        Properties props = new Properties();
        try {
            props.load(new ByteArrayInputStream(properties.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new RuntimeException("Properties 解析失败: " + e.getMessage(), e);
        }
        return props;
    }

    @SuppressWarnings("unchecked")
    private static void flatten(String prefix, Map<String, Object> map, Properties props) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            Object val = entry.getValue();
            if (val instanceof Map) {
                flatten(key, (Map<String, Object>) val, props);
            } else if (val instanceof List) {
                props.put(key, OBJECT_MAPPER.convertValue(val, String.class));
            } else {
                props.put(key, val == null ? "" : val.toString());
            }
        }
    }

    private static void putNested(Map<String, Object> map, String key, String value) {
        String[] parts = key.split("\\.");
        Map<String, Object> cur = map;
        for (int i = 0; i < parts.length - 1; i++) {
            cur = (Map<String, Object>) cur.computeIfAbsent(parts[i], k -> new LinkedHashMap<>());
        }
        cur.put(parts[parts.length - 1], value);
    }
}
