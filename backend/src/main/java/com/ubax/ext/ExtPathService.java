package com.ubax.ext;

import com.ubax.event.repo.EventRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.ubax.ext.ExtSupport.*;

/**
 * 转化路径分析 (conversion-path) and 漏斗对比 (funnel-compare) extension apps.
 * Both aggregate the wide event projection in memory (seeded/demo scale).
 */
@Service
public class ExtPathService {

    private static final List<String> FUNNEL = List.of("view_product", "add_to_cart", "begin_checkout", "purchase");

    private final EventRepository eventRepository;

    public ExtPathService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    /* ------------------------------------------------------------------ */
    /* 转化路径分析                                                         */
    /* ------------------------------------------------------------------ */

    public Map<String, Object> conversionPath(Long tid, Long appId, String from, String to, String goal) {
        LocalDateTime[] r = range(from, to, 14);
        List<Object[]> rows = eventRepository.extWideRows(tid, appId == null ? 0L : appId, r[0], r[1]);
        String target = goal == null || goal.isBlank() ? "purchase" : goal;

        // session -> ordered step nodes (wide rows already ordered by event_time)
        Map<String, List<String>> sessionSteps = new LinkedHashMap<>();
        Map<String, String> sessionUser = new HashMap<>();

        for (Object[] row : rows) {
            String type = nz(row[C_TYPE]);
            String node = stepNode(row);
            if (node == null) continue;                 // skip click / leave noise
            String sid = label(nz(row[C_SESSION]));
            List<String> steps = sessionSteps.computeIfAbsent(sid, k -> new ArrayList<>());
            if (steps.isEmpty() || !steps.get(steps.size() - 1).equals(node)) steps.add(node);
            sessionUser.putIfAbsent(sid, nz(row[C_DISTINCT]));
        }

        String goalLabel = stepLabel(target);

        // count full paths
        Map<String, long[]> pathCount = new LinkedHashMap<>();     // pathStr -> [sessions, reachedGoal]
        Map<String, Set<String>> pathUsers = new HashMap<>();
        Map<String, List<String>> pathNodes = new HashMap<>();
        // transition counts for sankey
        Map<String, Long> transitions = new LinkedHashMap<>();
        long totalSessions = 0, converted = 0, stepSum = 0;

        for (Map.Entry<String, List<String>> e : sessionSteps.entrySet()) {
            List<String> steps = e.getValue();
            if (steps.isEmpty()) continue;
            totalSessions++;
            stepSum += steps.size();
            // truncate very long sessions for readability
            List<String> view = steps.size() > 8 ? steps.subList(0, 8) : steps;
            String key = String.join(" → ", view);
            boolean reached = steps.contains(goalLabel);
            if (reached) converted++;
            long[] c = pathCount.computeIfAbsent(key, k -> new long[2]);
            c[0]++;
            if (reached) c[1]++;
            pathUsers.computeIfAbsent(key, k -> new HashSet<>()).add(sessionUser.get(e.getKey()));
            pathNodes.putIfAbsent(key, view);
            for (int i = 0; i + 1 < view.size(); i++) {
                transitions.merge(view.get(i) + "\u0001" + view.get(i + 1), 1L, Long::sum);
            }
        }

        List<Map.Entry<String, long[]>> pathEntries = new ArrayList<>(pathCount.entrySet());
        pathEntries.sort((a, b) -> Long.compare(b.getValue()[0], a.getValue()[0]));

        List<Map<String, Object>> topPaths = new ArrayList<>();
        for (Map.Entry<String, long[]> e : pathEntries) {
            if (topPaths.size() >= 12) break;
            String key = e.getKey();
            List<String> nodes = pathNodes.get(key);
            topPaths.add(map(
                    "path", key,
                    "nodes", nodes,
                    "sessions", e.getValue()[0],
                    "users", pathUsers.get(key).size(),
                    "converted", e.getValue()[1] > 0,
                    "share", totalSessions == 0 ? 0 : round1(e.getValue()[0] * 100.0 / totalSessions)));
        }

        // sankey: top transitions among top nodes
        List<Map.Entry<String, Long>> tr = new ArrayList<>(transitions.entrySet());
        tr.sort((a, b) -> Long.compare(b.getValue(), a.getValue()));
        Set<String> nodeNames = new LinkedHashSet<>();
        List<Map<String, Object>> links = new ArrayList<>();
        for (Map.Entry<String, Long> e : tr) {
            if (links.size() >= 40) break;
            String[] parts = e.getKey().split("\u0001");
            nodeNames.add(parts[0]);
            nodeNames.add(parts[1]);
            links.add(map("source", parts[0], "target", parts[1], "value", e.getValue()));
        }
        List<Map<String, Object>> nodes = new ArrayList<>();
        for (String n : nodeNames) nodes.add(map("name", n));

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("summary", map(
                "goal", goalLabel,
                "sessions", totalSessions,
                "convertedSessions", converted,
                "convRate", totalSessions == 0 ? 0 : round1(converted * 100.0 / totalSessions),
                "distinctPaths", pathCount.size(),
                "avgSteps", totalSessions == 0 ? 0 : round1((double) stepSum / totalSessions)));
        out.put("topPaths", topPaths);
        out.put("sankey", map("nodes", nodes, "links", links));
        return out;
    }

    /** Node label for a wide row, or null if the row is not a meaningful path step. */
    private String stepNode(Object[] row) {
        String type = nz(row[C_TYPE]);
        String name = nz(row[C_NAME]);
        if ("click".equals(type) || "leave".equals(type)) return null;
        if ("pageview".equals(type)) {
            String title = nz(row[C_TITLE]);
            String path = nz(row[C_PATH]);
            if (!title.isBlank()) return title;
            return path.isBlank() ? null : path;
        }
        // business / custom events
        if (name.isBlank() || name.startsWith("$")) return null;
        return stepLabel(name);
    }

    /* ------------------------------------------------------------------ */
    /* 漏斗对比                                                             */
    /* ------------------------------------------------------------------ */

    public Map<String, Object> funnelCompare(Long tid, Long appId, String from, String to, String dim) {
        LocalDateTime[] r = range(from, to, 14);
        List<Object[]> rows = eventRepository.extWideRows(tid, appId == null ? 0L : appId, r[0], r[1]);
        String dimension = dim == null || dim.isBlank() ? "device" : dim;

        // user -> (step -> earliest time), and user -> segment value (from first row)
        Map<String, Map<String, LocalDateTime>> userSteps = new LinkedHashMap<>();
        Map<String, String> userSeg = new HashMap<>();

        for (Object[] row : rows) {
            String name = nz(row[C_NAME]);
            String uid = nz(row[C_DISTINCT]);
            if (uid.isBlank()) continue;
            userSeg.putIfAbsent(uid, dimValue(row, dimension));
            if (!FUNNEL.contains(name)) continue;
            LocalDateTime t = toDateTime(row[C_TIME]);
            Map<String, LocalDateTime> steps = userSteps.computeIfAbsent(uid, k -> new HashMap<>());
            LocalDateTime prev = steps.get(name);
            if (prev == null || t.isBefore(prev)) steps.put(name, t);
        }

        // segment -> cumulative counts per step
        Map<String, long[]> segCounts = new LinkedHashMap<>();     // seg -> counts[FUNNEL.size]
        Map<String, Long> segUsers = new LinkedHashMap<>();
        for (Map.Entry<String, Map<String, LocalDateTime>> e : userSteps.entrySet()) {
            String uid = e.getKey();
            String seg = userSeg.getOrDefault(uid, "未知");
            Map<String, LocalDateTime> steps = e.getValue();
            segUsers.merge(seg, 1L, Long::sum);
            long[] counts = segCounts.computeIfAbsent(seg, k -> new long[FUNNEL.size()]);
            LocalDateTime cursor = null;
            for (int i = 0; i < FUNNEL.size(); i++) {
                LocalDateTime t = steps.get(FUNNEL.get(i));
                if (t == null) break;
                if (cursor != null && t.isBefore(cursor)) break;   // must be ordered
                counts[i]++;
                cursor = t;
            }
        }

        // pick top-2 segments by participating users
        List<Map.Entry<String, Long>> segEntries = new ArrayList<>(segUsers.entrySet());
        segEntries.sort((a, b) -> Long.compare(b.getValue(), a.getValue()));
        List<String> chosen = segEntries.stream().limit(2).map(Map.Entry::getKey).collect(Collectors.toList());

        List<Map<String, Object>> stepsOut = new ArrayList<>();
        for (String s : FUNNEL) stepsOut.add(map("key", s, "label", stepLabel(s)));

        List<Map<String, Object>> series = new ArrayList<>();
        for (String seg : chosen) {
            long[] counts = segCounts.getOrDefault(seg, new long[FUNNEL.size()]);
            List<Long> cs = new ArrayList<>();
            List<Double> rates = new ArrayList<>();
            long base = counts.length > 0 ? counts[0] : 0;
            for (long c : counts) {
                cs.add(c);
                rates.add(base == 0 ? 0 : round1(c * 100.0 / base));
            }
            series.add(map("segment", seg, "users", segUsers.getOrDefault(seg, 0L),
                    "counts", cs, "rates", rates));
        }

        long overallTop = 0, overallBottom = 0;
        for (long[] counts : segCounts.values()) {
            if (counts.length > 0) overallTop += counts[0];
            if (counts.length == FUNNEL.size()) overallBottom += counts[FUNNEL.size() - 1];
        }

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("summary", map(
                "dimension", dimension,
                "segments", chosen,
                "users", userSteps.size(),
                "entry", overallTop,
                "purchase", overallBottom,
                "overallRate", overallTop == 0 ? 0 : round1(overallBottom * 100.0 / overallTop)));
        out.put("steps", stepsOut);
        out.put("series", series);
        return out;
    }
}
