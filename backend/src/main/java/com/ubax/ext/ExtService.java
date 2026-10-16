package com.ubax.ext;

import com.ubax.event.repo.EventRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * "数据应用" extension apps: access-security analysis, geo analysis and client
 * fingerprinting. All three aggregate raw events in the requested range.
 */
@Service
public class ExtService {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final List<String> BOT_HINTS =
            List.of("bot", "spider", "crawler", "scraper", "python", "curl", "wget", "headless");

    private final EventRepository eventRepository;

    public ExtService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    private LocalDateTime[] range(String from, String to, int defaultDays) {
        LocalDate end = to != null && !to.isBlank() ? LocalDate.parse(to) : LocalDate.now();
        LocalDate start = from != null && !from.isBlank() ? LocalDate.parse(from) : end.minusDays(defaultDays - 1L);
        return new LocalDateTime[]{start.atStartOfDay(), end.plusDays(1).atStartOfDay().minusNanos(1)};
    }

    private static String nz(Object o) {
        return o == null ? "" : String.valueOf(o);
    }

    /** Native queries surface temporal columns as java.sql types; normalize to LocalDateTime. */
    private static LocalDateTime toDateTime(Object o) {
        if (o instanceof LocalDateTime t) return t;
        if (o instanceof java.sql.Timestamp ts) return ts.toLocalDateTime();
        if (o instanceof java.util.Date d) return new java.sql.Timestamp(d.getTime()).toLocalDateTime();
        return LocalDateTime.now();
    }

    private static String label(String v) {
        return v == null || v.isBlank() ? "未知" : v;
    }

    private static Map<String, Object> map(Object... kv) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2) m.put((String) kv[i], kv[i + 1]);
        return m;
    }

    /* ------------------------------------------------------------------ */
    /* 访问安全分析                                                         */
    /* ------------------------------------------------------------------ */

    public Map<String, Object> security(Long tid, Long appId, String from, String to) {
        LocalDateTime[] r = range(from, to, 7);
        List<Object[]> rows = eventRepository.extSecurityRows(tid, appId == null ? 0L : appId, r[0], r[1]);

        Map<String, long[]> ips = new LinkedHashMap<>();      // ip -> [events, users(set handled separately), sessions]
        Map<String, Set<String>> ipUsers = new HashMap<>();
        Map<String, Set<String>> ipSessions = new HashMap<>();
        Map<String, long[]> agents = new LinkedHashMap<>();    // ua -> [events, users]
        Map<String, Set<String>> agentUsers = new HashMap<>();
        Map<String, long[]> sessions = new LinkedHashMap<>();  // session -> [events, minEpoch, maxEpoch]
        Map<String, String> sessionUser = new HashMap<>();
        Map<Integer, Long> hourly = new TreeMap<>();
        long total = 0;

        for (Object[] row : rows) {
            String ip = label(nz(row[0]));
            String ua = nz(row[1]);
            String sid = label(nz(row[2]));
            String uid = nz(row[3]);
            LocalDateTime t = toDateTime(row[4]);
            total++;

            ips.computeIfAbsent(ip, k -> new long[1])[0]++;
            ipUsers.computeIfAbsent(ip, k -> new HashSet<>()).add(uid);
            ipSessions.computeIfAbsent(ip, k -> new HashSet<>()).add(sid);

            String uaKey = ua.isBlank() ? "(空 UA)" : ua;
            agents.computeIfAbsent(uaKey, k -> new long[1])[0]++;
            agentUsers.computeIfAbsent(uaKey, k -> new HashSet<>()).add(uid);

            long epoch = t.toEpochSecond(java.time.ZoneOffset.UTC);
            long[] s = sessions.computeIfAbsent(sid, k -> new long[]{0, Long.MAX_VALUE, Long.MIN_VALUE});
            s[0]++;
            s[1] = Math.min(s[1], epoch);
            s[2] = Math.max(s[2], epoch);
            sessionUser.put(sid, uid);

            hourly.merge(t.getHour(), 1L, Long::sum);
        }

        double avgPerIp = ips.isEmpty() ? 0 : (double) total / ips.size();

        List<Map<String, Object>> topIps = new ArrayList<>();
        long riskyHigh = 0, riskyMid = 0, flaggedEvents = 0;
        List<Map.Entry<String, long[]>> ipEntries = new ArrayList<>(ips.entrySet());
        ipEntries.sort((a, b) -> Long.compare(b.getValue()[0], a.getValue()[0]));
        for (Map.Entry<String, long[]> e : ipEntries.stream().limit(15).collect(Collectors.toList())) {
            String ip = e.getKey();
            long events = e.getValue()[0];
            int users = ipUsers.get(ip).size();
            int sess = ipSessions.get(ip).size();
            double ratio = avgPerIp == 0 ? 0 : events / avgPerIp;
            List<String> reasons = new ArrayList<>();
            String risk = "NORMAL";
            if (ratio >= 4) { risk = "HIGH"; reasons.add("事件量为均值 " + String.format("%.1f", ratio) + " 倍"); }
            else if (ratio >= 2.5) { risk = "MEDIUM"; reasons.add("事件量为均值 " + String.format("%.1f", ratio) + " 倍"); }
            if (users >= 15) { reasons.add("同 IP 关联 " + users + " 个用户（疑似代理/爬虫池）"); if ("NORMAL".equals(risk)) risk = "MEDIUM"; }
            if ("HIGH".equals(risk)) { riskyHigh++; flaggedEvents += events; }
            else if ("MEDIUM".equals(risk)) { riskyMid++; flaggedEvents += events; }
            topIps.add(map("ip", ip, "events", events, "users", users, "sessions", sess,
                    "ratio", Math.round(ratio * 10) / 10.0, "risk", risk, "reasons", reasons));
        }

        List<Map<String, Object>> botAgents = new ArrayList<>();
        long botEvents = 0;
        for (Map.Entry<String, long[]> e : agents.entrySet()) {
            String ua = e.getKey();
            String lower = ua.toLowerCase(Locale.ROOT);
            boolean bot = BOT_HINTS.stream().anyMatch(lower::contains);
            if (bot) {
                botEvents += e.getValue()[0];
                botAgents.add(map("ua", ua, "events", e.getValue()[0], "users", agentUsers.get(ua).size()));
            }
        }
        botAgents.sort((a, b) -> Long.compare((Long) b.get("events"), (Long) a.get("events")));
        botAgents = botAgents.stream().limit(8).collect(Collectors.toList());

        List<Map<String, Object>> burstSessions = new ArrayList<>();
        for (Map.Entry<String, long[]> e : sessions.entrySet()) {
            long[] s = e.getValue();
            long span = s[2] == Long.MIN_VALUE ? 0 : Math.max(0, s[2] - s[1]);
            if (s[0] >= 40 && span <= 60) {
                burstSessions.add(map("sessionId", e.getKey(), "distinctId", label(sessionUser.get(e.getKey())),
                        "events", s[0], "spanSeconds", span));
            }
        }
        burstSessions.sort((a, b) -> Long.compare((Long) b.get("events"), (Long) a.get("events")));
        burstSessions = burstSessions.stream().limit(8).collect(Collectors.toList());

        String level;
        if (riskyHigh > 0 || !burstSessions.isEmpty()) level = "高";
        else if (riskyMid > 0 || botEvents > 0) level = "中";
        else level = "低";

        List<Map<String, Object>> hourlyList = new ArrayList<>();
        for (int h = 0; h < 24; h++) hourlyList.add(map("hour", h, "events", hourly.getOrDefault(h, 0L)));

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("summary", map(
                "events", total,
                "distinctIps", ips.size(),
                "riskyIps", riskyHigh + riskyMid,
                "highRiskIps", riskyHigh,
                "botEvents", botEvents,
                "burstSessions", burstSessions.size(),
                "flaggedEvents", flaggedEvents + botEvents,
                "riskLevel", level));
        out.put("topIps", topIps);
        out.put("botAgents", botAgents);
        out.put("burstSessions", burstSessions);
        out.put("hourly", hourlyList);
        return out;
    }

    /* ------------------------------------------------------------------ */
    /* 地域分析                                                             */
    /* ------------------------------------------------------------------ */

    public Map<String, Object> geo(Long tid, Long appId, String from, String to) {
        LocalDateTime[] r = range(from, to, 7);
        List<Object[]> rows = eventRepository.extGeoRows(tid, appId == null ? 0L : appId, r[0], r[1]);

        Map<String, long[]> regions = new LinkedHashMap<>();   // region -> [pv]
        Map<String, Set<String>> regionUsers = new HashMap<>();
        Map<String, long[]> cities = new LinkedHashMap<>();    // "region / city" -> [pv]
        Map<String, Set<String>> cityUsers = new HashMap<>();
        Map<String, Map<String, Long>> regionDevice = new LinkedHashMap<>();
        Map<String, Map<String, Long>> regionDaily = new LinkedHashMap<>();
        long total = 0;

        for (Object[] row : rows) {
            String region = label(nz(row[1]));
            String city = label(nz(row[2]));
            String device = label(nz(row[3]));
            String uid = nz(row[4]);
            String day = row[5] == null ? "" : (row[5] instanceof java.sql.Date sd
                    ? sd.toLocalDate().toString() : String.valueOf(row[5]));
            total++;

            regions.computeIfAbsent(region, k -> new long[1])[0]++;
            regionUsers.computeIfAbsent(region, k -> new HashSet<>()).add(uid);

            String ck = region + " / " + city;
            cities.computeIfAbsent(ck, k -> new long[1])[0]++;
            cityUsers.computeIfAbsent(ck, k -> new HashSet<>()).add(uid);

            regionDevice.computeIfAbsent(region, k -> new LinkedHashMap<>()).merge(device, 1L, Long::sum);
            regionDaily.computeIfAbsent(region, k -> new LinkedHashMap<>()).merge(day, 1L, Long::sum);
        }

        List<Map.Entry<String, long[]>> regionEntries = new ArrayList<>(regions.entrySet());
        regionEntries.sort((a, b) -> Long.compare(b.getValue()[0], a.getValue()[0]));

        List<Map<String, Object>> regionList = new ArrayList<>();
        for (Map.Entry<String, long[]> e : regionEntries) {
            long pv = e.getValue()[0];
            regionList.add(map("name", e.getKey(), "pv", pv, "users", regionUsers.get(e.getKey()).size(),
                    "share", total == 0 ? 0 : Math.round(pv * 1000.0 / total) / 10.0));
        }

        List<Map.Entry<String, long[]>> cityEntries = new ArrayList<>(cities.entrySet());
        cityEntries.sort((a, b) -> Long.compare(b.getValue()[0], a.getValue()[0]));
        List<Map<String, Object>> cityList = new ArrayList<>();
        for (Map.Entry<String, long[]> e : cityEntries.stream().limit(12).collect(Collectors.toList())) {
            cityList.add(map("name", e.getKey(), "pv", e.getValue()[0], "users", cityUsers.get(e.getKey()).size()));
        }

        // stacked device breakdown for top regions
        List<Map<String, Object>> deviceMatrix = new ArrayList<>();
        Set<String> devices = new LinkedHashSet<>();
        for (Map.Entry<String, long[]> e : regionEntries.stream().limit(6).collect(Collectors.toList())) {
            devices.addAll(regionDevice.getOrDefault(e.getKey(), Map.of()).keySet());
        }
        for (Map.Entry<String, long[]> e : regionEntries.stream().limit(6).collect(Collectors.toList())) {
            Map<String, Object> rowOut = new LinkedHashMap<>();
            rowOut.put("region", e.getKey());
            for (String d : devices) rowOut.put(d, regionDevice.getOrDefault(e.getKey(), Map.of()).getOrDefault(d, 0L));
            deviceMatrix.add(rowOut);
        }

        // daily pv for top 5 regions
        List<String> days = new ArrayList<>(regionDaily.values().stream()
                .flatMap(m -> m.keySet().stream()).collect(Collectors.toCollection(TreeSet::new)));
        List<Map<String, Object>> daily = new ArrayList<>();
        for (String d : days) {
            Map<String, Object> rowOut = new LinkedHashMap<>();
            rowOut.put("date", d);
            for (Map.Entry<String, long[]> e : regionEntries.stream().limit(5).collect(Collectors.toList())) {
                rowOut.put(e.getKey(), regionDaily.getOrDefault(e.getKey(), Map.of()).getOrDefault(d, 0L));
            }
            daily.add(rowOut);
        }

        Map<String, Object> summary = map(
                "events", total,
                "regions", regions.size(),
                "cities", cities.size(),
                "topRegion", regionEntries.isEmpty() ? "—" : regionEntries.get(0).getKey(),
                "topRegionShare", regionEntries.isEmpty() || total == 0 ? 0
                        : Math.round(regionEntries.get(0).getValue()[0] * 1000.0 / total) / 10.0);

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("summary", summary);
        out.put("regions", regionList);
        out.put("cities", cityList);
        out.put("devices", new ArrayList<>(devices));
        out.put("deviceMatrix", deviceMatrix);
        out.put("daily", daily);
        return out;
    }

    /* ------------------------------------------------------------------ */
    /* 客户端指纹                                                           */
    /* ------------------------------------------------------------------ */

    public Map<String, Object> fingerprint(Long tid, Long appId, String from, String to) {
        LocalDateTime[] r = range(from, to, 7);
        List<Object[]> rows = eventRepository.extFingerprintRows(tid, appId == null ? 0L : appId, r[0], r[1]);

        Map<String, long[]> fps = new LinkedHashMap<>();       // fpKey -> [events]
        Map<String, Set<String>> fpUsers = new HashMap<>();
        Map<String, String[]> fpMeta = new HashMap<>();        // fpKey -> [device, os, browser, screen]
        Map<String, long[]> osDist = new LinkedHashMap<>();
        Map<String, long[]> browserDist = new LinkedHashMap<>();
        Map<String, long[]> screenDist = new LinkedHashMap<>();
        Map<String, long[]> deviceDist = new LinkedHashMap<>();
        Set<String> users = new HashSet<>();
        long total = 0;

        for (Object[] row : rows) {
            String device = label(nz(row[0]));
            String os = label(nz(row[1]));
            String browser = label(nz(row[2]));
            String screen = label(nz(row[3]));
            String uid = nz(row[4]);
            total++;
            users.add(uid);

            String key = device + "|" + os + "|" + browser + "|" + screen;
            fps.computeIfAbsent(key, k -> new long[1])[0]++;
            fpUsers.computeIfAbsent(key, k -> new HashSet<>()).add(uid);
            fpMeta.put(key, new String[]{device, os, browser, screen});

            osDist.computeIfAbsent(os, k -> new long[1])[0]++;
            browserDist.computeIfAbsent(browser, k -> new long[1])[0]++;
            screenDist.computeIfAbsent(screen, k -> new long[1])[0]++;
            deviceDist.computeIfAbsent(device, k -> new long[1])[0]++;
        }

        List<Map.Entry<String, long[]>> fpEntries = new ArrayList<>(fps.entrySet());
        fpEntries.sort((a, b) -> Long.compare(b.getValue()[0], a.getValue()[0]));

        List<Map<String, Object>> top = new ArrayList<>();
        long top10Users = 0;
        int i = 0;
        for (Map.Entry<String, long[]> e : fpEntries) {
            int u = fpUsers.get(e.getKey()).size();
            if (i < 10) top10Users += u;
            i++;
            if (top.size() < 10) {
                String[] meta = fpMeta.get(e.getKey());
                top.add(map("fp", meta[0] + " · " + meta[1] + " · " + meta[2] + " · " + meta[3],
                        "device", meta[0], "os", meta[1], "browser", meta[2], "screen", meta[3],
                        "users", u, "events", e.getValue()[0],
                        "share", users.isEmpty() ? 0 : Math.round(u * 1000.0 / users.size()) / 10.0));
            }
        }
        long rare = fpEntries.stream().filter(e -> fpUsers.get(e.getKey()).size() <= 1).count();

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("summary", map(
                "events", total,
                "users", users.size(),
                "fingerprints", fps.size(),
                "rareFingerprints", rare,
                "top10Coverage", users.isEmpty() ? 0 : Math.round(top10Users * 1000.0 / users.size()) / 10.0));
        out.put("top", top);
        out.put("os", dist(osDist));
        out.put("browser", dist(browserDist));
        out.put("screen", dist(screenDist));
        out.put("device", dist(deviceDist));
        return out;
    }

    private List<Map<String, Object>> dist(Map<String, long[]> m) {
        List<Map.Entry<String, long[]>> es = new ArrayList<>(m.entrySet());
        es.sort((a, b) -> Long.compare(b.getValue()[0], a.getValue()[0]));
        List<Map<String, Object>> out = new ArrayList<>();
        for (Map.Entry<String, long[]> e : es.stream().limit(10).collect(Collectors.toList())) {
            out.add(map("name", e.getKey(), "value", e.getValue()[0]));
        }
        return out;
    }
}
