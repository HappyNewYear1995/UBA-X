package com.ubax.stream;

import com.ubax.event.domain.Event;
import com.ubax.stream.domain.RealtimeMinute;
import com.ubax.stream.repo.RealtimeMinuteRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * In-app realtime stream processing engine. Acts as the lightweight stand-in for a
 * Flink job on constrained hardware: a bounded queue decouples ingestion from writes,
 * a worker thread batches persistence, and in-memory windowed accumulators maintain
 * per-minute rollups and user-profile deltas that are flushed periodically.
 */
@Component
public class StreamEngine {

    private static final Logger log = LoggerFactory.getLogger(StreamEngine.class);

    private final StreamWriter streamWriter;
    private final RealtimeMinuteRepository realtimeRepository;
    private final int capacity;
    private final long flushIntervalMs;

    private static final java.time.ZoneOffset ZONE = java.time.ZoneOffset.ofHours(8);

    private final BlockingQueue<Event> queue;
    private final AtomicLong received = new AtomicLong();
    private final AtomicLong persisted = new AtomicLong();
    private final AtomicLong dropped = new AtomicLong();

    /** key = tenant|app|minuteEpoch -> accumulator */
    private final Map<String, MinuteAcc> minutes = new ConcurrentHashMap<>();
    /** key = tenant|distinctId -> latest event + deltas */
    private final Map<String, ProfileDelta> profiles = new ConcurrentHashMap<>();
    /** recently seen session ids for visit counting */
    private final Set<String> activeSessions = ConcurrentHashMap.newKeySet();

    private volatile boolean running = true;
    private Thread worker;
    private ScheduledExecutorService flusher;

    public StreamEngine(StreamWriter streamWriter,
                        RealtimeMinuteRepository realtimeRepository,
                        @Value("${ubax.stream.queue-capacity:50000}") int capacity,
                        @Value("${ubax.stream.flush-interval-ms:5000}") long flushIntervalMs) {
        this.streamWriter = streamWriter;
        this.realtimeRepository = realtimeRepository;
        this.capacity = capacity;
        this.flushIntervalMs = flushIntervalMs;
        this.queue = new LinkedBlockingQueue<>(capacity);
    }

    static class MinuteAcc {
        long pv, events;
        // UV of distinct ids seen live in this window. On (re)seed from an existing
        // DB row we cannot rebuild the distinct set, so we remember the persisted
        // count as a monotonic floor and never write a smaller UV than already stored.
        long seededUv;
        final Set<String> uvs = ConcurrentHashMap.newKeySet();
    }

    static class ProfileDelta {
        Event last;
        long eventAdd, pageviewAdd;
        boolean newVisit;
    }

    public boolean submit(Event e) {
        received.incrementAndGet();
        boolean ok = queue.offer(e);
        if (!ok) dropped.incrementAndGet();
        return ok;
    }

    public int queueSize() { return queue.size(); }

    public Map<String, Object> metrics() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("received", received.get());
        m.put("persisted", persisted.get());
        m.put("dropped", dropped.get());
        m.put("queueSize", queue.size());
        m.put("queueCapacity", capacity);
        m.put("activeWindows", minutes.size());
        return m;
    }

    @PostConstruct
    public void start() {
        worker = new Thread(this::loop, "ubax-stream-worker");
        worker.setDaemon(true);
        worker.start();
        flusher = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "ubax-stream-flusher");
            t.setDaemon(true);
            return t;
        });
        flusher.scheduleWithFixedDelay(this::safeFlush, flushIntervalMs, flushIntervalMs, TimeUnit.MILLISECONDS);
        log.info("StreamEngine started (capacity={}, flushInterval={}ms)", capacity, flushIntervalMs);
    }

    private void loop() {
        List<Event> buf = new ArrayList<>(512);
        while (running) {
            try {
                buf.clear();
                Event first = queue.poll(400, TimeUnit.MILLISECONDS);
                if (first != null) {
                    buf.add(first);
                    queue.drainTo(buf, 511);
                    process(buf);
                }
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception ex) {
                log.error("stream worker error", ex);
            }
        }
    }

    private void process(List<Event> batch) {
        try {
            int n = streamWriter.persistEvents(batch);
            persisted.addAndGet(n);
        } catch (Exception ex) {
            log.error("failed to persist event batch ({} events)", batch.size(), ex);
        }
        LocalDateTime now = LocalDateTime.now();
        long curMin = now.truncatedTo(ChronoUnit.MINUTES).toEpochSecond(ZONE) / 60;
        for (Event e : batch) {
            // minute window
            LocalDateTime minute = e.getEventTime().truncatedTo(ChronoUnit.MINUTES);
            long epochMin = minute.toEpochSecond(ZONE) / 60;
            String mk = e.getTenantId() + "|" + (e.getAppId() == null ? 0 : e.getAppId()) + "|" + epochMin;
            MinuteAcc acc = minutes.computeIfAbsent(mk, k -> seed(e.getTenantId(), e.getAppId(), minute));
            synchronized (acc) {
                acc.events++;
                if ("pageview".equals(e.getEventType())) acc.pv++;
                if (e.getDistinctId() != null) acc.uvs.add(e.getDistinctId());
            }
            // profile delta
            if (e.getDistinctId() != null) {
                String pk = e.getTenantId() + "|" + e.getDistinctId();
                ProfileDelta d = profiles.computeIfAbsent(pk, k -> new ProfileDelta());
                synchronized (d) {
                    d.last = e;
                    d.eventAdd++;
                    if ("pageview".equals(e.getEventType())) d.pageviewAdd++;
                    if (e.getSessionId() != null && activeSessions.add(e.getSessionId())) {
                        d.newVisit = true;
                    }
                }
            }
        }
        // evict stale session ids (older windows) occasionally
        if (activeSessions.size() > 20000) activeSessions.clear();
        // evict minute windows older than 3 minutes (already flushed)
        minutes.keySet().removeIf(k -> {
            try {
                String[] parts = k.split("\\|");
                long epochMinute = Long.parseLong(parts[2]);
                return epochMinute < curMin - 3;
            } catch (Exception ex) {
                return false;
            }
        });
    }

    private MinuteAcc seed(Long tenantId, Long appId, LocalDateTime minute) {
        MinuteAcc acc = new MinuteAcc();
        try {
            Optional<RealtimeMinute> existing =
                    realtimeRepository.findByTenantIdAndAppIdAndMinute(tenantId, appId == null ? 0L : appId, minute);
            existing.ifPresent(m -> {
                acc.pv = m.getPv();
                acc.events = m.getEvents();
                acc.seededUv = m.getUv();
            });
        } catch (Exception ignored) {}
        return acc;
    }

    private void safeFlush() {
        try {
            flush();
        } catch (Exception ex) {
            log.error("flush error", ex);
        }
    }

    private void flush() {
        // flush minute windows
        for (Map.Entry<String, MinuteAcc> en : minutes.entrySet()) {
            String[] parts = en.getKey().split("\\|");
            Long tenantId = Long.valueOf(parts[0]);
            Long appId = Long.valueOf(parts[1]);
            LocalDateTime minute = LocalDateTime.ofEpochSecond(Long.parseLong(parts[2]) * 60, 0, java.time.ZoneOffset.ofHours(8));
            MinuteAcc acc = en.getValue();
            long pv, events;
            long uv;
            synchronized (acc) {
                pv = acc.pv; events = acc.events;
                // Never regress UV below what was already persisted for this minute
                // (the live distinct set is empty after a re-seed / restart).
                uv = Math.max(acc.seededUv, acc.uvs.size());
            }
            streamWriter.setMinute(tenantId, appId, minute, pv, uv, events);
        }
        // flush profile deltas
        Iterator<Map.Entry<String, ProfileDelta>> it = profiles.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, ProfileDelta> en = it.next();
            ProfileDelta d = en.getValue();
            Event last; long ea, pa; boolean nv;
            synchronized (d) {
                last = d.last; ea = d.eventAdd; pa = d.pageviewAdd; nv = d.newVisit;
                d.eventAdd = 0; d.pageviewAdd = 0; d.newVisit = false;
            }
            if (last != null) {
                try {
                    streamWriter.upsertProfile(last, ea, pa, nv);
                } catch (Exception ex) {
                    log.warn("profile upsert failed for {}", last.getDistinctId(), ex);
                }
            }
            it.remove();
        }
    }

    @PreDestroy
    public void stop() {
        running = false;
        try {
            if (flusher != null) flusher.shutdownNow();
            // drain remaining
            List<Event> rest = new ArrayList<>();
            queue.drainTo(rest);
            if (!rest.isEmpty()) process(rest);
            flush();
        } catch (Exception ignored) {}
        log.info("StreamEngine stopped. persisted={}, dropped={}", persisted.get(), dropped.get());
    }
}
