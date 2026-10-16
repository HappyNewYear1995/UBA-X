package com.ubax.ext;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Shared helpers for the analytical "数据应用" extension services. The wide projection
 * returned by {@code EventRepository.extWideRows} is decoded here so every service uses
 * the same column indices and normalization rules.
 */
final class ExtSupport {

    private ExtSupport() {}

    /* wide-row column indices */
    static final int C_DISTINCT = 0, C_SESSION = 1, C_TYPE = 2, C_NAME = 3, C_PATH = 4,
            C_TITLE = 5, C_REFERRER = 6, C_DEVICE = 7, C_OS = 8, C_BROWSER = 9,
            C_REGION = 10, C_CITY = 11, C_PROPS = 12, C_DURATION = 13, C_TIME = 14, C_DAY = 15;

    static final Set<String> FUNNEL_STEPS =
            new LinkedHashSet<>(List.of("view_product", "add_to_cart", "begin_checkout", "purchase"));

    private static final Pattern AMOUNT = Pattern.compile("\"amount\"\\s*:\\s*([0-9]+(?:\\.[0-9]+)?)");

    static LocalDateTime[] range(String from, String to, int defaultDays) {
        LocalDate end = to != null && !to.isBlank() ? LocalDate.parse(to) : LocalDate.now();
        LocalDate start = from != null && !from.isBlank() ? LocalDate.parse(from) : end.minusDays(defaultDays - 1L);
        return new LocalDateTime[]{start.atStartOfDay(), end.plusDays(1).atStartOfDay().minusNanos(1)};
    }

    static String nz(Object o) { return o == null ? "" : String.valueOf(o); }

    static String label(String v) { return v == null || v.isBlank() ? "未知" : v; }

    static LocalDateTime toDateTime(Object o) {
        if (o instanceof LocalDateTime t) return t;
        if (o instanceof java.sql.Timestamp ts) return ts.toLocalDateTime();
        if (o instanceof java.util.Date d) return new java.sql.Timestamp(d.getTime()).toLocalDateTime();
        return LocalDateTime.now();
    }

    static LocalDate toDate(Object o) {
        if (o instanceof java.sql.Date d) return d.toLocalDate();
        if (o instanceof java.util.Date d) return new java.sql.Date(d.getTime()).toLocalDate();
        if (o instanceof LocalDate d) return d;
        if (o instanceof LocalDateTime t) return t.toLocalDate();
        return LocalDate.now();
    }

    static long toLong(Object o) {
        if (o == null) return 0L;
        if (o instanceof Number n) return n.longValue();
        try { return Long.parseLong(String.valueOf(o)); } catch (Exception e) { return 0L; }
    }

    static Map<String, Object> map(Object... kv) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i + 1 < kv.length; i += 2) m.put((String) kv[i], kv[i + 1]);
        return m;
    }

    /** Sorted [{name,value}] distribution, top N. */
    static List<Map<String, Object>> dist(Map<String, Long> m, int limit) {
        List<Map.Entry<String, Long>> es = new ArrayList<>(m.entrySet());
        es.sort((a, b) -> Long.compare(b.getValue(), a.getValue()));
        List<Map<String, Object>> out = new ArrayList<>();
        for (Map.Entry<String, Long> e : es) {
            if (out.size() >= limit) break;
            out.add(map("name", e.getKey(), "value", e.getValue()));
        }
        return out;
    }

    static double round1(double v) { return Math.round(v * 10.0) / 10.0; }

    /** Extract purchase amount from a properties JSON string (regex, dependency-free). */
    static double parseAmount(String props) {
        if (props == null || props.isBlank()) return 0d;
        Matcher mt = AMOUNT.matcher(props);
        if (mt.find()) {
            try { return Double.parseDouble(mt.group(1)); } catch (Exception ignored) {}
        }
        return 0d;
    }

    /** Referrer URL -> coarse traffic source label. */
    static String referrerSource(String ref) {
        if (ref == null || ref.isBlank() || "direct".equalsIgnoreCase(ref)) return "直接访问";
        String lower = ref.toLowerCase(Locale.ROOT);
        if (lower.contains("baidu")) return "百度";
        if (lower.contains("google")) return "谷歌";
        if (lower.contains("weixin") || lower.contains("wechat") || lower.contains("qq.com")) return "微信";
        if (lower.contains("douyin")) return "抖音";
        if (lower.contains("shop.demo.com")) return "站内";
        try {
            String host = ref.replaceFirst("https?://", "").split("/")[0];
            return host.isBlank() ? "其他" : host;
        } catch (Exception e) {
            return "其他";
        }
    }

    /** Value of a grouping dimension for a wide row. */
    static String dimValue(Object[] row, String dim) {
        if (dim == null) dim = "device";
        return switch (dim) {
            case "os" -> label(nz(row[C_OS]));
            case "browser" -> label(nz(row[C_BROWSER]));
            case "region" -> label(nz(row[C_REGION]));
            case "city" -> label(nz(row[C_CITY]));
            case "source" -> referrerSource(nz(row[C_REFERRER]));
            case "urlPath" -> label(nz(row[C_PATH]));
            case "eventName" -> label(nz(row[C_NAME]));
            default -> label(nz(row[C_DEVICE]));
        };
    }

    static List<String> dimOptions() {
        return List.of("device", "os", "browser", "region", "city", "source", "urlPath", "eventName");
    }

    /** Human-readable funnel step label. */
    static String stepLabel(String step) {
        return switch (step) {
            case "view_product" -> "浏览商品";
            case "add_to_cart" -> "加入购物车";
            case "begin_checkout" -> "开始结算";
            case "purchase" -> "完成购买";
            case "sign_up" -> "注册";
            case "search" -> "搜索";
            default -> step;
        };
    }
}
