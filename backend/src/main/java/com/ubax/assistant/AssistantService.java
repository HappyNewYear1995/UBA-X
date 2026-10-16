package com.ubax.assistant;

import com.ubax.analytics.AnalyticsService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

/**
 * Conversational analytics assistant.
 *
 * A lightweight, deterministic natural-language engine that maps a user's prompt
 * (time range + metric + dimension + chart type) onto the existing AnalyticsService
 * and returns a structured answer: a text reply, an optional data table, and an
 * optional ready-to-render ECharts option.
 *
 * It is intentionally pluggable: the parse(...) step could later be delegated to a
 * real LLM (Qwen / DeepSeek / etc.) that returns the same {@link Intent} structure,
 * while answer(...) keeps rendering verified data from the warehouse.
 */
@Service
public class AssistantService {

    private final AnalyticsService analytics;

    public AssistantService(AnalyticsService analytics) {
        this.analytics = analytics;
    }

    // ------------------------------------------------------------------
    // public entry point
    // ------------------------------------------------------------------

    public Map<String, Object> chat(Long tid, Long appId, String message) {
        String msg = message == null ? "" : message.trim();
        Intent it = parse(msg);
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("intent", it.summary());
        try {
            answer(tid, appId, msg, it, resp);
        } catch (Exception e) {
            resp.put("reply", "抱歉，处理这个问题时出错了：" + e.getMessage());
        }
        resp.putIfAbsent("suggestions", suggestionsFor(it));
        return resp;
    }

    // ------------------------------------------------------------------
    // intent model
    // ------------------------------------------------------------------

    enum ChartType { NONE, PIE, BAR, LINE }

    static class Intent {
        LocalDate from;
        LocalDate to;
        String rangeLabel = "近7天";
        boolean today = false;

        boolean wantTrend = false;      // daily pv/uv over time
        boolean wantReport = false;     // compose a multi-part report
        boolean wantOverview = false;   // overall metrics summary

        Set<String> metrics = new LinkedHashSet<>(); // pv, uv, visits, newUsers, avgDuration, bounceRate, events
        String dimension = null;        // device, browser, region, referrer
        String topSubject = null;       // pages, events

        ChartType chart = ChartType.NONE;

        boolean matched() {
            return wantTrend || wantReport || wantOverview || !metrics.isEmpty()
                    || dimension != null || topSubject != null;
        }

        String summary() {
            List<String> parts = new ArrayList<>();
            parts.add(rangeLabel);
            if (wantReport) parts.add("报表");
            if (wantTrend) parts.add("趋势");
            if (!metrics.isEmpty()) parts.add("指标:" + String.join("/", metrics));
            if (dimension != null) parts.add("维度:" + dimension);
            if (topSubject != null) parts.add("TOP:" + topSubject);
            if (chart != ChartType.NONE) parts.add("图:" + chart.name().toLowerCase());
            return String.join(" · ", parts);
        }
    }

    // ------------------------------------------------------------------
    // parsing (rule based)
    // ------------------------------------------------------------------

    Intent parse(String m) {
        Intent it = new Intent();
        String s = m.toLowerCase();

        // ---- time range ----
        LocalDate today = LocalDate.now();
        if (has(s, "今日", "今天", "当天", "today")) {
            it.from = today; it.to = today; it.rangeLabel = "今日"; it.today = true;
        } else if (has(s, "昨日", "昨天", "yesterday")) {
            it.from = today.minusDays(1); it.to = today.minusDays(1); it.rangeLabel = "昨日";
        } else if (has(s, "近30天", "最近30天", "30天", "三十天", "本月", "这个月", "一个月", "月度")) {
            it.from = today.minusDays(29); it.to = today; it.rangeLabel = "近30天";
        } else if (has(s, "近14天", "最近14天", "14天", "两周", "近两周", " fortnight")) {
            it.from = today.minusDays(13); it.to = today; it.rangeLabel = "近14天";
        } else if (has(s, "本周", "这周", "近一周", "最近一周", "一周", "过去一周", "7天", "七天", "近7天", "weekly")) {
            it.from = today.minusDays(6); it.to = today; it.rangeLabel = "近一周";
        } else {
            it.from = today.minusDays(6); it.to = today; it.rangeLabel = "近7天";
        }

        // ---- chart type ----
        if (has(s, "饼图", "pie", "占比图", "环形图")) it.chart = ChartType.PIE;
        else if (has(s, "柱状图", "柱图", "条形图", "bar", "直方图")) it.chart = ChartType.BAR;
        else if (has(s, "折线图", "折线", "趋势图", "line", "走势图", "曲线图")) it.chart = ChartType.LINE;

        // ---- dimension (distribution) ----
        if (has(s, "设备", "终端", "访问方式", "客户端", "端分布", "device")) it.dimension = "device";
        else if (has(s, "浏览器", "browser")) it.dimension = "browser";
        else if (has(s, "地区", "地域", "省份", "城市", "地理", "区域", "region", "geo")) it.dimension = "region";
        else if (has(s, "来源", "渠道", "引流", "referrer", "source")) it.dimension = "referrer";

        // ---- trend ----
        if (has(s, "趋势", "走势", "每天", "每日", "按天", "逐日", "变化", "trend")) it.wantTrend = true;

        // ---- top subjects ----
        if (has(s, "热门页面", "top页面", "页面排行", "访问最多的页面", "页面top")) it.topSubject = "pages";
        else if (has(s, "热门事件", "top事件", "事件排行", "事件top")) it.topSubject = "events";
        else if (has(s, "页面") && !it.wantTrend) it.topSubject = "pages";
        else if (has(s, "事件")) it.topSubject = "events";

        // ---- explicit metrics ----
        if (has(s, "pv", "浏览量", "访问量", "浏览数", "页面浏览")) it.metrics.add("pv");
        if (has(s, "uv", "访客", "独立访客", "访客数")) it.metrics.add("uv");
        if (has(s, "新增用户", "新用户", "新增")) it.metrics.add("newUsers");
        if (has(s, "访问次数", "会话数", "会话次数", "访问数", "sessions", "visits")) it.metrics.add("visits");
        if (has(s, "停留", "时长", "平均停留")) it.metrics.add("avgDuration");
        if (has(s, "跳出", "跳出率", "bounce")) it.metrics.add("bounceRate");
        if (has(s, "事件数", "事件总量", "总事件")) it.metrics.add("events");

        // ---- report / overview ----
        if (has(s, "报表", "报告", "汇总", "明细表", "统计表", "report")) it.wantReport = true;
        if (has(s, "概览", "概况", "整体情况", "总体情况", "数据情况", "overview", "总览")) it.wantOverview = true;

        // ---- implicit chart defaults ----
        if (it.chart == ChartType.NONE) {
            if (has(s, "饼图", "占比", "分布", "构成", "比例")) it.chart = ChartType.PIE;
        }
        if (it.wantTrend && it.chart == ChartType.NONE) it.chart = ChartType.LINE;
        if (it.dimension != null && it.chart == ChartType.NONE) it.chart = ChartType.PIE;
        if (it.topSubject != null && it.chart == ChartType.NONE) it.chart = ChartType.BAR;

        return it;
    }

    private static boolean has(String s, String... keys) {
        for (String k : keys) if (s.contains(k.toLowerCase())) return true;
        return false;
    }

    // ------------------------------------------------------------------
    // answering
    // ------------------------------------------------------------------

    private void answer(Long tid, Long appId, String raw, Intent it, Map<String, Object> resp) {
        String from = it.from.toString();
        String to = it.to.toString();

        if (!it.matched()) {
            resp.put("reply", helpText());
            return;
        }

        // A "report" composes several sections together.
        if (it.wantReport) {
            buildReport(tid, appId, it, from, to, resp);
            return;
        }

        // Dimension distribution (device / browser / region / referrer)
        if (it.dimension != null) {
            buildDistribution(tid, appId, it, from, to, resp);
            return;
        }

        // Top pages / events
        if (it.topSubject != null) {
            buildTop(tid, appId, it, from, to, resp);
            return;
        }

        // Trend over time
        if (it.wantTrend) {
            buildTrend(tid, appId, it, from, to, resp);
            return;
        }

        // Overview / explicit metrics
        buildMetrics(tid, appId, it, from, to, resp);
    }

    // ---------------- metrics / overview ----------------

    private void buildMetrics(Long tid, Long appId, Intent it, String from, String to, Map<String, Object> resp) {
        Map<String, Object> ov = analytics.overview(tid, appId, from, to);
        Set<String> metrics = it.metrics.isEmpty() || it.wantOverview
                ? new LinkedHashSet<>(Arrays.asList("pv", "uv", "visits", "newUsers", "avgDuration", "bounceRate"))
                : it.metrics;

        StringBuilder sb = new StringBuilder();
        sb.append(it.rangeLabel).append("（").append(from).append(" ~ ").append(to).append("）");
        if (it.wantOverview || it.metrics.isEmpty()) sb.append("的整体数据概览：\n");
        else sb.append("的数据：\n");

        List<Map<String, Object>> rows = new ArrayList<>();
        for (String key : metrics) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("metric", metricLabel(key));
            row.put("value", metricValueText(key, ov));
            rows.add(row);
            sb.append("· ").append(metricLabel(key)).append("：").append(metricValueText(key, ov)).append("\n");
        }
        resp.put("reply", sb.toString().trim());
        resp.put("table", table(Arrays.asList("metric", "value"), Arrays.asList("指标", "数值"), rows));
        resp.put("range", rangeMap(it));
    }

    // ---------------- trend ----------------

    private void buildTrend(Long tid, Long appId, Intent it, String from, String to, Map<String, Object> resp) {
        List<Map<String, Object>> trend = analytics.trend(tid, appId, from, to);
        long pv = 0, uv = 0, peakPv = -1;
        String peakDay = "-";
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map<String, Object> d : trend) {
            long p = lng(d.get("pv")), u = lng(d.get("uv"));
            pv += p; uv += u;
            if (p > peakPv) { peakPv = p; peakDay = str(d.get("date")); }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("date", str(d.get("date")));
            row.put("pv", p);
            row.put("uv", u);
            rows.add(row);
        }
        StringBuilder sb = new StringBuilder();
        sb.append(it.rangeLabel).append("的访问趋势：累计 PV ").append(nf(pv))
                .append("，累计 UV ").append(nf(uv))
                .append("；其中 ").append(peakDay).append(" 的 PV 最高，达 ").append(nf(peakPv)).append("。");
        resp.put("reply", sb.toString());
        resp.put("range", rangeMap(it));
        if (it.chart == ChartType.BAR) {
            resp.put("chart", barChart("每日访问量趋势",
                    rows.stream().map(r -> str(r.get("date"))).toList(),
                    Arrays.asList("PV", "UV"),
                    Arrays.asList(rows.stream().map(r -> (Object) lng(r.get("pv"))).toList(),
                            rows.stream().map(r -> (Object) lng(r.get("uv"))).toList())));
        } else {
            resp.put("chart", lineChart("每日访问量趋势",
                    rows.stream().map(r -> str(r.get("date"))).toList(),
                    Arrays.asList("PV", "UV"),
                    Arrays.asList(rows.stream().map(r -> (Object) lng(r.get("pv"))).toList(),
                            rows.stream().map(r -> (Object) lng(r.get("uv"))).toList())));
        }
        resp.put("table", table(Arrays.asList("date", "pv", "uv"), Arrays.asList("日期", "PV", "UV"), rows));
    }

    // ---------------- distributions ----------------

    private void buildDistribution(Long tid, Long appId, Intent it, String from, String to, Map<String, Object> resp) {
        Map<String, Object> dist = analytics.distributions(tid, appId, from, to);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> data = (List<Map<String, Object>>) dist.get(it.dimension);
        if (data == null) data = Collections.emptyList();
        String dimLabel = dimensionLabel(it.dimension);

        long total = 0;
        for (Map<String, Object> d : data) total += lng(d.get("value"));
        StringBuilder sb = new StringBuilder();
        sb.append(it.rangeLabel).append("按「").append(dimLabel).append("」的分布");
        if (!data.isEmpty()) {
            Map<String, Object> top = data.get(0);
            double share = total == 0 ? 0 : round(lng(top.get("value")) * 100.0 / total);
            sb.append("：共 ").append(nf(total)).append(" 次，其中「").append(str(top.get("name")))
                    .append("」最多，占 ").append(share).append("%。");
        } else {
            sb.append("：暂无数据。");
        }
        resp.put("reply", sb.toString());
        resp.put("range", rangeMap(it));

        List<Map<String, Object>> pieData = new ArrayList<>();
        for (Map<String, Object> d : data) {
            Map<String, Object> p = new LinkedHashMap<>();
            p.put("name", str(d.get("name")));
            p.put("value", lng(d.get("value")));
            pieData.add(p);
        }
        ChartType ct = it.chart == ChartType.NONE ? ChartType.PIE : it.chart;
        String title = it.rangeLabel + " · " + dimLabel + "分布";
        if (ct == ChartType.BAR) {
            resp.put("chart", barChart(title,
                    pieData.stream().map(d -> str(d.get("name"))).toList(),
                    Collections.singletonList(dimLabel),
                    Collections.singletonList(pieData.stream().map(d -> (Object) lng(d.get("value"))).toList())));
        } else if (ct == ChartType.LINE) {
            resp.put("chart", lineChart(title,
                    pieData.stream().map(d -> str(d.get("name"))).toList(),
                    Collections.singletonList(dimLabel),
                    Collections.singletonList(pieData.stream().map(d -> (Object) lng(d.get("value"))).toList())));
        } else {
            resp.put("chart", pieChart(title, pieData));
        }
        resp.put("table", table(Arrays.asList("name", "value"), Arrays.asList(dimLabel, "次数"), pieData));
    }

    // ---------------- top pages / events ----------------

    private void buildTop(Long tid, Long appId, Intent it, String from, String to, Map<String, Object> resp) {
        if ("pages".equals(it.topSubject)) {
            List<Map<String, Object>> pages = analytics.topPages(tid, appId, from, to, 10);
            StringBuilder sb = new StringBuilder(it.rangeLabel + "的热门页面 TOP" + Math.min(10, pages.size()) + "：\n");
            List<Map<String, Object>> rows = new ArrayList<>();
            int i = 1;
            for (Map<String, Object> p : pages) {
                sb.append(i++).append(". ").append(str(p.get("path")))
                        .append(" — PV ").append(nf(lng(p.get("pv"))))
                        .append("，UV ").append(nf(lng(p.get("uv")))).append("\n");
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("path", str(p.get("path")));
                row.put("pv", lng(p.get("pv")));
                row.put("uv", lng(p.get("uv")));
                rows.add(row);
            }
            resp.put("reply", pages.isEmpty() ? (it.rangeLabel + "暂无页面数据。") : sb.toString().trim());
            resp.put("range", rangeMap(it));
            resp.put("table", table(Arrays.asList("path", "pv", "uv"), Arrays.asList("页面路径", "PV", "UV"), rows));
            resp.put("chart", barChart(it.rangeLabel + " · 热门页面 TOP10",
                    rows.stream().map(r -> str(r.get("path"))).toList(),
                    Arrays.asList("PV", "UV"),
                    Arrays.asList(rows.stream().map(r -> (Object) lng(r.get("pv"))).toList(),
                            rows.stream().map(r -> (Object) lng(r.get("uv"))).toList())));
        } else {
            List<Map<String, Object>> events = analytics.topEvents(tid, appId, from, to, 10);
            StringBuilder sb = new StringBuilder(it.rangeLabel + "的热门事件 TOP" + Math.min(10, events.size()) + "：\n");
            List<Map<String, Object>> rows = new ArrayList<>();
            int i = 1;
            for (Map<String, Object> e : events) {
                sb.append(i++).append(". ").append(str(e.get("event")))
                        .append(" — ").append(nf(lng(e.get("count")))).append(" 次，")
                        .append(nf(lng(e.get("users")))).append(" 人\n");
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("event", str(e.get("event")));
                row.put("count", lng(e.get("count")));
                row.put("users", lng(e.get("users")));
                rows.add(row);
            }
            resp.put("reply", events.isEmpty() ? (it.rangeLabel + "暂无事件数据。") : sb.toString().trim());
            resp.put("range", rangeMap(it));
            resp.put("table", table(Arrays.asList("event", "count", "users"), Arrays.asList("事件", "次数", "用户数"), rows));
            resp.put("chart", barChart(it.rangeLabel + " · 热门事件 TOP10",
                    rows.stream().map(r -> str(r.get("event"))).toList(),
                    Collections.singletonList("触发次数"),
                    Collections.singletonList(rows.stream().map(r -> (Object) lng(r.get("count"))).toList())));
        }
    }

    // ---------------- composite report ----------------

    private void buildReport(Long tid, Long appId, Intent it, String from, String to, Map<String, Object> resp) {
        Map<String, Object> ov = analytics.overview(tid, appId, from, to);
        List<Map<String, Object>> trend = analytics.trend(tid, appId, from, to);
        Map<String, Object> dist = analytics.distributions(tid, appId, from, to);
        List<Map<String, Object>> pages = analytics.topPages(tid, appId, from, to, 5);

        StringBuilder sb = new StringBuilder();
        sb.append("📊 ").append(it.rangeLabel).append("访问数据报表（").append(from).append(" ~ ").append(to).append("）\n\n");
        sb.append("【核心指标】\n");
        sb.append("· 浏览量 PV：").append(nf(lng(ov.get("pv")))).append("\n");
        sb.append("· 访客数 UV：").append(nf(lng(ov.get("uv")))).append("\n");
        sb.append("· 访问次数：").append(nf(lng(ov.get("visits")))).append("\n");
        sb.append("· 新增用户：").append(nf(lng(ov.get("newUsers")))).append("\n");
        sb.append("· 平均停留：").append(ov.get("avgDuration")).append(" 秒\n");
        sb.append("· 跳出率：").append(ov.get("bounceRate")).append("%\n\n");

        // daily table
        List<Map<String, Object>> rows = new ArrayList<>();
        long pv = 0, uv = 0, peakPv = -1;
        String peakDay = "-";
        for (Map<String, Object> d : trend) {
            long p = lng(d.get("pv")), u = lng(d.get("uv"));
            pv += p; uv += u;
            if (p > peakPv) { peakPv = p; peakDay = str(d.get("date")); }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("date", str(d.get("date")));
            row.put("pv", p);
            row.put("uv", u);
            rows.add(row);
        }
        sb.append("【每日明细】累计 PV ").append(nf(pv)).append("，UV ").append(nf(uv))
                .append("，峰值日 ").append(peakDay).append("（PV ").append(nf(peakPv)).append("）。\n\n");

        // device summary
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> devices = (List<Map<String, Object>>) dist.getOrDefault("device", Collections.emptyList());
        if (!devices.isEmpty()) {
            sb.append("【访问方式】");
            List<String> segs = new ArrayList<>();
            long dt = 0;
            for (Map<String, Object> d : devices) dt += lng(d.get("value"));
            for (Map<String, Object> d : devices) {
                double share = dt == 0 ? 0 : round(lng(d.get("value")) * 100.0 / dt);
                segs.add(str(d.get("name")) + " " + share + "%");
            }
            sb.append(String.join("，", segs)).append("。\n\n");
        }

        if (!pages.isEmpty()) {
            sb.append("【热门页面 TOP5】\n");
            int i = 1;
            for (Map<String, Object> p : pages) {
                sb.append(i++).append(". ").append(str(p.get("path")))
                        .append("（PV ").append(nf(lng(p.get("pv")))).append("）\n");
            }
        }

        resp.put("reply", sb.toString().trim());
        resp.put("range", rangeMap(it));
        resp.put("table", table(Arrays.asList("date", "pv", "uv"), Arrays.asList("日期", "PV", "UV"), rows));
        // report defaults to a line trend chart; honour an explicit chart request
        List<Map<String, Object>> chartData = new ArrayList<>();
        for (Map<String, Object> d : devices) {
            Map<String, Object> p = new LinkedHashMap<>();
            p.put("name", str(d.get("name")));
            p.put("value", lng(d.get("value")));
            chartData.add(p);
        }
        if (it.chart == ChartType.PIE && !chartData.isEmpty()) {
            resp.put("chart", pieChart(it.rangeLabel + " · 访问方式占比", chartData));
        } else {
            resp.put("chart", lineChart(it.rangeLabel + " · 每日访问趋势",
                    rows.stream().map(r -> str(r.get("date"))).toList(),
                    Arrays.asList("PV", "UV"),
                    Arrays.asList(rows.stream().map(r -> (Object) lng(r.get("pv"))).toList(),
                            rows.stream().map(r -> (Object) lng(r.get("uv"))).toList())));
        }
    }

    // ------------------------------------------------------------------
    // chart builders (produce ready-to-use ECharts options)
    // ------------------------------------------------------------------

    private Map<String, Object> pieChart(String title, List<Map<String, Object>> data) {
        Map<String, Object> c = new LinkedHashMap<>();
        c.put("__type", "pie");
        c.put("title", titleMap(title));
        c.put("tooltip", map("trigger", "item", "formatter", "{b}: {c} ({d}%)"));
        c.put("legend", map("bottom", 0, "type", "scroll"));
        Map<String, Object> series = new LinkedHashMap<>();
        series.put("type", "pie");
        series.put("radius", Arrays.asList("42%", "68%"));
        series.put("center", Arrays.asList("50%", "46%"));
        series.put("data", data);
        series.put("label", map("formatter", "{b}\n{d}%"));
        c.put("series", Collections.singletonList(series));
        return c;
    }

    private Map<String, Object> barChart(String title, List<String> categories, List<String> seriesNames, List<List<Object>> seriesData) {
        return axisChart("bar", title, categories, seriesNames, seriesData);
    }

    private Map<String, Object> lineChart(String title, List<String> categories, List<String> seriesNames, List<List<Object>> seriesData) {
        return axisChart("line", title, categories, seriesNames, seriesData);
    }

    private Map<String, Object> axisChart(String type, String title, List<String> categories, List<String> seriesNames, List<List<Object>> seriesData) {
        Map<String, Object> c = new LinkedHashMap<>();
        c.put("__type", type);
        c.put("title", titleMap(title));
        c.put("tooltip", map("trigger", "axis"));
        c.put("legend", map("top", 26, "data", seriesNames));
        c.put("grid", map("left", 55, "right", 24, "top", 64, "bottom", 70, "containLabel", true));
        Map<String, Object> xAxis = new LinkedHashMap<>();
        xAxis.put("type", "category");
        xAxis.put("data", categories);
        xAxis.put("axisLabel", map("interval", 0, "rotate", categories.size() > 6 ? 30 : 0));
        c.put("xAxis", xAxis);
        c.put("yAxis", map("type", "value"));
        List<Map<String, Object>> series = new ArrayList<>();
        String[] palette = {"#2b5cff", "#00c2a8", "#ff9f43", "#6a3cff", "#f5576c"};
        for (int i = 0; i < seriesNames.size(); i++) {
            Map<String, Object> s = new LinkedHashMap<>();
            s.put("name", seriesNames.get(i));
            s.put("type", type);
            if ("line".equals(type)) { s.put("smooth", true); s.put("areaStyle", map("opacity", 0.12)); }
            if ("bar".equals(type)) s.put("barMaxWidth", 34);
            s.put("itemStyle", map("color", palette[i % palette.length]));
            s.put("data", seriesData.get(i));
            series.add(s);
        }
        c.put("series", series);
        return c;
    }

    // ------------------------------------------------------------------
    // small helpers
    // ------------------------------------------------------------------

    private Map<String, Object> table(List<String> keys, List<String> labels, List<Map<String, Object>> rows) {
        List<Map<String, Object>> cols = new ArrayList<>();
        for (int i = 0; i < keys.size(); i++) cols.add(map("key", keys.get(i), "label", labels.get(i)));
        Map<String, Object> t = new LinkedHashMap<>();
        t.put("columns", cols);
        t.put("rows", rows);
        return t;
    }

    private Map<String, Object> rangeMap(Intent it) {
        return map("label", it.rangeLabel, "from", it.from.toString(), "to", it.to.toString());
    }

    private Map<String, Object> titleMap(String text) {
        return map("text", text, "left", "center", "textStyle", map("fontSize", 14));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> map(Object... kv) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i + 1 < kv.length; i += 2) m.put(String.valueOf(kv[i]), kv[i + 1]);
        return m;
    }

    private static String metricLabel(String key) {
        switch (key) {
            case "pv": return "浏览量 PV";
            case "uv": return "访客数 UV";
            case "visits": return "访问次数";
            case "newUsers": return "新增用户";
            case "avgDuration": return "平均停留(秒)";
            case "bounceRate": return "跳出率(%)";
            case "events": return "事件总数";
            default: return key;
        }
    }

    private String metricValueText(String key, Map<String, Object> ov) {
        switch (key) {
            case "avgDuration": return ov.get("avgDuration") + " 秒";
            case "bounceRate": return ov.get("bounceRate") + " %";
            default: return nf(lng(ov.get(key)));
        }
    }

    private static String dimensionLabel(String dim) {
        switch (dim) {
            case "device": return "访问方式(设备)";
            case "browser": return "浏览器";
            case "region": return "地区";
            case "referrer": return "来源渠道";
            default: return dim;
        }
    }

    private List<String> suggestionsFor(Intent it) {
        List<String> s = new ArrayList<>();
        s.add("今日的 PV 和 UV 是多少？");
        s.add("生成近一周的访问数据报表");
        s.add("近一周访问方式的饼图");
        s.add("最近14天的访问趋势");
        s.add("热门页面 TOP10");
        return s;
    }

    private String helpText() {
        return "我可以帮你用自然语言查询行为数据、生成报表和图表。试试这样问我：\n"
                + "· 今日的 PV / UV 是多少\n"
                + "· 生成近一周的访问数据报表\n"
                + "· 近一周访问方式(设备)的饼图\n"
                + "· 最近30天各浏览器占比\n"
                + "· 每天的访问趋势折线图\n"
                + "· 热门页面 TOP10 / 热门事件排行\n\n"
                + "支持的时间范围：今日、昨日、近一周、近14天、近30天；"
                + "支持的图表：饼图、柱状图、折线图。";
    }

    private static long lng(Object o) {
        if (o == null) return 0L;
        if (o instanceof Number) return ((Number) o).longValue();
        try { return Long.parseLong(o.toString()); } catch (Exception e) { return 0L; }
    }

    private static String str(Object o) { return o == null ? "" : o.toString(); }

    private static String nf(long v) { return String.format("%,d", v); }

    private static double round(double v) { return Math.round(v * 100.0) / 100.0; }
}
