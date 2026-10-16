package com.ubax.ext;

import com.ubax.event.repo.EventRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.ubax.ext.ExtSupport.*;

/**
 * Insight extension apps: 异常预警 (anomaly), 页面性能与体验分析 (page-experience),
 * and 事件探索 / 自助看板 (event-explore + event-meta).
 */
@Service
public class ExtInsightService {

    private static final Map<String, String> DIM_LABELS = new LinkedHashMap<>() {{
        put("device", "设备");
        put("os", "操作系统");
        put("browser", "浏览器");
        put("region", "地域");
        put("city", "城市");
        put("source", "流量来源");
        put("urlPath", "页面路径");
        put("eventName", "事件名");
    }};

    private final EventRepository eventRepository;

    public ExtInsightService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    /* ------------------------------------------------------------------ */
    /* 异常预警                                                             */
    /* ------------------------------------------------------------------ */

    public Map<String, Object> anomaly(Long tid, Long appId, String from, String to, String metric) {
        LocalDateTime[] r = range(from, to, 30);
        List<Object[]> rows = eventRepository.extWideRows(tid, appId == null ? 0L : appId, r[0], r[1]);
        String m = metric == null || metric.isBlank() ? "events" : metric;

        Map<LocalDate, long[]> daily = new TreeMap<>();            // day -> [events, pv]
        Map<LocalDate, Set<String>> dailyUsers = new HashMap<>();
        for (Object[] row : rows) {
            LocalDate d = toDate(row[C_DAY]);
            long[] c = daily.computeIfAbsent(d, k -> new long[2]);
            c[0]++;
            if ("pageview".equals(nz(row[C_TYPE]))) c[1]++;
            String uid = nz(row[C_DISTINCT]);
            if (!uid.isBlank()) dailyUsers.computeIfAbsent(d, k -> new HashSet<>()).add(uid);
        }

        List<Map<String, Object>> series = new ArrayList<>();
        List<Double> values = new ArrayList<>();
        for (Map.Entry<LocalDate, long[]> e : daily.entrySet()) {
            LocalDate d = e.getKey();
            long events = e.getValue()[0], pv = e.getValue()[1];
            long uv = dailyUsers.getOrDefault(d, Collections.emptySet()).size();
            double val = switch (m) {
                case "pv" -> pv;
                case "uv" -> uv;
                default -> events;
            };
            values.add(val);
            series.add(map("date", d.toString(), "events", events, "pv", pv, "uv", uv, "value", (long) val));
        }

        double mean = values.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double variance = values.stream().mapToDouble(v -> (v - mean) * (v - mean)).average().orElse(0);
        double sd = Math.sqrt(variance);

        List<Map<String, Object>> anomalies = new ArrayList<>();
        for (int i = 0; i < series.size(); i++) {
            double v = values.get(i);
            if (sd <= 0) continue;
            double z = (v - mean) / sd;
            if (Math.abs(z) >= 2.0) {
                Map<String, Object> s = series.get(i);
                double prev = i > 0 ? values.get(i - 1) : v;
                double changePct = prev == 0 ? 0 : round1((v - prev) * 100.0 / prev);
                anomalies.add(map(
                        "date", s.get("date"),
                        "metric", m,
                        "value", (long) v,
                        "expected", Math.round(mean),
                        "sigma", round1(Math.abs(z)),
                        "direction", z > 0 ? "激增" : "骤降",
                        "changePct", changePct));
            }
        }
        anomalies.sort((a, b) -> Double.compare(((Number) b.get("sigma")).doubleValue(), ((Number) a.get("sigma")).doubleValue()));

        String level = anomalies.isEmpty() ? "正常" : anomalies.size() >= 3 ? "高" : "中";
        double latest = values.isEmpty() ? 0 : values.get(values.size() - 1);

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("summary", map(
                "metric", m,
                "points", series.size(),
                "mean", Math.round(mean),
                "stddev", Math.round(sd),
                "latest", (long) latest,
                "anomalies", anomalies.size(),
                "level", level));
        out.put("series", series);
        out.put("anomalies", anomalies);
        return out;
    }

    /* ------------------------------------------------------------------ */
    /* 页面性能与体验分析                                                   */
    /* ------------------------------------------------------------------ */

    public Map<String, Object> pageExperience(Long tid, Long appId, String from, String to) {
        LocalDateTime[] r = range(from, to, 14);
        List<Object[]> rows = eventRepository.extWideRows(tid, appId == null ? 0L : appId, r[0], r[1]);

        Map<String, long[]> pv = new LinkedHashMap<>();            // path -> [pv, exits]
        Map<String, Set<String>> uv = new HashMap<>();
        Map<String, String> titles = new HashMap<>();
        Map<String, long[]> dwell = new HashMap<>();               // path -> [sumMs, count]

        // session pageview paths for bounce detection
        Map<String, List<String>> sessionPaths = new LinkedHashMap<>();
        Map<String, String> sessionEntry = new HashMap<>();

        for (Object[] row : rows) {
            String type = nz(row[C_TYPE]);
            String path = label(nz(row[C_PATH]));
            String uid = nz(row[C_DISTINCT]);
            String sid = label(nz(row[C_SESSION]));
            String title = nz(row[C_TITLE]);
            if (!title.isBlank()) titles.putIfAbsent(path, title);

            if ("pageview".equals(type)) {
                pv.computeIfAbsent(path, k -> new long[2])[0]++;
                if (!uid.isBlank()) uv.computeIfAbsent(path, k -> new HashSet<>()).add(uid);
                sessionPaths.computeIfAbsent(sid, k -> new ArrayList<>()).add(path);
            } else if ("leave".equals(type)) {
                pv.computeIfAbsent(path, k -> new long[2])[1]++;
                long dur = toLong(row[C_DURATION]);
                if (dur > 0) {
                    long[] d = dwell.computeIfAbsent(path, k -> new long[2]);
                    d[0] += dur; d[1]++;
                }
            }
        }

        // bounce per entry path
        Map<String, long[]> bounce = new HashMap<>();              // path -> [sessions, bounces]
        for (Map.Entry<String, List<String>> e : sessionPaths.entrySet()) {
            List<String> paths = e.getValue();
            if (paths.isEmpty()) continue;
            String entry = paths.get(0);
            long[] b = bounce.computeIfAbsent(entry, k -> new long[2]);
            b[0]++;
            if (paths.size() == 1) b[1]++;
        }

        List<Map<String, Object>> pages = new ArrayList<>();
        long dwellSumAll = 0, dwellCntAll = 0, bounceSessAll = 0, bounceAll = 0;
        for (Map.Entry<String, long[]> e : pv.entrySet()) {
            String path = e.getKey();
            long p = e.getValue()[0], exits = e.getValue()[1];
            long[] d = dwell.getOrDefault(path, new long[]{0, 0});
            double avgDwell = d[1] == 0 ? 0 : round1(d[0] / 1000.0 / d[1]);
            long[] b = bounce.getOrDefault(path, new long[]{0, 0});
            double bounceRate = b[0] == 0 ? 0 : round1(b[1] * 100.0 / b[0]);
            dwellSumAll += d[0]; dwellCntAll += d[1]; bounceSessAll += b[0]; bounceAll += b[1];
            pages.add(map(
                    "path", path,
                    "title", titles.getOrDefault(path, path),
                    "pv", p,
                    "uv", uv.getOrDefault(path, Collections.emptySet()).size(),
                    "avgDwell", avgDwell,
                    "exits", exits,
                    "bounceRate", bounceRate));
        }
        pages.sort((a, b) -> Long.compare(((Number) b.get("pv")).longValue(), ((Number) a.get("pv")).longValue()));

        List<Map<String, Object>> top = pages.stream().limit(15).collect(Collectors.toList());
        double avgDwellAll = dwellCntAll == 0 ? 0 : round1(dwellSumAll / 1000.0 / dwellCntAll);
        double bounceRateAll = bounceSessAll == 0 ? 0 : round1(bounceAll * 100.0 / bounceSessAll);

        String topPage = pages.isEmpty() ? "—" : pages.get(0).get("path").toString();
        String engaging = pages.stream()
                .filter(x -> ((Number) x.get("pv")).longValue() >= 5)
                .max(Comparator.comparingDouble(x -> ((Number) x.get("avgDwell")).doubleValue()))
                .map(x -> String.valueOf(x.get("path"))).orElse("—");

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("summary", map(
                "pages", pages.size(),
                "avgDwell", avgDwellAll,
                "bounceRate", bounceRateAll,
                "topPage", topPage,
                "mostEngaging", engaging));
        out.put("pages", top);
        return out;
    }

    /* ------------------------------------------------------------------ */
    /* 事件探索 / 自助看板                                                  */
    /* ------------------------------------------------------------------ */

    public Map<String, Object> eventMeta(Long tid, Long appId, String from, String to) {
        LocalDateTime[] r = range(from, to, 30);
        List<Object[]> rows = eventRepository.extWideRows(tid, appId == null ? 0L : appId, r[0], r[1]);
        Map<String, Long> counts = new LinkedHashMap<>();
        for (Object[] row : rows) {
            String name = nz(row[C_NAME]);
            if (name.isBlank()) continue;
            counts.merge(name, 1L, Long::sum);
        }
        List<Map.Entry<String, Long>> es = new ArrayList<>(counts.entrySet());
        es.sort((a, b) -> Long.compare(b.getValue(), a.getValue()));
        List<Map<String, Object>> events = new ArrayList<>();
        for (Map.Entry<String, Long> e : es) {
            events.add(map("name", e.getKey(), "label", stepLabel(e.getKey()), "count", e.getValue()));
        }
        List<Map<String, Object>> dims = new ArrayList<>();
        for (Map.Entry<String, String> e : DIM_LABELS.entrySet()) dims.add(map("key", e.getKey(), "label", e.getValue()));
        return map("events", events, "dimensions", dims);
    }

    public Map<String, Object> eventExplore(Long tid, Long appId, String from, String to,
                                            String event, String dim, String metric) {
        LocalDateTime[] r = range(from, to, 30);
        List<Object[]> rows = eventRepository.extWideRows(tid, appId == null ? 0L : appId, r[0], r[1]);
        String dimension = dim == null || dim.isBlank() ? "device" : dim;
        String met = metric == null || metric.isBlank() ? "events" : metric;
        String ev = event == null ? "" : event.trim();

        Map<String, long[]> agg = new LinkedHashMap<>();           // value -> [events]
        Map<String, Set<String>> users = new HashMap<>();
        long total = 0;
        for (Object[] row : rows) {
            if (!ev.isBlank() && !ev.equals(nz(row[C_NAME]))) continue;
            String key = dimValue(row, dimension);
            total++;
            agg.computeIfAbsent(key, k -> new long[1])[0]++;
            String uid = nz(row[C_DISTINCT]);
            if (!uid.isBlank()) users.computeIfAbsent(key, k -> new HashSet<>()).add(uid);
        }

        List<Map<String, Object>> outRows = new ArrayList<>();
        for (Map.Entry<String, long[]> e : agg.entrySet()) {
            long events = e.getValue()[0];
            int u = users.getOrDefault(e.getKey(), Collections.emptySet()).size();
            long value = "users".equals(met) ? u : events;
            outRows.add(map("name", e.getKey(), "events", events, "users", u, "value", value,
                    "share", total == 0 ? 0 : round1(events * 100.0 / total)));
        }
        outRows.sort((a, b) -> Long.compare(((Number) b.get("value")).longValue(), ((Number) a.get("value")).longValue()));
        if (outRows.size() > 30) outRows = outRows.subList(0, 30);

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("summary", map(
                "dimension", dimension,
                "dimensionLabel", DIM_LABELS.getOrDefault(dimension, dimension),
                "metric", met,
                "event", ev.isBlank() ? "全部事件" : ev,
                "total", total,
                "groups", outRows.size()));
        out.put("rows", outRows);
        return out;
    }
}
