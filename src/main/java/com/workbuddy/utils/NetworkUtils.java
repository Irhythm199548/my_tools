package com.workbuddy.utils;

import cn.hutool.http.HttpUtil;
import com.github.javafaker.Faker;

import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 网络与安全工具类
 * 包含：IP 归属地查询、HTTP Header / User-Agent 解析、随机密码生成、假数据生成、端口扫描（内网版）。
 */
public class NetworkUtils {

    private static final Pattern IPV4 = Pattern.compile("^(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})$");

    // ============ IP 归属地查询 ============

    /** 调用免费 API（ip-api.com）查询 IPv4 归属地，需联网；失败返回错误信息 */
    public static Map<String, Object> ipLocation(String ip) {
        Map<String, Object> result = new HashMap<>();
        if (!isValidIpv4(ip) && !"localhost".equalsIgnoreCase(ip)) {
            result.put("ok", false);
            result.put("message", "不是合法的 IPv4 地址");
            return result;
        }
        String query = "localhost".equalsIgnoreCase(ip) ? "127.0.0.1" : ip;
        String url = "http://ip-api.com/json/" + query
                + "?lang=zh-CN&fields=status,message,country,regionName,city,district,isp,org,as,query,timezone,lat,lon";
        try {
            String resp = HttpUtil.get(url, 4000);
            result.put("ok", true);
            result.put("raw", resp);
        } catch (Exception e) {
            result.put("ok", false);
            result.put("message", "查询失败，请确认网络可访问 ip-api.com：" + e.getMessage());
        }
        return result;
    }

    public static boolean isValidIpv4(String ip) {
        if (ip == null) return false;
        Matcher m = IPV4.matcher(ip);
        if (!m.matches()) return false;
        for (int i = 1; i <= 4; i++) {
            if (Integer.parseInt(m.group(i)) > 255) return false;
        }
        return true;
    }

    // ============ User-Agent 解析 ============

    public static Map<String, Object> parseUserAgent(String ua) {
        Map<String, Object> r = new HashMap<>();
        r.put("raw", ua);
        r.put("browser", detectBrowser(ua));
        r.put("os", detectOs(ua));
        r.put("device", detectDevice(ua));
        r.put("isMobile", ua != null && (ua.contains("Mobile") || ua.contains("Android") || ua.contains("iPhone")));
        return r;
    }

    private static String detectBrowser(String ua) {
        if (ua == null) return "未知";
        if (ua.contains("Edg/")) return "Microsoft Edge";
        if (ua.contains("OPR/") || ua.contains("Opera")) return "Opera";
        if (ua.contains("Chrome/") && !ua.contains("Chromium")) return "Chrome";
        if (ua.contains("Firefox/")) return "Firefox";
        if (ua.contains("Safari/") && ua.contains("Version/")) return "Safari";
        if (ua.contains("MSIE") || ua.contains("Trident/")) return "Internet Explorer";
        return "未知";
    }

    private static String detectOs(String ua) {
        if (ua == null) return "未知";
        if (ua.contains("Windows NT 10.0")) return "Windows 10/11";
        if (ua.contains("Windows")) return "Windows";
        if (ua.contains("Mac OS X")) return "macOS";
        if (ua.contains("Android")) return "Android";
        if (ua.contains("iPhone") || ua.contains("iPad")) return "iOS";
        if (ua.contains("Linux")) return "Linux";
        return "未知";
    }

    private static String detectDevice(String ua) {
        if (ua == null) return "未知";
        if (ua.contains("iPad")) return "Tablet";
        if (ua.contains("iPhone")) return "Mobile";
        if (ua.contains("Android") && ua.contains("Mobile")) return "Mobile";
        if (ua.contains("Mobile")) return "Mobile";
        return "Desktop";
    }

    // ============ HTTP Header 解析 ============

    public static Map<String, Object> parseHeaders(String raw) {
        Map<String, Object> headers = new LinkedHashMap<>();
        if (raw != null) {
            for (String line : raw.split("\n")) {
                line = line.trim();
                if (line.isEmpty()) continue;
                int idx = line.indexOf(":");
                if (idx > 0) {
                    headers.put(line.substring(0, idx).trim(), line.substring(idx + 1).trim());
                }
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("headers", headers);
        result.put("knownCount", countKnown(headers.keySet()));
        result.put("totalCount", headers.size());
        return result;
    }

    private static int countKnown(Set<String> keys) {
        String[] known = {"Host", "User-Agent", "Accept", "Accept-Language", "Accept-Encoding",
                "Connection", "Content-Type", "Content-Length", "Authorization", "Cookie",
                "Referer", "Cache-Control", "Origin", "Upgrade-Insecure-Requests"};
        int c = 0;
        for (String k : keys) {
            for (String s : known) {
                if (s.equalsIgnoreCase(k)) { c++; break; }
            }
        }
        return c;
    }

    // ============ 随机密码生成 ============

    public static String generatePassword(int length, boolean upper, boolean lower, boolean digit, boolean special) {
        if (length <= 0) length = 16;
        String U = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String L = "abcdefghijklmnopqrstuvwxyz";
        String D = "0123456789";
        String S = "!@#$%^&*()-_=+[]{};:,.<>?";
        StringBuilder pool = new StringBuilder();
        List<Character> guaranteed = new ArrayList<>();
        SecureRandom rnd = new SecureRandom();
        if (upper) { pool.append(U); guaranteed.add(U.charAt(rnd.nextInt(U.length()))); }
        if (lower) { pool.append(L); guaranteed.add(L.charAt(rnd.nextInt(L.length()))); }
        if (digit) { pool.append(D); guaranteed.add(D.charAt(rnd.nextInt(D.length()))); }
        if (special) { pool.append(S); guaranteed.add(S.charAt(rnd.nextInt(S.length()))); }
        if (pool.length() == 0) { pool.append(L); guaranteed.add(L.charAt(rnd.nextInt(L.length()))); }

        char[] out = new char[length];
        int i = 0;
        for (char c : guaranteed) {
            if (i < length) out[i++] = c;
        }
        for (; i < length; i++) {
            out[i] = pool.charAt(rnd.nextInt(pool.length()));
        }
        // Fisher-Yates 洗牌，保证必含字符位置随机
        for (int k = length - 1; k > 0; k--) {
            int j = rnd.nextInt(k + 1);
            char tmp = out[k];
            out[k] = out[j];
            out[j] = tmp;
        }
        return new String(out);
    }

    // ============ 假数据生成（JavaFaker） ============

    public static Map<String, Object> mockPerson(String locale) {
        Faker faker = buildFaker(locale);
        Map<String, Object> m = new HashMap<>();
        m.put("name", safe(() -> faker.name().fullName()));
        m.put("firstName", safe(() -> faker.name().firstName()));
        m.put("lastName", safe(() -> faker.name().lastName()));
        m.put("email", safe(() -> faker.internet().emailAddress()));
        m.put("phone", safe(() -> faker.phoneNumber().cellPhone()));
        m.put("address", safe(() -> faker.address().fullAddress()));
        m.put("city", safe(() -> faker.address().city()));
        m.put("company", safe(() -> faker.company().name()));
        m.put("jobTitle", safe(() -> faker.job().title()));
        m.put("color", safe(() -> faker.color().name()));
        m.put("idNumber", safe(() -> faker.idNumber().valid()));
        return m;
    }

    private static Faker buildFaker(String locale) {
        try {
            if (locale != null && !locale.trim().isEmpty() && !"en".equalsIgnoreCase(locale.trim())) {
                return new Faker(new Locale(locale.trim()));
            }
        } catch (Exception ignored) {
            // 回退到默认
        }
        return new Faker();
    }

    private static <T> T safe(Supplier<T> call) {
        try {
            return call.get();
        } catch (Exception e) {
            return null;
        }
    }

    // ============ 端口扫描（内网版） ============

    /**
     * 是否为允许扫描的地址：仅本机或私有网段。
     * 出于安全考虑（公网暴露时防止被当作端口扫描跳板/SSRF），
     * 拒绝任何公网地址或解析到公网地址的主机名。
     */
    public static boolean isScannableHost(String host) {
        if (host == null) return false;
        String h = host.trim().toLowerCase();
        if (h.isEmpty()) return false;
        if ("localhost".equals(h) || "127.0.0.1".equals(h) || "::1".equals(h) || "[::1]".equals(h)) return true;
        try {
            java.net.InetAddress addr = java.net.InetAddress.getByName(h);
            return addr.isLoopbackAddress() || addr.isSiteLocalAddress();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 扫描 host 的 [from,to] 端口范围。
     * 使用 Socket 连接超时机制，线程池并发，线程安全（同步列表收集结果）。
     * 安全限制：仅允许本机/内网地址；线程数与端口数上限封顶，防止资源耗尽。
     */
    public static Map<String, Object> portScan(String host, int from, int to, int threads, int timeout) {
        Map<String, Object> denied = new HashMap<>();
        if (!isScannableHost(host)) {
            denied.put("ok", false);
            denied.put("message", "出于安全考虑，仅允许扫描本机或内网地址（127.0.0.1 / localhost / 10.x / 172.16-31.x / 192.168.x / 169.254.x）。");
            return denied;
        }
        if (from < 1) from = 1;
        if (to > 65535) to = 65535;
        if (from > to) {
            int t = from;
            from = to;
            to = t;
        }
        // 端口数封顶，避免单次扫描耗尽资源
        if (to - from + 1 > 2000) to = from + 1999;
        if (threads < 1) threads = 32;
        if (threads > 100) threads = 100;

        List<Integer> openPorts = Collections.synchronizedList(new ArrayList<>());
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        int total = to - from + 1;
        CountDownLatch latch = new CountDownLatch(total);
        for (int port = from; port <= to; port++) {
            final int p = port;
            pool.submit(() -> {
                try (java.net.Socket socket = new java.net.Socket()) {
                    socket.connect(new java.net.InetSocketAddress(host, p), timeout);
                    openPorts.add(p);
                } catch (Exception ignored) {
                    // 端口关闭或不可达，忽略
                } finally {
                    latch.countDown();
                }
            });
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            pool.shutdownNow();
        }
        Collections.sort(openPorts);

        Map<String, Object> result = new HashMap<>();
        result.put("host", host);
        result.put("range", from + "-" + to);
        result.put("openPorts", openPorts);
        result.put("openCount", openPorts.size());
        return result;
    }
}
