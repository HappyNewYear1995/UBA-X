package com.ubax.ext;

import com.ubax.event.domain.Event;
import com.ubax.event.repo.EventRepository;
import com.ubax.profile.domain.UserProfile;
import com.ubax.profile.repo.UserProfileRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.ubax.ext.ExtSupport.*;

/**
 * User-centric extension apps: 留存分群 (retention-segments), 流失预警/健康分
 * (churn-health), RFM 用户价值分层 (rfm), and 用户旅程时间线 (journey).
 */
@Service
public class ExtUserService {

    private final EventRepository eventRepository;
    private final UserProfileRepository profileRepository;

    public ExtUserService(EventRepository eventRepository, UserProfileRepository profileRepository) {
        this.eventRepository = eventRepository;
        this.profileRepository = profileRepository;
    }

    private List<UserProfile> profiles(Long tid, Long appId) {
        List<UserProfile> all = profileRepository.findAllByTenantIdOrderByIdAsc(tid);
        if (appId == null) return all;
        return all.stream().filter(p -> appId.equals(p.getAppId())).collect(Collectors.toList());
    }

    /* ------------------------------------------------------------------ */
    /* 留存分群                                                             */
    /* ------------------------------------------------------------------ */

    public Map<String, Object> retentionSegments(Long tid, Long appId, String from, String to, String dim) {
        LocalDateTime[] r = range(from, to, 14);
        List<Object[]> rows = eventRepository.extWideRows(tid, appId == null ? 0L : appId, r[0], r[1]);
        String dimension = dim == null || dim.isBlank() ? "device" : dim;
        LocalDateTime mid = r[0].plus(Duration.between(r[0], r[1]).dividedBy(2));

        Map<String, String> userSeg = new HashMap<>();
        Set<String> firstHalf = new HashSet<>();
        Set<String> secondHalf = new HashSet<>();
        for (Object[] row : rows) {
            String uid = nz(row[C_DISTINCT]);
            if (uid.isBlank()) continue;
            LocalDateTime t = toDateTime(row[C_TIME]);
            if (t.isBefore(mid)) {
                firstHalf.add(uid);
                userSeg.putIfAbsent(uid, dimValue(row, dimension));
            } else {
                secondHalf.add(uid);
            }
        }

        Map<String, long[]> seg = new LinkedHashMap<>();   // seg -> [cohort, retained]
        for (String uid : firstHalf) {
            String s = userSeg.getOrDefault(uid, "未知");
            long[] c = seg.computeIfAbsent(s, k -> new long[2]);
            c[0]++;
            if (secondHalf.contains(uid)) c[1]++;
        }

        List<Map<String, Object>> segments = new ArrayList<>();
        List<Map.Entry<String, long[]>> es = new ArrayList<>(seg.entrySet());
        es.sort((a, b) -> Long.compare(b.getValue()[0], a.getValue()[0]));
        for (Map.Entry<String, long[]> e : es) {
            long cohort = e.getValue()[0], retained = e.getValue()[1];
            segments.add(map("name", e.getKey(), "cohort", cohort, "retained", retained,
                    "rate", cohort == 0 ? 0 : round1(retained * 100.0 / cohort)));
        }

        long overallCohort = firstHalf.size();
        long overallRetained = firstHalf.stream().filter(secondHalf::contains).count();

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("summary", map(
                "dimension", dimension,
                "cohort", overallCohort,
                "retained", overallRetained,
                "rate", overallCohort == 0 ? 0 : round1(overallRetained * 100.0 / overallCohort),
                "segments", segments.size(),
                "midPoint", mid.toLocalDate().toString()));
        out.put("segments", segments);
        return out;
    }

    /* ------------------------------------------------------------------ */
    /* 流失预警 / 用户健康分                                                */
    /* ------------------------------------------------------------------ */

    public Map<String, Object> churnHealth(Long tid, Long appId, String from, String to) {
        List<UserProfile> list = profiles(tid, appId);
        LocalDateTime now = LocalDateTime.now();

        List<Map<String, Object>> scored = new ArrayList<>();
        long scoreSum = 0, high = 0, mid = 0, low = 0, active = 0;
        for (UserProfile p : list) {
            LocalDateTime last = p.getLastSeen() != null ? p.getLastSeen() : p.getCreatedAt();
            long recency = last == null ? 999 : Math.max(0, Duration.between(last, now).toDays());
            long visits = p.getVisitCount() == null ? 0 : p.getVisitCount();
            long events = p.getEventCount() == null ? 0 : p.getEventCount();

            double recencyScore = recency <= 1 ? 100 : recency <= 3 ? 85 : recency <= 7 ? 65 : recency <= 14 ? 40 : 15;
            double freqScore = Math.min(100, visits * 12.0);
            int score = (int) Math.round(0.6 * recencyScore + 0.4 * freqScore);
            String risk = score >= 70 ? "低" : score >= 45 ? "中" : "高";
            if ("高".equals(risk)) high++; else if ("中".equals(risk)) mid++; else low++;
            if (recency <= 7) active++;
            scoreSum += score;

            scored.add(map(
                    "distinctId", p.getDistinctId(),
                    "region", label(p.getRegion()),
                    "city", label(p.getCity()),
                    "device", label(p.getDevice()),
                    "lastSeen", last == null ? "—" : last.toString(),
                    "recencyDays", recency,
                    "visitCount", visits,
                    "eventCount", events,
                    "score", score,
                    "risk", risk));
        }

        scored.sort(Comparator.comparingInt((Map<String, Object> m) -> ((Number) m.get("score")).intValue())
                .thenComparing(m -> String.valueOf(m.get("distinctId"))));
        List<Map<String, Object>> riskList = scored.stream().limit(15).collect(Collectors.toList());

        int users = list.size();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("summary", map(
                "users", users,
                "avgScore", users == 0 ? 0 : Math.round(scoreSum * 1.0 / users),
                "highRisk", high,
                "midRisk", mid,
                "lowRisk", low,
                "activeRate", users == 0 ? 0 : round1(active * 100.0 / users)));
        out.put("buckets", List.of(
                map("name", "高流失风险", "value", high),
                map("name", "中流失风险", "value", mid),
                map("name", "健康", "value", low)));
        out.put("riskList", riskList);
        return out;
    }

    /* ------------------------------------------------------------------ */
    /* RFM 用户价值分层                                                     */
    /* ------------------------------------------------------------------ */

    public Map<String, Object> rfm(Long tid, Long appId, String from, String to) {
        LocalDateTime[] r = range(from, to, 30);
        List<UserProfile> list = profiles(tid, appId);
        LocalDateTime now = LocalDateTime.now();

        // monetary: sum of purchase amounts per user within range
        List<Object[]> rows = eventRepository.extWideRows(tid, appId == null ? 0L : appId, r[0], r[1]);
        Map<String, Double> money = new HashMap<>();
        for (Object[] row : rows) {
            if ("purchase".equals(nz(row[C_NAME]))) {
                String uid = nz(row[C_DISTINCT]);
                money.merge(uid, parseAmount(nz(row[C_PROPS])), Double::sum);
            }
        }

        List<double[]> raw = new ArrayList<>();       // [recencyDays, frequency, monetary]
        List<UserProfile> kept = new ArrayList<>();
        for (UserProfile p : list) {
            LocalDateTime last = p.getLastSeen() != null ? p.getLastSeen() : p.getCreatedAt();
            long recency = last == null ? 999 : Math.max(0, Duration.between(last, now).toDays());
            double freq = p.getVisitCount() == null ? 0 : p.getVisitCount();
            double m = money.getOrDefault(p.getDistinctId(), 0d);
            raw.add(new double[]{recency, freq, m});
            kept.add(p);
        }

        double[] rVals = col(raw, 0), fVals = col(raw, 1), mVals = col(raw, 2);
        int n = raw.size();
        List<Map<String, Object>> table = new ArrayList<>();
        Map<String, Long> segCount = new LinkedHashMap<>();
        double sumR = 0, sumF = 0, sumM = 0;

        for (int i = 0; i < n; i++) {
            UserProfile p = kept.get(i);
            double rec = raw.get(i)[0], freq = raw.get(i)[1], m = raw.get(i)[2];
            int rs = quintile(rec, rVals, false);   // lower recency = better
            int fs = quintile(freq, fVals, true);
            int ms = quintile(m, mVals, true);
            String segment = rfmSegment(rs, fs, ms);
            segCount.merge(segment, 1L, Long::sum);
            sumR += rs; sumF += fs; sumM += ms;
            table.add(map(
                    "distinctId", p.getDistinctId(),
                    "region", label(p.getRegion()),
                    "r", (long) rec, "f", (long) freq, "m", Math.round(m),
                    "rs", rs, "fs", fs, "ms", ms,
                    "score", rs + fs + ms,
                    "segment", segment));
        }

        table.sort((a, b) -> Integer.compare(((Number) b.get("score")).intValue(), ((Number) a.get("score")).intValue()));
        List<Map<String, Object>> topTable = table.stream().limit(20).collect(Collectors.toList());

        List<Map<String, Object>> segments = new ArrayList<>();
        List<Map.Entry<String, Long>> se = new ArrayList<>(segCount.entrySet());
        se.sort((a, b) -> Long.compare(b.getValue(), a.getValue()));
        for (Map.Entry<String, Long> e : se) {
            segments.add(map("name", e.getKey(), "value", e.getValue(),
                    "share", n == 0 ? 0 : round1(e.getValue() * 100.0 / n)));
        }

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("summary", map(
                "users", n,
                "avgR", n == 0 ? 0 : round1(sumR / n),
                "avgF", n == 0 ? 0 : round1(sumF / n),
                "avgM", n == 0 ? 0 : round1(sumM / n),
                "payingUsers", money.size(),
                "topSegment", se.isEmpty() ? "—" : se.get(0).getKey()));
        out.put("segments", segments);
        out.put("table", topTable);
        return out;
    }

    private static double[] col(List<double[]> raw, int idx) {
        double[] a = new double[raw.size()];
        for (int i = 0; i < raw.size(); i++) a[i] = raw.get(i)[idx];
        return a;
    }

    /** Quintile score 1..5. higherIsBetter=true -> large values score 5. */
    private static int quintile(double v, double[] all, boolean higherIsBetter) {
        if (all.length == 0) return 1;
        int less = 0, lessEq = 0;
        for (double d : all) {
            if (d < v) less++;
            if (d <= v) lessEq++;
        }
        double percentile = higherIsBetter
                ? (double) lessEq / all.length
                : (double) (all.length - less) / all.length;
        int score = (int) Math.ceil(percentile * 5);
        return Math.max(1, Math.min(5, score));
    }

    private static String rfmSegment(int rs, int fs, int ms) {
        boolean rHigh = rs >= 3, fHigh = fs >= 3, mHigh = ms >= 3;
        if (mHigh) {
            if (rHigh && fHigh) return "重要价值客户";
            if (rHigh) return "重要发展客户";
            if (fHigh) return "重要保持客户";
            return "重要挽留客户";
        } else {
            if (rHigh && fHigh) return "一般价值客户";
            if (rHigh) return "一般发展客户";
            if (fHigh) return "一般保持客户";
            return "一般挽留客户";
        }
    }

    /* ------------------------------------------------------------------ */
    /* 用户旅程时间线                                                       */
    /* ------------------------------------------------------------------ */

    public Map<String, Object> journeyUsers(Long tid, Long appId) {
        List<UserProfile> list = profiles(tid, appId);
        list.sort((a, b) -> Long.compare(
                b.getEventCount() == null ? 0 : b.getEventCount(),
                a.getEventCount() == null ? 0 : a.getEventCount()));
        List<Map<String, Object>> users = new ArrayList<>();
        for (UserProfile p : list.stream().limit(20).collect(Collectors.toList())) {
            users.add(map(
                    "distinctId", p.getDistinctId(),
                    "region", label(p.getRegion()),
                    "city", label(p.getCity()),
                    "device", label(p.getDevice()),
                    "visitCount", p.getVisitCount() == null ? 0 : p.getVisitCount(),
                    "eventCount", p.getEventCount() == null ? 0 : p.getEventCount(),
                    "lastSeen", p.getLastSeen() == null ? "—" : p.getLastSeen().toString()));
        }
        return map("users", users);
    }

    public Map<String, Object> journey(Long tid, String distinctId) {
        if (distinctId == null || distinctId.isBlank()) return map("sessions", List.of(), "totalEvents", 0);
        List<Event> events = eventRepository.findByTenantIdAndDistinctIdOrderByEventTimeDesc(
                tid, distinctId, PageRequest.of(0, 500));
        List<Event> asc = new ArrayList<>(events);
        Collections.reverse(asc);

        UserProfile p = profileRepository.findByTenantIdAndDistinctId(tid, distinctId).orElse(null);

        Map<String, List<Map<String, Object>>> bySession = new LinkedHashMap<>();
        Map<String, LocalDateTime[]> span = new LinkedHashMap<>();
        for (Event e : asc) {
            String sid = e.getSessionId() == null ? "(无会话)" : e.getSessionId();
            String name = e.getEventName();
            String display = name != null && !name.startsWith("$") ? stepLabel(name)
                    : e.getTitle() != null && !e.getTitle().isBlank() ? e.getTitle()
                    : e.getUrlPath() != null ? e.getUrlPath() : nz(e.getEventType());
            Map<String, Object> step = map(
                    "time", e.getEventTime() == null ? "—" : e.getEventTime().toString(),
                    "type", nz(e.getEventType()),
                    "name", display,
                    "url", nz(e.getUrlPath()),
                    "duration", e.getDuration() == null ? 0 : e.getDuration());
            bySession.computeIfAbsent(sid, k -> new ArrayList<>()).add(step);
            LocalDateTime[] sp = span.computeIfAbsent(sid, k -> new LocalDateTime[]{e.getEventTime(), e.getEventTime()});
            if (e.getEventTime() != null) {
                if (sp[0] == null || e.getEventTime().isBefore(sp[0])) sp[0] = e.getEventTime();
                if (sp[1] == null || e.getEventTime().isAfter(sp[1])) sp[1] = e.getEventTime();
            }
        }

        List<Map<String, Object>> sessions = new ArrayList<>();
        for (Map.Entry<String, List<Map<String, Object>>> e : bySession.entrySet()) {
            LocalDateTime[] sp = span.get(e.getKey());
            sessions.add(map(
                    "sessionId", e.getKey(),
                    "start", sp[0] == null ? "—" : sp[0].toString(),
                    "end", sp[1] == null ? "—" : sp[1].toString(),
                    "events", e.getValue().size(),
                    "steps", e.getValue()));
        }
        // most recent session first
        sessions.sort((a, b) -> String.valueOf(b.get("start")).compareTo(String.valueOf(a.get("start"))));

        Map<String, Object> profile = p == null ? map("distinctId", distinctId) : map(
                "distinctId", p.getDistinctId(),
                "region", label(p.getRegion()),
                "city", label(p.getCity()),
                "device", label(p.getDevice()),
                "os", label(p.getOs()),
                "browser", label(p.getBrowser()),
                "firstSeen", p.getFirstSeen() == null ? "—" : p.getFirstSeen().toString(),
                "lastSeen", p.getLastSeen() == null ? "—" : p.getLastSeen().toString(),
                "visitCount", p.getVisitCount() == null ? 0 : p.getVisitCount(),
                "eventCount", p.getEventCount() == null ? 0 : p.getEventCount(),
                "pageviewCount", p.getPageviewCount() == null ? 0 : p.getPageviewCount(),
                "tags", nz(p.getTags()));

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("profile", profile);
        out.put("sessions", sessions);
        out.put("totalEvents", asc.size());
        return out;
    }
}
