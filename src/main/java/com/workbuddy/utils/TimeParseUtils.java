package com.workbuddy.utils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 中文自然语言时间解析（用于待办标题自动识别截止时间）。
 *
 * 支持：
 *  - 相对日：今天/今日、明天/明日、后天、大后天、昨天/昨日、前天
 *  - 星期：周一~周日、星期一~星期天、礼拜一~礼拜天、周1~周7
 *  - 具体日期：2026-07-21、2026年7月21日、7月21日、7月21号、7/21、7.21
 *  - 时段词：凌晨/清晨/早晨/早上/上午=8:00，中午/正午=12:00，
 *            午后/下午=14:00，傍晚=18:00，晚上/夜晚/夜里=20:00，深夜=22:00，半夜=23:00
 *  - 具体时刻：8点、8:30、8.30、15:30、下午3点、晚上8点半、8点一刻/两刻/三刻、8点30分
 *
 * 规则：仅有时段词（如“晚上”）但没有具体点钟时，取该时段的保守默认值；
 *       完全没有时间信息时，默认 09:00（便于人工再调整）。
 * 输出 LocalDateTime（秒固定为 0）。未识别到任何日期信息时返回 Optional.empty()。
 */
public class TimeParseUtils {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** 时段词 -> 默认小时（顺序敏感，先匹配更具体的词） */
    private static final String[][] PERIODS = {
            {"凌晨", "4"},
            {"清晨", "6"},
            {"早晨", "7"},
            {"早上", "8"},
            {"上午", "8"},
            {"中午", "12"},
            {"正午", "12"},
            {"午后", "13"},
            {"下午", "14"},
            {"傍晚", "18"},
            {"晚上", "20"},
            {"夜晚", "20"},
            {"夜里", "21"},
            {"深夜", "22"},
            {"半夜", "23"}
    };

    /** 解析标题文本中的时间，返回 yyyy-MM-dd HH:mm:ss 字符串；未识别返回 empty */
    public static Optional<String> parseToText(String text) {
        return parse(text).map(FORMATTER::format);
    }

    /** 解析为 LocalDateTime */
    public static Optional<LocalDateTime> parse(String text) {
        if (text == null || text.trim().isEmpty()) {
            return Optional.empty();
        }
        LocalDate date = parseDate(text);
        if (date == null) {
            // 仅识别出时间（如“下午3点开会”）但没有日期时，保守默认用今天
            if (hasTimeInfo(text)) {
                date = LocalDate.now();
            } else {
                return Optional.empty();
            }
        }
        int[] time = parseTime(text);
        return Optional.of(LocalDateTime.of(date, LocalTime.of(time[0], time[1])));
    }

    /** 文本中是否包含任何时间线索（时段词或具体时刻） */
    private static boolean hasTimeInfo(String text) {
        for (String[] p : PERIODS) {
            if (text.contains(p[0])) return true;
        }
        if (text.contains("今晚") || text.contains("今夜") || text.contains("今早")
                || text.contains("今晨") || text.contains("今午")) {
            return true;
        }
        if (Pattern.compile("(\\d{1,2})\\s*[:：.]\\s*(\\d{1,2})").matcher(text).find()) return true;
        if (Pattern.compile("(\\d{1,2})\\s*点").matcher(text).find()) return true;
        return false;
    }

    /** 解析为 java.util.Date */
    public static Optional<java.util.Date> parseToDate(String text) {
        return parse(text).map(ldt ->
                java.util.Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant()));
    }

    // ===================== 日期解析 =====================

    private static LocalDate parseDate(String text) {
        LocalDate today = LocalDate.now();

        // 1) 相对日
        if (text.contains("大后天")) return today.plusDays(3);
        if (text.contains("后天")) return today.plusDays(2);
        if (text.contains("明天") || text.contains("明日")) return today.plusDays(1);
        if (text.contains("今天") || text.contains("今日")
                || text.contains("今晚") || text.contains("今早") || text.contains("今午")) {
            return today;
        }
        if (text.contains("昨天") || text.contains("昨日")) return today.minusDays(1);
        if (text.contains("前天")) return today.minusDays(2);

        // 2) 星期
        LocalDate weekDate = parseWeekday(text, today);
        if (weekDate != null) return weekDate;

        // 3) 完整日期 YYYY年M月D日 / YYYY-M-D / YYYY.M.D
        Matcher m = Pattern.compile("(\\d{4})\\s*[-/年.\\u5e74]\\s*(\\d{1,2})\\s*[-/月.\\u6708]\\s*(\\d{1,2})\\s*[日号]?")
                .matcher(text);
        if (m.find()) {
            return safeDate(Integer.parseInt(m.group(1)),
                    Integer.parseInt(m.group(2)), Integer.parseInt(m.group(3)), today);
        }

        // 4) M月D日 / M月D号
        m = Pattern.compile("(\\d{1,2})\\s*[月\\u6708]\\s*(\\d{1,2})\\s*[日号]?").matcher(text);
        if (m.find()) {
            return safeDate(today.getYear(),
                    Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)), today);
        }

        // 5) M/D 或 M.D（避免匹配到版本号之类，限定月<=12）
        m = Pattern.compile("(\\d{1,2})\\s*[/_.]\\s*(\\d{1,2})(?!\\d)").matcher(text);
        if (m.find()) {
            int mo = Integer.parseInt(m.group(1));
            int d = Integer.parseInt(m.group(2));
            if (mo >= 1 && mo <= 12 && d >= 1 && d <= 31) {
                return safeDate(today.getYear(), mo, d, today);
            }
        }

        return null;
    }

    private static LocalDate safeDate(int y, int mo, int d, LocalDate today) {
        try {
            LocalDate ld = LocalDate.of(y, mo, d);
            // 日期已过且未显式给年份（用今年）时，顺延到下一年
            if (ld.isBefore(today)) {
                ld = ld.plusYears(1);
            }
            return ld;
        } catch (Exception e) {
            return null;
        }
    }

    private static LocalDate parseWeekday(String text, LocalDate today) {
        Matcher m = Pattern.compile("(周|星期|礼拜)\\s*([一二三四五六日天1-7])").matcher(text);
        if (!m.find()) {
            return null;
        }
        Integer target = toIsoDow(m.group(2));
        if (target == null) {
            return null;
        }
        int todayDow = today.getDayOfWeek().getValue(); // 1=Mon .. 7=Sun
        int diff = target - todayDow;
        if (diff <= 0) {
            diff += 7; // 总是取未来的那一个（含下一周）
        }
        return today.plusDays(diff);
    }

    private static Integer toIsoDow(String w) {
        switch (w) {
            case "一": case "1": return 1;
            case "二": case "2": return 2;
            case "三": case "3": return 3;
            case "四": case "4": return 4;
            case "五": case "5": return 5;
            case "六": case "6": return 6;
            case "日": case "天": case "7": return 7;
            default: return null;
        }
    }

    // ===================== 时间解析 =====================

    /** @return int[]{hour, minute} */
    private static int[] parseTime(String text) {
        // 1) 时段词 -> 默认小时 + 是否下午（需要给具体点钟 +12）
        Integer periodHour = null;
        boolean isPm = false;
        // “今X”型：今晚/今夜视为晚上，今早/今晨视为早上，今午视为中午
        if (text.contains("今晚") || text.contains("今夜")) {
            periodHour = 20;
            isPm = true;
        } else if (text.contains("今早") || text.contains("今晨")) {
            periodHour = 8;
        } else if (text.contains("今午")) {
            periodHour = 12;
        }
        if (periodHour == null) {
            for (String[] p : PERIODS) {
                if (text.contains(p[0])) {
                    periodHour = Integer.parseInt(p[1]);
                    if (isPmWord(p[0])) {
                        isPm = true;
                    }
                    break;
                }
            }
        }

        // 2) 具体时刻 HH:mm / HH.mm
        int hour = -1;
        int minute = 0;
        Matcher m = Pattern.compile("(\\d{1,2})\\s*[:：.]\\s*(\\d{1,2})").matcher(text);
        if (m.find()) {
            hour = Integer.parseInt(m.group(1));
            minute = Integer.parseInt(m.group(2));
        } else {
            // X点[(半|N刻)|(Y分)]
            m = Pattern.compile("(\\d{1,2})\\s*点\\s*(半|[一二三四1-4]刻|(\\d{1,2})\\s*分)?").matcher(text);
            if (m.find()) {
                hour = Integer.parseInt(m.group(1));
                String tail = m.group(2);
                if (tail != null) {
                    if (tail.contains("半")) {
                        minute = 30;
                    } else if (tail.contains("刻")) {
                        int n = "一二三四".indexOf(tail.charAt(0)) + 1;
                        if (n <= 0) {
                            try {
                                n = Integer.parseInt(tail.replace("刻", ""));
                            } catch (NumberFormatException e) {
                                n = 1;
                            }
                        }
                        minute = n * 15;
                    } else if (m.group(3) != null) {
                        minute = Integer.parseInt(m.group(3));
                    }
                }
            }
        }

        // 3) 合并
        if (hour == -1) {
            // 没有具体点钟：用时段默认值；都没有则保守默认 09:00
            if (periodHour != null) {
                return new int[]{periodHour, 0};
            }
            return new int[]{9, 0};
        }
        // 具体时刻存在
        if (hour <= 12 && isPm) {
            hour += 12;
        }
        if (hour == 24) {
            hour = 0;
        }
        return new int[]{hour, minute};
    }

    private static boolean isPmWord(String word) {
        return word.equals("午后") || word.equals("下午") || word.equals("傍晚")
                || word.equals("晚上") || word.equals("夜晚") || word.equals("夜里")
                || word.equals("深夜") || word.equals("半夜");
    }
}
