package com.ubax.ext;

import com.ubax.event.repo.EventRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.ubax.ext.ExtSupport.*;

/**
 * Growth extension apps: 归因分析 (multi-touch channel attribution) and
 * A/B 实验 (variant bucketing + significance testing). Both aggregate the wide
 * event projection in memory.
 */
@Service
public class ExtGrowthService {

    private static final String INTERNAL = "站内";
    private static final Set<String> PROPORTION_METRICS =
            new LinkedHashSet<>(List.of("purchase", "add_to_cart", "begin_checkout", "sign_up"));

    private final EventRepository eventRepository;

    public ExtGrowthService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    /* ------------------------------------------------------------------ */
    /* 归因分析                                                             */
    /* ------------------------------------------------------------------ */

    public Map<String, Object> attribution(Long tid, Long appId, String from, String to) {
        LocalDateTime[] r = range(from, to, 30);
        List<Object[]> rows = eventRepository.extWideRows(tid, appId == null ? 0L : appId, r[0], r[1]);

        // per-user ordered external touchpoints + conversions/value
        Map<String, List<String>> touch = new LinkedHashMap<>();
        Map<String, Long> convCount = new LinkedHashMap<>();
        Map<String, Double> convValue = new HashMap<>();

        for (Object[] row : rows) {
            String uid = nz(row[C_DISTINCT]);
            if (uid.isBlank()) continue;
            String type = nz(row[C_TYPE]);
            if ("pageview".equals(type)) {
                String src = referrerSource(nz(row[C_REFERRER]));
                if (!INTERNAL.equals(src)) {
                    List<String> t = touch.computeIfAbsent(uid, k -> new ArrayList<>());
                    if (t.isEmpty() || !t.get(t.size() - 1).equals(src)) t.add(src);
                }
            } else if ("purchase".equals(nz(row[C_NAME]))) {
                convCount.merge(uid, 1L, Long::sum);
                convValue.merge(uid, parseAmount(nz(row[C_PROPS])), Double::sum);
            }
        }

        Map<String, double[]> first = new LinkedHashMap<>();
        Map<String, double[]> last = new LinkedHashMap<>();
        Map<String, double[]> linear = new LinkedHashMap<>();
        Map<String, double[]> positional = new LinkedHashMap<>();

        long convertingUsers = 0, totalConv = 0, touchSum = 0;
        double totalValue = 0;

        for (Map.Entry<String, Long> e : convCount.entrySet()) {
            String uid = e.getKey();
            long c = e.getValue();
            double v = convValue.getOrDefault(uid, 0d);
            List<String> t = touch.getOrDefault(uid, Collections.emptyList());
            if (t.isEmpty()) t = List.of("直接访问");

            convertingUsers++;
            totalConv += c;
            totalValue += v;
            touchSum += t.size();

            credit(first, t.get(0), c, v);
            credit(last, t.get(t.size() - 1), c, v);
            for (String ch : t) credit(linear, ch, (double) c / t.size(), v / t.size());
            positionalCredit(positional, t, c, v);
        }

        Map<String, Object> models = new LinkedHashMap<>();
        models.put("firstTouch", modelList(first, totalConv, totalValue));
        models.put("lastTouch", modelList(last, totalConv, totalValue));
        models.put("linear", modelList(linear, totalConv, totalValue));
        models.put("positional", modelList(positional, totalConv, totalValue));

        Set<String> channels = new LinkedHashSet<>();
        channels.addAll(first.keySet());
        channels.addAll(last.keySet());
        channels.addAll(linear.keySet());
        channels.addAll(positional.keySet());

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("summary", map(
                "convertingUsers", convertingUsers,
                "totalConversions", totalConv,
                "totalValue", Math.round(totalValue),
                "channels", channels.size(),
                "avgTouchpoints", convertingUsers == 0 ? 0 : round1((double) touchSum / convertingUsers)));
        out.put("models", models);
        return out;
    }

    private static void credit(Map<String, double[]> m, String channel, double conv, double value) {
        double[] a = m.computeIfAbsent(channel, k -> new double[2]);
        a[0] += conv;
        a[1] += value;
    }

    private static void positionalCredit(Map<String, double[]> m, List<String> t, long c, double v) {
        int n = t.size();
        if (n == 1) { credit(m, t.get(0), c, v); return; }
        if (n == 2) {
            credit(m, t.get(0), c / 2.0, v / 2.0);
            credit(m, t.get(1), c / 2.0, v / 2.0);
            return;
        }
        credit(m, t.get(0), c * 0.4, v * 0.4);
        credit(m, t.get(n - 1), c * 0.4, v * 0.4);
        double midConv = c * 0.2 / (n - 2);
        double midVal = v * 0.2 / (n - 2);
        for (int i = 1; i < n - 1; i++) credit(m, t.get(i), midConv, midVal);
    }

    private static List<Map<String, Object>> modelList(Map<String, double[]> m, long totalConv, double totalValue) {
        List<Map.Entry<String, double[]>> es = new ArrayList<>(m.entrySet());
        es.sort((a, b) -> Double.compare(b.getValue()[0], a.getValue()[0]));
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map.Entry<String, double[]> e : es) {
            double conv = e.getValue()[0], val = e.getValue()[1];
            list.add(map(
                    "channel", e.getKey(),
                    "conversions", round1(conv),
                    "value", Math.round(val),
                    "convShare", totalConv == 0 ? 0 : round1(conv * 100.0 / totalConv),
                    "valueShare", totalValue == 0 ? 0 : round1(val * 100.0 / totalValue)));
        }
        return list;
    }

    /* ------------------------------------------------------------------ */
    /* A/B 实验                                                             */
    /* ------------------------------------------------------------------ */

    public Map<String, Object> abTest(Long tid, Long appId, String from, String to, String metric) {
        LocalDateTime[] r = range(from, to, 30);
        List<Object[]> rows = eventRepository.extWideRows(tid, appId == null ? 0L : appId, r[0], r[1]);
        String met = metric == null || metric.isBlank() ? "purchase" : metric;
        boolean proportion = PROPORTION_METRICS.contains(met);

        Map<String, String> variant = new HashMap<>();          // uid -> A/B
        Map<String, Long> events = new HashMap<>();             // uid -> event count
        Map<String, Double> orderValue = new HashMap<>();       // uid -> purchase value
        Set<String> goalUsersA = new HashSet<>(), goalUsersB = new HashSet<>();
        Map<LocalDate, double[]> daily = new TreeMap<>();        // day -> [A, B]

        for (Object[] row : rows) {
            String uid = nz(row[C_DISTINCT]);
            if (uid.isBlank()) continue;
            String v = (uid.hashCode() & 1) == 0 ? "A" : "B";
            variant.put(uid, v);
            events.merge(uid, 1L, Long::sum);

            String name = nz(row[C_NAME]);
            LocalDate day = toDate(row[C_DAY]);
            double[] dd = daily.computeIfAbsent(day, k -> new double[2]);
            int vi = "A".equals(v) ? 0 : 1;

            if ("purchase".equals(name)) orderValue.merge(uid, parseAmount(nz(row[C_PROPS])), Double::sum);

            if (proportion) {
                if (met.equals(name)) {
                    if ("A".equals(v)) goalUsersA.add(uid); else goalUsersB.add(uid);
                    dd[vi] += 1;
                }
            } else if ("order_value".equals(met)) {
                if ("purchase".equals(name)) dd[vi] += parseAmount(nz(row[C_PROPS]));
            } else { // engagement
                dd[vi] += 1;
            }
        }

        List<String> usersA = variant.entrySet().stream().filter(e -> "A".equals(e.getValue())).map(Map.Entry::getKey).collect(Collectors.toList());
        List<String> usersB = variant.entrySet().stream().filter(e -> "B".equals(e.getValue())).map(Map.Entry::getKey).collect(Collectors.toList());

        Map<String, Object> summary;
        if (proportion) {
            double nA = usersA.size(), nB = usersB.size();
            double xA = goalUsersA.size(), xB = goalUsersB.size();
            double pA = nA == 0 ? 0 : xA / nA, pB = nB == 0 ? 0 : xB / nB;
            double pooled = (xA + xB) / Math.max(1, nA + nB);
            double se = Math.sqrt(pooled * (1 - pooled) * (1 / Math.max(1, nA) + 1 / Math.max(1, nB)));
            double z = se == 0 ? 0 : (pB - pA) / se;
            double p = 2 * (1 - normalCdf(Math.abs(z)));
            double lift = pA == 0 ? 0 : (pB - pA) * 100.0 / pA;
            summary = proportionSummary(met, nA, xA, pA, nB, xB, pB, z, p, lift);
        } else {
            List<Double> a = new ArrayList<>(), b = new ArrayList<>();
            if ("order_value".equals(met)) {
                for (String u : usersA) if (orderValue.containsKey(u)) a.add(orderValue.get(u));
                for (String u : usersB) if (orderValue.containsKey(u)) b.add(orderValue.get(u));
            } else {
                for (String u : usersA) a.add((double) events.getOrDefault(u, 0L));
                for (String u : usersB) b.add((double) events.getOrDefault(u, 0L));
            }
            double mA = mean(a), mB = mean(b);
            double vA = variance(a), vB = variance(b);
            double se = Math.sqrt(vA / Math.max(1, a.size()) + vB / Math.max(1, b.size()));
            double t = se == 0 ? 0 : (mB - mA) / se;
            double p = 2 * (1 - normalCdf(Math.abs(t)));   // large-sample normal approximation
            double lift = mA == 0 ? 0 : (mB - mA) * 100.0 / mA;
            summary = meanSummary(met, a.size(), mA, b.size(), mB, t, p, lift);
        }

        List<Map<String, Object>> dailyOut = new ArrayList<>();
        for (Map.Entry<LocalDate, double[]> e : daily.entrySet()) {
            dailyOut.add(map("date", e.getKey().toString(),
                    "a", round1(e.getValue()[0]), "b", round1(e.getValue()[1])));
        }

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("summary", summary);
        out.put("daily", dailyOut);
        return out;
    }

    private Map<String, Object> proportionSummary(String met, double nA, double xA, double pA,
                                                  double nB, double xB, double pB,
                                                  double z, double p, double lift) {
        boolean sig = p < 0.05;
        Map<String, Object> s = new LinkedHashMap<>();
        s.put("metric", met);
        s.put("metricLabel", metricLabel(met));
        s.put("type", "proportion");
        s.put("variantA", map("name", "变体 A", "users", (long) nA, "conversions", (long) xA, "rate", round1(pA * 100)));
        s.put("variantB", map("name", "变体 B", "users", (long) nB, "conversions", (long) xB, "rate", round1(pB * 100)));
        s.put("lift", round1(lift));
        s.put("statName", "z");
        s.put("stat", round1(z * 100) / 100.0);
        s.put("pValue", Math.round(p * 10000) / 10000.0);
        s.put("confidence", "95%");
        s.put("significant", sig);
        s.put("winner", sig ? (pB > pA ? "B" : "A") : "无");
        s.put("conclusion", conclusion(sig, lift, "转化率"));
        return s;
    }

    private Map<String, Object> meanSummary(String met, double nA, double mA, double nB, double mB,
                                            double t, double p, double lift) {
        boolean sig = p < 0.05;
        Map<String, Object> s = new LinkedHashMap<>();
        s.put("metric", met);
        s.put("metricLabel", metricLabel(met));
        s.put("type", "mean");
        s.put("variantA", map("name", "变体 A", "users", (long) nA, "mean", round1(mA)));
        s.put("variantB", map("name", "变体 B", "users", (long) nB, "mean", round1(mB)));
        s.put("lift", round1(lift));
        s.put("statName", "t");
        s.put("stat", round1(t * 100) / 100.0);
        s.put("pValue", Math.round(p * 10000) / 10000.0);
        s.put("confidence", "95%");
        s.put("significant", sig);
        s.put("winner", sig ? (mB > mA ? "B" : "A") : "无");
        s.put("conclusion", conclusion(sig, lift, metricLabel(met)));
        return s;
    }

    private static String conclusion(boolean sig, double lift, String what) {
        if (!sig) return "两组" + what + "差异不显著（p ≥ 0.05），暂无充分证据表明存在优劣，建议扩大样本或延长实验。";
        String dir = lift >= 0 ? "B 组高于 A 组" : "B 组低于 A 组";
        return "差异显著（p < 0.05），" + dir + " " + Math.abs(round1(lift)) + "%，可考虑采用表现更优的变体。";
    }

    private static String metricLabel(String met) {
        return switch (met) {
            case "purchase" -> "购买转化率";
            case "add_to_cart" -> "加购转化率";
            case "begin_checkout" -> "结算转化率";
            case "sign_up" -> "注册转化率";
            case "order_value" -> "客单价";
            case "engagement" -> "人均事件数";
            default -> met;
        };
    }

    /* ---- statistics helpers ---- */

    private static double mean(List<Double> a) {
        if (a.isEmpty()) return 0;
        double s = 0;
        for (double d : a) s += d;
        return s / a.size();
    }

    private static double variance(List<Double> a) {
        if (a.size() < 2) return 0;
        double m = mean(a), s = 0;
        for (double d : a) s += (d - m) * (d - m);
        return s / (a.size() - 1);
    }

    /** Standard normal CDF via Abramowitz-Stegun erf approximation. */
    private static double normalCdf(double z) {
        return 0.5 * (1 + erf(z / Math.sqrt(2)));
    }

    private static double erf(double x) {
        double sign = x < 0 ? -1 : 1;
        x = Math.abs(x);
        double t = 1.0 / (1.0 + 0.3275911 * x);
        double y = 1.0 - (((((1.061405429 * t - 1.453152027) * t) + 1.421413741) * t - 0.284496736) * t + 0.254829592) * t * Math.exp(-x * x);
        return sign * y;
    }
}
