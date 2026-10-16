package com.ubax.profile;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Deterministic "AI" profile-insight engine.
 *
 * Mirrors the philosophy of {@code AssistantService}: rather than calling an external
 * LLM (which would need keys/network and could hallucinate), it turns the user's real,
 * already-aggregated behaviour data into a structured narrative — an engagement score,
 * a persona label, key highlights, churn risks and concrete运营 recommendations.
 *
 * Everything here is derived from the {@code bundle} produced by
 * {@link ProfileController#buildBundle}, so the analysis is always grounded in verified
 * warehouse data. The scoring/segmentation heuristics could later be swapped for an LLM
 * call that receives the same bundle and returns the same response shape.
 */
@Service
public class ProfileInsightService {

    /** Analyse a profile bundle and return the insight payload for the detail page. */
    public Map<String, Object> analyze(Map<String, Object> bundle) {
        @SuppressWarnings("unchecked")
        Map<String, Object> stats = (Map<String, Object>) bundle.getOrDefault("stats", Collections.emptyMap());
        @SuppressWarnings("unchecked")
        Map<String, Object> derived = (Map<String, Object>) bundle.getOrDefault("derived", Collections.emptyMap());
        @SuppressWarnings("unchecked")
        Map<String, Object> profile = asMap(bundle.get("profile"));
        List<Map<String, Object>> typeDist = asList(bundle.get("eventTypeDist"));
        List<Map<String, Object>> topPages = asList(bundle.get("topPages"));
        List<Map<String, Object>> topEvents = asList(bundle.get("topEvents"));
        List<Map<String, Object>> hourDist = asList(bundle.get("hourDist"));
        List<Map<String, Object>> sessions = asList(bundle.get("sessions"));

        long eventTotal = lng(stats.get("eventTotal"));
        long sampledEvents = lng(stats.get("sampledEvents"));
        boolean truncated = Boolean.TRUE.equals(stats.get("truncated")) || (sampledEvents > 0 && eventTotal > sampledEvents);
        long activeDays = lng(stats.get("activeDays"));
        long sessionCount = lng(stats.get("sessionCount"));
        long pageviewCount = lng(stats.get("pageviewCount"));
        long visitCount = lng(stats.get("visitCount"));
        double avgPerDay = dbl(stats.get("avgEventsPerDay"));
        long avgDwellMs = lng(stats.get("avgDwellMs"));
        long lifetimeDays = lng(stats.get("lifetimeDays"));
        Long daysSince = stats.get("daysSinceLastSeen") == null ? null : lng(stats.get("daysSinceLastSeen"));

        long orderCount = lng(derived.get("orderCount"));
        long totalAmount = lng(derived.get("totalAmount"));
        String currency = str(derived.getOrDefault("currency", "CNY"));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> keywords = (List<Map<String, Object>>) derived.getOrDefault("keywords", Collections.emptyList());

        String device = str(profile.get("device"));
        String region = str(profile.get("region"));
        String city = str(profile.get("city"));
        String tags = str(profile.get("tags"));

        // ---- engagement score (0..100), weighted composite of real signals ----
        int score = 0;
        score += (int) Math.min(25, Math.round(eventTotal * 0.5));                 // volume
        score += (int) Math.min(20, activeDays * 4);                               // frequency
        score += (int) (daysSince == null ? 10 : Math.max(0, 20 - daysSince * 5)); // recency
        score += (int) Math.min(15, Math.round(avgPerDay));                        // depth
        score += (int) Math.min(10, Math.round(avgDwellMs / 60000.0 * 3));         // dwell (3pt/min)
        score += orderCount > 0 ? 10 : 0;                                          // conversion
        score = Math.max(0, Math.min(100, score));

        String level;
        String levelTone;
        if (score >= 80) { level = "高价值活跃"; levelTone = "success"; }
        else if (score >= 60) { level = "活跃用户"; levelTone = "success"; }
        else if (score >= 40) { level = "一般活跃"; levelTone = "warning"; }
        else if (score >= 20) { level = "低活跃"; levelTone = "warning"; }
        else { level = "沉默用户"; levelTone = "danger"; }

        // ---- churn risk ----
        String churn;
        String churnTone;
        if (daysSince != null && daysSince >= 7) { churn = "高"; churnTone = "danger"; }
        else if (daysSince != null && daysSince >= 3) { churn = "中"; churnTone = "warning"; }
        else if (sessionCount <= 1 && activeDays <= 1) { churn = "中"; churnTone = "warning"; }
        else { churn = "低"; churnTone = "success"; }

        // ---- persona ----
        String persona = buildPersona(device, tags, orderCount, pageviewCount, eventTotal, topEvents, keywords);

        // ---- peak hour / daypart ----
        int peakHour = -1;
        long peakCnt = -1;
        for (Map<String, Object> h : hourDist) {
            long c = lng(h.get("count"));
            if (c > peakCnt) { peakCnt = c; peakHour = (int) lng(h.get("hour")); }
        }
        String daypart = peakHour < 0 ? null : daypart(peakHour);

        String topPage = topPages.isEmpty() ? null : firstNonEmpty(str(topPages.get(0).get("title")), str(topPages.get(0).get("key")));
        String topEvent = meaningfulEvent(topEvents);
        String topKeyword = keywords.isEmpty() ? null : str(keywords.get(0).get("keyword"));
        String dominantType = typeDist.isEmpty() ? null : str(typeDist.get(0).get("type"));

        // ---- summary paragraph ----
        StringBuilder sum = new StringBuilder();
        sum.append("该用户");
        if (lifetimeDays > 0) sum.append("生命周期 ").append(lifetimeDays).append(" 天，");
        sum.append("累计产生 ").append(eventTotal).append(" 个行为事件，覆盖 ").append(activeDays)
                .append(" 个活跃日、").append(sessionCount).append(" 次会话，日均 ").append(trim(avgPerDay)).append(" 个事件");
        if (truncated) sum.append("（活跃日 / 会话 / 分布等基于最近 ").append(sampledEvents).append(" 条事件采样）");
        if (avgDwellMs > 0) sum.append("，单次平均停留 ").append(secondsText(avgDwellMs / 1000));
        sum.append("。");
        if (topPage != null) sum.append("最常访问「").append(topPage).append("」");
        if (topEvent != null) sum.append(topPage != null ? "，" : "。最常").append("高频事件为「").append(topEvent).append("」");
        sum.append("。");
        if (orderCount > 0) {
            sum.append("已完成 ").append(orderCount).append(" 笔订单、累计消费 ")
                    .append(money(totalAmount)).append(" ").append(currency).append("，属于已转化用户。");
        } else if (pageviewCount >= 3) {
            sum.append("浏览了 ").append(pageviewCount).append(" 个页面但尚未下单，属于有购买意向的潜在转化用户。");
        } else {
            sum.append("目前尚无购买行为，处于早期探索阶段。");
        }

        // ---- highlights ----
        List<Map<String, Object>> highlights = new ArrayList<>();
        highlights.add(item("活跃度", level + " · 综合评分 " + score + "/100，近 " + activeDays + " 天贡献 " + eventTotal + " 个事件", toneOf(score >= 60, "positive", "neutral")));
        if (daypart != null && peakCnt > 0) {
            highlights.add(item("偏好时段", "最活跃于" + daypart + "（" + peakHour + " 点前后，占比最高 " + peakCnt + " 次）", "info"));
        }
        if (topPage != null) highlights.add(item("内容偏好", "最常访问页面「" + topPage + "」" + (topKeyword != null ? "，搜索关注「" + topKeyword + "」" : ""), "info"));
        if (dominantType != null) highlights.add(item("行为构成", "以「" + typeLabel(dominantType) + "」类事件为主" + (typeDist.size() > 1 ? "，另涉及 " + (typeDist.size() - 1) + " 类行为" : ""), "neutral"));
        if (orderCount > 0) highlights.add(item("消费贡献", "累计 " + orderCount + " 单 / " + money(totalAmount) + " " + currency + "，客单价 " + money(orderCount == 0 ? 0 : totalAmount / orderCount) + " " + currency, "positive"));
        List<String> loc = new ArrayList<>();
        if (!region.isBlank()) loc.add(region);
        if (!city.isBlank() && !city.equals(region)) loc.add(city);
        String locText = String.join(" ", loc);
        String devText = deviceLabel(device);
        if (!locText.isBlank() || !devText.isBlank()) {
            highlights.add(item("设备地域", (devText.isBlank() ? "" : devText) + (!devText.isBlank() && !locText.isBlank() ? " · " : "") + (locText.isBlank() ? "未知地区" : locText), "neutral"));
        }

        // ---- risks ----
        List<Map<String, Object>> risks = new ArrayList<>();
        if (daysSince != null && daysSince >= 3) {
            risks.add(item("流失风险", "已 " + daysSince + " 天未活跃" + (daysSince >= 7 ? "，流失概率较高" : "，需关注") + "。", daysSince >= 7 ? "danger" : "warning"));
        }
        if (orderCount == 0 && pageviewCount >= 5) {
            risks.add(item("转化阻力", "浏览 " + pageviewCount + " 个页面仍未下单，购买链路可能存在价格、信任或体验障碍。", "warning"));
        }
        if (sessionCount <= 1) {
            risks.add(item("粘性偏低", "仅有 " + sessionCount + " 次会话，尚未形成稳定访问习惯。", "warning"));
        }
        if (avgDwellMs > 0 && avgDwellMs < 30000 && eventTotal >= 5) {
            risks.add(item("停留过短", "平均停留仅 " + secondsText(avgDwellMs / 1000) + "，内容吸引力或加载体验有待提升。", "warning"));
        }
        if (risks.isEmpty()) {
            risks.add(item("健康度", "暂未发现明显风险信号，用户处于良性活跃状态。", "positive"));
        }

        // ---- recommendations ----
        List<String> recs = new ArrayList<>();
        if (orderCount == 0 && pageviewCount >= 3) {
            recs.add("浏览充分但未转化：推送首单优惠 / 限时折扣，并在其高频页面挂载购物车挽回与信任背书（评价、保障）。");
        }
        if (orderCount > 0) {
            recs.add("已转化用户：纳入会员 / 积分体系，基于「" + (topKeyword != null ? topKeyword : "历史购买品类") + "」做复购与交叉推荐，提升 LTV。");
        }
        if (daysSince != null && daysSince >= 3) {
            recs.add("启动召回：通过个性化 Push / 短信唤醒，附带回访激励（优惠券或内容更新提醒）。");
        }
        if (daypart != null) {
            recs.add("触达时机：优先在" + daypart + "（" + peakHour + " 点前后）投放消息，打开率更高。");
        }
        if ("mobile".equalsIgnoreCase(device) || "移动端".equals(device)) {
            recs.add("移动端为主：以 App 推送 / 移动专享券为主要触达渠道，保证移动端落地页体验。");
        } else if ("desktop".equalsIgnoreCase(device) || "桌面端".equals(device)) {
            recs.add("桌面端为主：适合邮件 / 站内信触达，可承载更丰富的详情与比价内容。");
        }
        if (topEvent != null) {
            recs.add("兴趣运营：围绕高频行为「" + topEvent + "」编排相关内容或活动，延长停留与回访。");
        }
        if (sessionCount <= 1) {
            recs.add("培养习惯：通过签到、收藏、订阅等轻互动提升会话频次与粘性。");
        }
        // guarantee a small, non-redundant set
        if (recs.isEmpty()) {
            recs.add("保持现有运营节奏，持续监测其活跃与转化指标变化。");
        }
        List<String> recommendations = recs.size() > 5 ? recs.subList(0, 5) : recs;

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("score", score);
        out.put("level", level);
        out.put("levelTone", levelTone);
        out.put("churn", churn);
        out.put("churnTone", churnTone);
        out.put("persona", persona);
        out.put("summary", sum.toString());
        out.put("highlights", highlights);
        out.put("risks", risks);
        out.put("recommendations", recommendations);
        out.put("basedOn", eventTotal);
        out.put("sampledEvents", sampledEvents);
        out.put("truncated", truncated);
        out.put("generatedAt", LocalDateTime.now().toString());
        return out;
    }

    // ------------------------------------------------------------------
    // persona composition
    // ------------------------------------------------------------------

    private String buildPersona(String device, String tags, long orderCount, long pageviewCount,
                                long eventTotal, List<Map<String, Object>> topEvents, List<Map<String, Object>> keywords) {
        List<String> parts = new ArrayList<>();
        // frequency prefix from tags (already computed by the segmentation job)
        if (tags != null && tags.contains("高频")) parts.add("高频");
        else if (eventTotal >= 40) parts.add("高频");
        else if (eventTotal >= 15) parts.add("中频");
        else parts.add("低频");

        String dev = deviceLabel(device);
        if (!dev.isBlank()) parts.add(dev);

        if (!keywords.isEmpty()) parts.add("「" + str(keywords.get(0).get("keyword")) + "」兴趣");
        else {
            String ev = meaningfulEvent(topEvents);
            if (ev != null) parts.add("「" + ev + "」倾向");
        }

        if (orderCount > 0) parts.add("已转化购买用户");
        else if (pageviewCount >= 5) parts.add("浏览型潜在用户");
        else parts.add("探索型新访客");

        return String.join(" · ", parts);
    }

    // ------------------------------------------------------------------
    // helpers
    // ------------------------------------------------------------------

    private static Map<String, Object> item(String label, String text, String tone) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("label", label);
        m.put("text", text);
        m.put("tone", tone);
        return m;
    }

    private static String toneOf(boolean good, String yes, String no) { return good ? yes : no; }

    private static String daypart(int h) {
        if (h >= 5 && h < 9) return "清晨";
        if (h >= 9 && h < 12) return "上午";
        if (h >= 12 && h < 14) return "午间";
        if (h >= 14 && h < 18) return "下午";
        if (h >= 18 && h < 23) return "晚间";
        return "深夜";
    }

    private static String deviceLabel(String device) {
        if (device == null) return "";
        String d = device.toLowerCase();
        if (d.contains("mobile") || d.contains("移动") || d.contains("phone") || d.contains("ios") || d.contains("android")) return "移动端";
        if (d.contains("desktop") || d.contains("桌面") || d.contains("pc")) return "桌面端";
        if (d.contains("tablet") || d.contains("平板")) return "平板端";
        return device;
    }

    /**
     * Pick the first event name that carries real semantic meaning, skipping generic
     * SDK/auto names like {@code $click}, {@code $pageview}, {@code pageview}, etc.
     * Returns null when only generic events exist.
     */
    private static String meaningfulEvent(List<Map<String, Object>> topEvents) {
        for (Map<String, Object> e : topEvents) {
            String k = str(e.get("key")).trim();
            if (k.isEmpty()) continue;
            String lk = k.toLowerCase();
            if (k.startsWith("$") || lk.startsWith("ubax") || lk.startsWith("auto")) continue;
            if (lk.equals("click") || lk.equals("pageview") || lk.equals("view") || lk.equals("leave")) continue;
            return k;
        }
        return null;
    }

    private static String typeLabel(String t) {        switch (t == null ? "" : t) {
            case "pageview": return "页面浏览";
            case "click": return "点击";
            case "event": return "交互事件";
            case "custom": return "自定义事件";
            case "leave": return "离开";
            default: return t;
        }
    }

    private static String secondsText(long s) {
        if (s < 60) return s + " 秒";
        long m = s / 60, r = s % 60;
        if (m < 60) return r == 0 ? m + " 分钟" : m + " 分 " + r + " 秒";
        return (m / 60) + " 小时 " + (m % 60) + " 分";
    }

    private static String money(long v) { return String.format("%,d", v); }

    private static String trim(double v) {
        if (v == Math.rint(v)) return String.valueOf((long) v);
        return String.valueOf(Math.round(v * 10) / 10.0);
    }

    private static String firstNonEmpty(String a, String b) { return (a != null && !a.isBlank()) ? a : b; }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> asMap(Object o) {
        if (o instanceof Map) return (Map<String, Object>) o;
        // UserProfile entity is serialised by Jackson only at the HTTP layer; in-process it
        // arrives as the entity. Reflectively pull the few fields we need into a map.
        Map<String, Object> m = new LinkedHashMap<>();
        if (o == null) return m;
        for (String f : new String[]{"device", "os", "browser", "region", "city", "tags", "distinctId", "userId", "lastUrl"}) {
            try {
                java.lang.reflect.Method g = o.getClass().getMethod("get" + Character.toUpperCase(f.charAt(0)) + f.substring(1));
                m.put(f, g.invoke(o));
            } catch (Exception ignore) { /* field absent */ }
        }
        return m;
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> asList(Object o) {
        if (o instanceof List) {
            List<Map<String, Object>> out = new ArrayList<>();
            for (Object e : (List<Object>) o) if (e instanceof Map) out.add((Map<String, Object>) e);
            return out;
        }
        return Collections.emptyList();
    }

    private static long lng(Object o) {
        if (o == null) return 0L;
        if (o instanceof Number) return ((Number) o).longValue();
        try { return Long.parseLong(o.toString()); } catch (Exception e) { return 0L; }
    }

    private static double dbl(Object o) {
        if (o == null) return 0d;
        if (o instanceof Number) return ((Number) o).doubleValue();
        try { return Double.parseDouble(o.toString()); } catch (Exception e) { return 0d; }
    }

    private static String str(Object o) { return o == null ? "" : o.toString(); }
}
