package com.ubax.stream;

import com.ubax.event.domain.Event;
import com.ubax.event.repo.EventRepository;
import com.ubax.profile.domain.UserProfile;
import com.ubax.profile.repo.UserProfileRepository;
import com.ubax.stream.domain.RealtimeMinute;
import com.ubax.stream.repo.RealtimeMinuteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Transactional writer used by the {@link StreamEngine} worker thread. Kept as a
 * separate bean so Spring's transaction proxy applies to batched flushes.
 */
@Service
public class StreamWriter {

    private final EventRepository eventRepository;
    private final RealtimeMinuteRepository realtimeRepository;
    private final UserProfileRepository profileRepository;

    public StreamWriter(EventRepository eventRepository, RealtimeMinuteRepository realtimeRepository,
                        UserProfileRepository profileRepository) {
        this.eventRepository = eventRepository;
        this.realtimeRepository = realtimeRepository;
        this.profileRepository = profileRepository;
    }

    @Transactional
    public int persistEvents(List<Event> events) {
        if (events.isEmpty()) return 0;
        eventRepository.saveAll(events);
        return events.size();
    }

    /** Absolute upsert of a per-minute rollup (values are totals, not deltas). */
    @Transactional
    public void setMinute(Long tenantId, Long appId, LocalDateTime minute, long pv, long uv, long events) {
        RealtimeMinute m = realtimeRepository
                .findByTenantIdAndAppIdAndMinute(tenantId, appId, minute)
                .orElseGet(() -> {
                    RealtimeMinute nm = new RealtimeMinute();
                    nm.setTenantId(tenantId);
                    nm.setAppId(appId == null ? 0L : appId);
                    nm.setMinute(minute);
                    return nm;
                });
        m.setPv(pv);
        m.setUv(uv);
        m.setEvents(events);
        realtimeRepository.save(m);
    }

    @Transactional
    public void upsertProfile(Event last, long eventAdd, long pageviewAdd, boolean newVisit) {
        UserProfile p = profileRepository
                .findByTenantIdAndDistinctId(last.getTenantId(), last.getDistinctId())
                .orElseGet(() -> {
                    UserProfile np = new UserProfile();
                    np.setTenantId(last.getTenantId());
                    np.setDistinctId(last.getDistinctId());
                    np.setFirstSeen(last.getEventTime());
                    np.setVisitCount(0L);
                    np.setEventCount(0L);
                    np.setPageviewCount(0L);
                    return np;
                });
        p.setAppId(last.getAppId());
        p.setLastSeen(last.getEventTime());
        p.setEventCount(p.getEventCount() + eventAdd);
        p.setPageviewCount(p.getPageviewCount() + pageviewAdd);
        if (newVisit) p.setVisitCount(p.getVisitCount() + 1);
        if (last.getUserId() != null) p.setUserId(last.getUserId());
        if (last.getUrl() != null) p.setLastUrl(last.getUrl());
        if (last.getDevice() != null) p.setDevice(last.getDevice());
        if (last.getOs() != null) p.setOs(last.getOs());
        if (last.getBrowser() != null) p.setBrowser(last.getBrowser());
        if (last.getRegion() != null) p.setRegion(last.getRegion());
        if (last.getCity() != null) p.setCity(last.getCity());
        p.setTags(deriveTags(p));
        profileRepository.save(p);
    }

    private String deriveTags(UserProfile p) {
        StringBuilder sb = new StringBuilder();
        if (p.getVisitCount() >= 5) sb.append("高频用户,");
        if (p.getEventCount() >= 50) sb.append("活跃用户,");
        if ("Mobile".equals(p.getDevice()) || "MiniProgram".equals(p.getDevice())) sb.append("移动端,");
        if ("Desktop".equals(p.getDevice())) sb.append("桌面端,");
        if (sb.length() == 0) sb.append("新用户,");
        return sb.substring(0, Math.min(sb.length(), 255) - (sb.charAt(sb.length() - 1) == ',' ? 1 : 0));
    }
}
