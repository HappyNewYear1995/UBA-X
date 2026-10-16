package com.ubax.profile;

import com.ubax.common.ApiResponse;
import com.ubax.common.BusinessException;
import com.ubax.common.TenantContext;
import com.ubax.profile.domain.UserProfile;
import com.ubax.profile.repo.UserProfileRepository;
import com.ubax.event.domain.Event;
import com.ubax.event.repo.EventRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/profiles")
public class ProfileController {

    private final UserProfileRepository profileRepository;
    private final EventRepository eventRepository;
    private final ProfileInsightService insightService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ProfileController(UserProfileRepository profileRepository, EventRepository eventRepository,
                             ProfileInsightService insightService) {
        this.profileRepository = profileRepository;
        this.eventRepository = eventRepository;
        this.insightService = insightService;
    }

    private Long tid() { return TenantContext.requireTenantId(); }

    @GetMapping
    public ApiResponse<Map<String, Object>> list(@RequestParam(required = false) String kw,
                                                 @RequestParam(defaultValue = "1") int page,
                                                 @RequestParam(defaultValue = "20") int size) {
        PageRequest pr = PageRequest.of(Math.max(0, page - 1), Math.max(1, Math.min(size, 100)),
                Sort.by(Sort.Direction.DESC, "lastSeen"));
        Page<UserProfile> p = (kw == null || kw.isBlank())
                ? profileRepository.findByTenantId(tid(), pr)
                : profileRepository.search(tid(), kw, pr);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("total", p.getTotalElements());
        out.put("page", page);
        out.put("size", size);
        out.put("records", p.getContent());
        return ApiResponse.ok(out);
    }

    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> stats() {
        Long t = tid();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("total", profileRepository.countByTenantId(t));
        Map<String, Long> tags = new LinkedHashMap<>();
        for (String tag : List.of("高频用户", "活跃用户", "移动端", "桌面端", "新用户")) {
            tags.put(tag, profileRepository.countByTag(t, tag));
        }
        out.put("tags", tags);
        return ApiResponse.ok(out);
    }

    @GetMapping("/{distinctId}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable String distinctId) {
        return ApiResponse.ok(buildBundle(tid(), distinctId));
    }

    /** On-demand AI-style insight for a single profile, grounded in its real aggregates. */
    @GetMapping("/{distinctId}/insight")
    public ApiResponse<Map<String, Object>> insight(@PathVariable String distinctId) {
        Map<String, Object> bundle = buildBundle(tid(), distinctId);
        return ApiResponse.ok(insightService.analyze(bundle));
    }

    /**
     * Assemble every per-user aggregate the detail page (and the insight engine) needs:
     * the profile row, its recent event timeline, and derived stats / distributions.
     */
    private Map<String, Object> buildBundle(Long t, String distinctId) {
        UserProfile p = profileRepository.findByTenantIdAndDistinctId(t, distinctId)
                .orElseThrow(() -> new BusinessException(404, "用户画像不存在"));
        // fetch the user's recent events (DESC), capped; reused for both the timeline
        // and all client-side aggregations below.
        List<Event> desc = eventRepository.findByTenantIdAndDistinctIdOrderByEventTimeDesc(
                t, distinctId, PageRequest.of(0, 2000));
        List<Event> recent = desc.stream().limit(30).collect(Collectors.toList());
        List<Event> asc = new ArrayList<>(desc);
        Collections.reverse(asc);

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("profile", p);
        out.put("recentEvents", recent);
        out.put("stats", buildStats(p, asc));
        out.put("eventTypeDist", eventTypeDist(asc));
        out.put("topPages", topPages(asc));
        out.put("topEvents", topEvents(asc));
        out.put("dailyTrend", dailyTrend(asc));
        out.put("hourDist", hourDist(asc));
        out.put("sessions", sessions(asc));
        out.put("derived", deriveAttributes(asc));
        return out;
    }

    /**
     * Mine the user's event-level {@code properties} JSON into human-meaningful
     * commerce / behaviour attributes (order count, total spend, items bought,
     * search keywords). The profile row itself has no properties in seed data, so
     * these derived signals give the detail page real, populated content.
     */
    private Map<String, Object> deriveAttributes(List<Event> asc) {
        long orderCount = 0, totalItems = 0;
        double amountSum = 0;
        String currency = null;
        Map<String, Long> keywords = new LinkedHashMap<>();
        for (Event e : asc) {
            String raw = e.getProperties();
            if (raw != null && !raw.isBlank()) {
                try {
                    JsonNode n = objectMapper.readTree(raw);
                    if (n.has("amount")) {
                        orderCount++;
                        amountSum += n.get("amount").asDouble(0);   // keep decimals, round on output
                        totalItems += n.path("items").asLong(0);
                        if (n.has("currency")) currency = n.get("currency").asText(currency);
                    }
                    if (n.has("keyword")) {
                        String kw = n.get("keyword").asText();
                        if (kw != null && !kw.isBlank()) keywords.merge(kw, 1L, Long::sum);
                    }
                } catch (Exception ignore) { /* malformed json — skip */ }
            }
        }
        Map<String, Object> d = new LinkedHashMap<>();
        d.put("orderCount", orderCount);
        d.put("totalAmount", Math.round(amountSum));
        d.put("totalItems", totalItems);
        d.put("currency", currency == null ? "CNY" : currency);
        List<Map<String, Object>> kwList = new ArrayList<>();
        keywords.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(10)
                .forEach(en -> {
                    Map<String, Object> r = new LinkedHashMap<>();
                    r.put("keyword", en.getKey());
                    r.put("count", en.getValue());
                    kwList.add(r);
                });
        d.put("keywords", kwList);
        return d;
    }

    // ---- per-user aggregations --------------------------------------------

    private Map<String, Object> buildStats(UserProfile p, List<Event> asc) {
        Map<String, Object> s = new LinkedHashMap<>();
        Set<String> sessionIds = new HashSet<>();
        Set<LocalDate> days = new LinkedHashSet<>();
        long dwellSum = 0, dwellN = 0;
        for (Event e : asc) {
            if (e.getSessionId() != null) sessionIds.add(e.getSessionId());
            if (e.getEventTime() != null) days.add(e.getEventTime().toLocalDate());
            if (e.getDuration() != null && e.getDuration() > 0) { dwellSum += e.getDuration(); dwellN++; }
        }
        // asc is capped at 2000 events, so its size is a *sample*, not the lifetime
        // total. Prefer the persistent profile counter for the cumulative figure and
        // expose the sample size + a truncation flag so callers can label it honestly.
        long sampled = asc.size();
        long trueTotal = Math.max(nz(p.getEventCount()), sampled);
        boolean truncated = trueTotal > sampled;
        s.put("eventTotal", trueTotal);
        s.put("sampledEvents", sampled);
        s.put("truncated", truncated);
        s.put("sessionCount", sessionIds.size());
        s.put("activeDays", days.size());
        s.put("pageviewCount", nz(p.getPageviewCount()));
        s.put("visitCount", nz(p.getVisitCount()));
        s.put("avgEventsPerDay", days.isEmpty() ? 0 : Math.round((double) sampled / days.size() * 10) / 10.0);
        s.put("avgDwellMs", dwellN == 0 ? 0 : dwellSum / dwellN);
        // lifetime in days between first and last activity
        LocalDateTime first = p.getFirstSeen();
        LocalDateTime last = p.getLastSeen();
        s.put("lifetimeDays", (first == null || last == null) ? 0
                : Math.max(1, Duration.between(first, last).toDays() + 1));
        s.put("daysSinceLastSeen", last == null ? null
                : Duration.between(last, LocalDateTime.now()).toDays());
        return s;
    }

    private List<Map<String, Object>> eventTypeDist(List<Event> asc) {
        Map<String, Long> m = asc.stream()
                .filter(e -> e.getEventType() != null)
                .collect(Collectors.groupingBy(Event::getEventType, LinkedHashMap::new, Collectors.counting()));
        List<Map<String, Object>> out = new ArrayList<>();
        m.forEach((k, v) -> { Map<String, Object> r = new LinkedHashMap<>(); r.put("type", k); r.put("count", v); out.add(r); });
        out.sort((a, b) -> Long.compare((Long) b.get("count"), (Long) a.get("count")));
        return out;
    }

    private List<Map<String, Object>> topPages(List<Event> asc) {
        Map<String, Long> cnt = new LinkedHashMap<>();
        Map<String, String> titles = new HashMap<>();
        for (Event e : asc) {
            if (!"pageview".equals(e.getEventType()) || e.getUrlPath() == null) continue;
            cnt.merge(e.getUrlPath(), 1L, Long::sum);
            if (e.getTitle() != null) titles.putIfAbsent(e.getUrlPath(), e.getTitle());
        }
        return topN(cnt, titles, 8);
    }

    private List<Map<String, Object>> topEvents(List<Event> asc) {
        Map<String, Long> cnt = new LinkedHashMap<>();
        for (Event e : asc) {
            String ty = e.getEventType();
            if (e.getEventName() == null) continue;
            if (!("event".equals(ty) || "custom".equals(ty) || "click".equals(ty))) continue;
            cnt.merge(e.getEventName(), 1L, Long::sum);
        }
        return topN(cnt, Collections.emptyMap(), 8);
    }

    private List<Map<String, Object>> topN(Map<String, Long> cnt, Map<String, String> titles, int n) {
        return cnt.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(n)
                .map(en -> {
                    Map<String, Object> r = new LinkedHashMap<>();
                    r.put("key", en.getKey());
                    r.put("title", titles.getOrDefault(en.getKey(), ""));
                    r.put("count", en.getValue());
                    return r;
                })
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> dailyTrend(List<Event> asc) {
        Map<LocalDate, Long> byDay = asc.stream()
                .filter(e -> e.getEventTime() != null)
                .collect(Collectors.groupingBy(e -> e.getEventTime().toLocalDate(), TreeMap::new, Collectors.counting()));
        List<Map<String, Object>> out = new ArrayList<>();
        byDay.forEach((d, c) -> {
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("date", d.toString());
            r.put("count", c);
            out.add(r);
        });
        // keep the most recent 30 days for a readable trend
        return out.size() > 30 ? out.subList(out.size() - 30, out.size()) : out;
    }

    private List<Map<String, Object>> hourDist(List<Event> asc) {
        long[] buckets = new long[24];
        for (Event e : asc) {
            if (e.getEventTime() != null) buckets[e.getEventTime().getHour()]++;
        }
        List<Map<String, Object>> out = new ArrayList<>();
        for (int h = 0; h < 24; h++) {
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("hour", h);
            r.put("count", buckets[h]);
            out.add(r);
        }
        return out;
    }

    private List<Map<String, Object>> sessions(List<Event> asc) {
        Map<String, List<Event>> bySession = new LinkedHashMap<>();
        for (Event e : asc) {
            if (e.getSessionId() == null) continue;
            bySession.computeIfAbsent(e.getSessionId(), k -> new ArrayList<>()).add(e);
        }
        List<Map<String, Object>> out = new ArrayList<>();
        for (Map.Entry<String, List<Event>> en : bySession.entrySet()) {
            List<Event> evs = en.getValue();
            LocalDateTime start = evs.get(0).getEventTime();
            LocalDateTime end = evs.get(evs.size() - 1).getEventTime();
            String entry = evs.stream().map(Event::getUrlPath).filter(Objects::nonNull).findFirst().orElse(null);
            long pages = evs.stream().filter(e -> "pageview".equals(e.getEventType())).count();
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("sessionId", en.getKey());
            r.put("start", start);
            r.put("end", end);
            r.put("events", evs.size());
            r.put("pages", pages);
            r.put("entry", entry);
            r.put("durationMs", (start != null && end != null) ? Duration.between(start, end).toMillis() : 0);
            out.add(r);
        }
        out.sort((a, b) -> {
            LocalDateTime ta = (LocalDateTime) a.get("start"), tb = (LocalDateTime) b.get("start");
            if (ta == null && tb == null) return 0;
            if (ta == null) return 1;
            if (tb == null) return -1;
            return tb.compareTo(ta);
        });
        return out.size() > 12 ? out.subList(0, 12) : out;
    }

    private static long nz(Long v) { return v == null ? 0L : v; }
}
