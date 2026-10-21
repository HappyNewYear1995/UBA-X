package com.ubax.analytics;

import com.ubax.event.domain.Event;
import com.ubax.event.repo.EventRepository;
import com.ubax.profile.repo.UserProfileRepository;
import com.ubax.stream.domain.RealtimeMinute;
import com.ubax.stream.repo.RealtimeMinuteRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
public class AnalyticsService {

    private final EventRepository eventRepository;
    private final UserProfileRepository profileRepository;
    private final RealtimeMinuteRepository realtimeRepository;

    public AnalyticsService(EventRepository eventRepository, UserProfileRepository profileRepository,
                            RealtimeMinuteRepository realtimeRepository) {
        this.eventRepository = eventRepository;
        this.profileRepository = profileRepository;
        this.realtimeRepository = realtimeRepository;
    }

    // ---------- helpers ----------

    public static LocalDateTime[] range(String from, String to, int defaultDays) {
        LocalDate toDate = (to == null || to.isBlank()) ? LocalDate.now() : LocalDate.parse(to);
        LocalDate fromDate = (from == null || from.isBlank()) ? toDate.minusDays(defaultDays - 1L) : LocalDate.parse(from);
        return new LocalDateTime[]{fromDate.atStartOfDay(), toDate.atTime(LocalTime.MAX)};
    }

    private Object num(Object o) {
        if (o == null) return 0;
        if (o instanceof BigDecimal) return ((BigDecimal) o).setScale(2, RoundingMode.HALF_UP).doubleValue();
        return o;
    }

    private String str(Object o) { return o == null ? null : o.toString(); }

    private LocalDateTime toLdt(Object o) {
        if (o == null) return null;
        if (o instanceof LocalDateTime) return (LocalDateTime) o;
        if (o instanceof Timestamp) return ((Timestamp) o).toLocalDateTime();
        if (o instanceof java.sql.Date) return ((java.sql.Date) o).toLocalDate().atStartOfDay();
        if (o instanceof java.util.Date) return LocalDateTime.ofInstant(((java.util.Date) o).toInstant(), java.time.ZoneId.systemDefault());
        return null;
    }

    // ---------- overview ----------

    public Map<String, Object> overview(Long tid, Long appId, String from, String to) {
        appId = nz(appId);
        LocalDateTime[] r = range(from, to, 7);
        Map<String, Object> m = new LinkedHashMap<>();
        long pv = eventRepository.countPageviews(tid, appId, r[0], r[1]);
        long totalEvents = eventRepository.countInRange(tid, appId, r[0], r[1]);
        long uv = eventRepository.uvInRange(tid, appId, r[0], r[1]);
        long visits = eventRepository.sessionsInRange(tid, appId, r[0], r[1]);
        long newUsers = eventRepository.newUsersInRange(tid, appId, r[0], r[1]);
        Double avgDur = eventRepository.avgDuration(tid, appId, r[0], r[1]);
        long multi = eventRepository.multiPageviewSessions(tid, appId, r[0], r[1]);
        double bounce = visits == 0 ? 0 : round((visits - multi) * 100.0 / visits);
        m.put("pv", pv);
        m.put("events", totalEvents);
        m.put("uv", uv);
        m.put("visits", visits);
        m.put("newUsers", newUsers);
        m.put("avgDuration", avgDur == null ? 0 : Math.round(avgDur / 1000));
        m.put("bounceRate", bounce);
        m.put("range", Map.of("from", r[0].toLocalDate().toString(), "to", r[1].toLocalDate().toString()));
        return m;
    }

    private double round(double v) { return BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP).doubleValue(); }

    private static Long nz(Long appId) { return appId == null ? 0L : appId; }
    private static String nz(String s) { return s == null ? "" : s; }

    // ---------- trend ----------

    public List<Map<String, Object>> trend(Long tid, Long appId, String from, String to) {
        appId = nz(appId);
        LocalDateTime[] r = range(from, to, 14);
        List<Object[]> rows = eventRepository.dailyTrend(tid, appId, r[0], r[1]);
        Map<String, long[]> byDate = new LinkedHashMap<>();
        // fill all dates so chart has continuous axis
        for (LocalDate d = r[0].toLocalDate(); !d.isAfter(r[1].toLocalDate()); d = d.plusDays(1)) {
            byDate.put(d.toString(), new long[]{0, 0});
        }
        for (Object[] row : rows) {
            String d = str(row[0]);
            if (d != null && d.length() >= 10) d = d.substring(0, 10);
            long[] v = byDate.get(d);
            if (v != null) {
                v[0] = ((Number) row[1]).longValue();
                v[1] = ((Number) row[2]).longValue();
            }
        }
        List<Map<String, Object>> out = new ArrayList<>();
        byDate.forEach((d, v) -> {
            Map<String, Object> e = new LinkedHashMap<>();
            e.put("date", d);
            e.put("pv", v[0]);
            e.put("uv", v[1]);
            out.add(e);
        });
        return out;
    }

    // ---------- realtime ----------

    public Map<String, Object> realtime(Long tid, Long appId, int minutes) {
        appId = nz(appId);
        LocalDateTime from = LocalDateTime.now().minusMinutes(minutes).withSecond(0).withNano(0);
        LocalDateTime to = LocalDateTime.now();
        List<Object[]> rows = realtimeRepository.series(tid, appId, from);
        List<Map<String, Object>> series = new ArrayList<>();
        long pv = 0, events = 0;
        for (Object[] row : rows) {
            Map<String, Object> e = new LinkedHashMap<>();
            LocalDateTime t = toLdt(row[0]);
            e.put("time", t == null ? str(row[0]) : t.toString());
            long p = ((Number) num(row[1])).longValue();
            long u = ((Number) num(row[2])).longValue();
            long ev = ((Number) num(row[3])).longValue();
            e.put("pv", p);
            e.put("uv", u);
            e.put("events", ev);
            pv += p; events += ev;
            series.add(e);
        }
        // PV and event counts are additive across minutes, but UV is not: a visitor
        // active in several minutes would be double-counted by summing the buckets.
        // Compute the true distinct-visitor count over the whole window instead.
        long uv = eventRepository.uvInRange(tid, appId, from, to);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("series", series);
        out.put("pv", pv);
        out.put("uv", uv);
        out.put("events", events);
        return out;
    }

    public List<Map<String, Object>> liveFeed(Long tid, int limit) {
        List<Event> events = eventRepository.findByTenantIdOrderByEventTimeDesc(tid, PageRequest.of(0, limit));
        List<Map<String, Object>> out = new ArrayList<>();
        for (Event e : events) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", e.getId());
            m.put("type", e.getEventType());
            m.put("event", e.getEventName());
            m.put("distinctId", e.getDistinctId());
            m.put("url", e.getUrl());
            m.put("path", e.getUrlPath());
            m.put("device", e.getDevice());
            m.put("browser", e.getBrowser());
            m.put("region", e.getRegion());
            m.put("city", e.getCity());
            m.put("time", e.getEventTime() == null ? null : e.getEventTime().toString());
            out.add(m);
        }
        return out;
    }

    // ---------- events / pages ----------

    public List<Map<String, Object>> topEvents(Long tid, Long appId, String from, String to, int limit) {
        appId = nz(appId);
        LocalDateTime[] r = range(from, to, 7);
        List<Object[]> rows = eventRepository.topEvents(tid, appId, r[0], r[1], limit);
        List<Map<String, Object>> out = new ArrayList<>();
        for (Object[] row : rows) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("event", str(row[0]));
            m.put("count", ((Number) row[1]).longValue());
            m.put("users", ((Number) row[2]).longValue());
            out.add(m);
        }
        return out;
    }

    public List<Map<String, Object>> topPages(Long tid, Long appId, String from, String to, int limit) {
        appId = nz(appId);
        LocalDateTime[] r = range(from, to, 7);
        List<Object[]> rows = eventRepository.topPages(tid, appId, r[0], r[1], limit);
        List<Map<String, Object>> out = new ArrayList<>();
        for (Object[] row : rows) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("path", str(row[0]));
            m.put("title", str(row[1]));
            m.put("pv", ((Number) row[2]).longValue());
            m.put("uv", ((Number) row[3]).longValue());
            m.put("avgDuration", row[4] == null ? 0 : Math.round(((Number) num(row[4])).doubleValue() / 1000));
            out.add(m);
        }
        return out;
    }

    public Map<String, Object> distributions(Long tid, Long appId, String from, String to) {
        appId = nz(appId);
        LocalDateTime[] r = range(from, to, 7);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("device", nameCount(eventRepository.deviceDistribution(tid, appId, r[0], r[1])));
        out.put("browser", nameCount(eventRepository.browserDistribution(tid, appId, r[0], r[1])));
        out.put("region", nameCount(eventRepository.geoDistribution(tid, appId, r[0], r[1])));
        out.put("referrer", nameCount(eventRepository.referrerDistribution(tid, appId, r[0], r[1])));
        return out;
    }

    private List<Map<String, Object>> nameCount(List<Object[]> rows) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (Object[] row : rows) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("name", str(row[0]) == null ? "未知" : str(row[0]));
            m.put("value", ((Number) row[1]).longValue());
            out.add(m);
        }
        return out;
    }

    // ---------- funnel ----------

    public Map<String, Object> funnel(Long tid, Long appId, List<String> steps, String from, String to) {
        appId = nz(appId);
        LocalDateTime[] r = range(from, to, 7);
        List<Map<String, Object>> out = new ArrayList<>();
        if (steps == null || steps.isEmpty()) {
            Map<String, Object> res = new LinkedHashMap<>();
            res.put("steps", out);
            return res;
        }
        // first-occurrence time per user per step
        List<Map<String, LocalDateTime>> perStep = new ArrayList<>();
        for (String s : steps) {
            Map<String, LocalDateTime> m = new HashMap<>();
            for (Object[] row : eventRepository.eventFirstTimes(tid, appId, s, r[0], r[1])) {
                m.put(str(row[0]), toLdt(row[1]));
            }
            perStep.add(m);
        }
        // ordered: user reaches step i if present in all steps 0..i with non-decreasing times
        Set<String> reached = new HashSet<>(perStep.get(0).keySet());
        long prev = reached.size();
        for (int i = 0; i < steps.size(); i++) {
            if (i == 0) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("step", steps.get(0));
                m.put("users", prev);
                m.put("rate", 100.0);
                m.put("stepRate", 100.0);
                out.add(m);
                continue;
            }
            Map<String, LocalDateTime> cur = perStep.get(i);
            Map<String, LocalDateTime> prevMap = perStep.get(i - 1);
            Set<String> next = new HashSet<>();
            for (String u : reached) {
                LocalDateTime pt = prevMap.get(u);
                LocalDateTime ct = cur.get(u);
                if (pt != null && ct != null && !ct.isBefore(pt)) next.add(u);
            }
            reached = next;
            long count = reached.size();
            double stepRate = prev == 0 ? 0 : round(count * 100.0 / prev);
            double totalRate = out.isEmpty() || ((Number) out.get(0).get("users")).longValue() == 0 ? 0
                    : round(count * 100.0 / ((Number) out.get(0).get("users")).longValue());
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("step", steps.get(i));
            m.put("users", count);
            m.put("rate", totalRate);
            m.put("stepRate", stepRate);
            out.add(m);
            prev = count;
        }
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("steps", out);
        return res;
    }

    // ---------- retention ----------

    public Map<String, Object> retention(Long tid, Long appId, String from, int days) {
        appId = nz(appId);
        int n = days <= 0 ? 7 : Math.min(days, 30);
        LocalDate anchor = (from == null || from.isBlank()) ? LocalDate.now().minusDays(n) : LocalDate.parse(from);
        List<String> cohort = eventRepository.distinctUsersInRange(tid, appId,
                anchor.atStartOfDay(), anchor.plusDays(1).atStartOfDay());
        int cohortSize = cohort.size();
        List<Map<String, Object>> out = new ArrayList<>();
        Map<String, Object> day0 = new LinkedHashMap<>();
        day0.put("day", 0);
        day0.put("date", anchor.toString());
        day0.put("retained", cohortSize);
        day0.put("rate", 100.0);
        out.add(day0);
        if (cohortSize > 0) {
            for (int d = 1; d <= n; d++) {
                LocalDate day = anchor.plusDays(d);
                long retained = eventRepository.retainedUsers(tid, appId, cohort,
                        day.atStartOfDay(), day.plusDays(1).atStartOfDay());
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("day", d);
                m.put("date", day.toString());
                m.put("retained", retained);
                m.put("rate", round(retained * 100.0 / cohortSize));
                out.add(m);
            }
        }
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("cohortSize", cohortSize);
        res.put("anchor", anchor.toString());
        res.put("retention", out);
        return res;
    }

    // ---------- heatmap ----------

    public Map<String, Object> heatmap(Long tid, Long appId, String path, String from, String to) {
        appId = nz(appId);
        path = nz(path);
        LocalDateTime[] r = range(from, to, 7);
        List<Object[]> rows = eventRepository.heatmapPoints(tid, appId, path, r[0], r[1]);
        List<Map<String, Object>> points = new ArrayList<>();
        long total = 0;
        // geometry accumulators (count-weighted) to derive the representative page size
        double sumDocW = 0, sumDocH = 0;
        long geoCount = 0;
        for (Object[] row : rows) {
            Map<String, Object> m = new LinkedHashMap<>();
            double x = row[0] == null ? 0 : ((Number) row[0]).doubleValue();
            double y = row[1] == null ? 0 : ((Number) row[1]).doubleValue();
            long c = ((Number) row[3]).longValue();
            double dw = row[4] == null ? 0 : ((Number) row[4]).doubleValue();
            double dh = row[5] == null ? 0 : ((Number) row[5]).doubleValue();
            m.put("x", x);
            m.put("y", y);
            m.put("element", str(row[2]));
            m.put("count", c);
            total += c;
            if (dh > 0) { sumDocW += dw * c; sumDocH += dh * c; geoCount += c; }
            points.add(m);
        }
        boolean hasGeometry = geoCount > 0;
        long docW = hasGeometry ? Math.round(sumDocW / geoCount) : 1440;
        long docH = hasGeometry ? Math.round(sumDocH / geoCount) : 900;
        // normalise each point against the representative page size for snapshot overlay
        for (Map<String, Object> m : points) {
            double x = ((Number) m.get("x")).doubleValue();
            double y = ((Number) m.get("y")).doubleValue();
            m.put("nx", docW > 0 ? round4(x / docW) : 0);
            m.put("ny", docH > 0 ? round4(y / docH) : 0);
        }
        String url = path.isEmpty() ? null : eventRepository.heatmapRepresentativeUrl(tid, appId, path, r[0], r[1]);

        Map<String, Object> page = new LinkedHashMap<>();
        page.put("docW", docW);
        page.put("docH", docH);
        Map<String, Object> viewport = new LinkedHashMap<>();
        viewport.put("w", 1440);
        viewport.put("h", 900);

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("path", path);
        out.put("url", url);
        out.put("hasGeometry", hasGeometry);
        out.put("page", page);
        out.put("viewport", viewport);
        out.put("points", points);
        out.put("total", total);
        return out;
    }

    private static double round4(double v) { return Math.round(v * 10000d) / 10000d; }

    /** Representative absolute URL for a path (most-reported), used to snapshot the real page. */
    public String representativeUrl(Long tid, Long appId, String path, String from, String to) {
        appId = nz(appId);
        path = nz(path);
        if (path.isEmpty()) return null;
        LocalDateTime[] r = range(from, to, 7);
        return eventRepository.heatmapRepresentativeUrl(tid, appId, path, r[0], r[1]);
    }

    // ---------- session replay ----------

    public List<Map<String, Object>> sessions(Long tid, Long appId, String from, String to, int limit) {
        appId = nz(appId);
        LocalDateTime[] r = range(from, to, 3);
        List<Object[]> rows = eventRepository.sessionSummaries(tid, appId, r[0], r[1], limit);
        List<Map<String, Object>> out = new ArrayList<>();
        for (Object[] row : rows) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("sessionId", str(row[0]));
            m.put("distinctId", str(row[1]));
            LocalDateTime st = toLdt(row[2]);
            LocalDateTime en = toLdt(row[3]);
            m.put("start", st == null ? null : st.toString());
            m.put("end", en == null ? null : en.toString());
            m.put("duration", (st != null && en != null) ? java.time.Duration.between(st, en).getSeconds() : 0);
            m.put("events", ((Number) row[4]).longValue());
            m.put("entry", str(row[5]));
            out.add(m);
        }
        return out;
    }

    public List<Map<String, Object>> sessionDetail(Long tid, String sessionId) {
        List<Event> events = eventRepository.findByTenantIdAndSessionIdOrderByEventTimeAsc(tid, sessionId);
        List<Map<String, Object>> out = new ArrayList<>();
        for (Event e : events) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("type", e.getEventType());
            m.put("event", e.getEventName());
            m.put("time", e.getEventTime() == null ? null : e.getEventTime().toString());
            m.put("url", e.getUrl());
            m.put("path", e.getUrlPath());
            m.put("title", e.getTitle());
            m.put("element", e.getElement());
            m.put("x", e.getX());
            m.put("y", e.getY());
            m.put("duration", e.getDuration());
            m.put("device", e.getDevice());
            m.put("browser", e.getBrowser());
            out.add(m);
        }
        return out;
    }
}
