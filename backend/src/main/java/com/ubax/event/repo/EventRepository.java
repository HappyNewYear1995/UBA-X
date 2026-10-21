package com.ubax.event.repo;

import com.ubax.event.domain.Event;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    /** Purge events older than the cutoff (used by the data-retention scheduled task). */
    @Modifying
    @Query("DELETE FROM Event e WHERE e.eventTime < :cutoff")
    int deleteBefore(@Param("cutoff") LocalDateTime cutoff);

    List<Event> findByTenantIdOrderByEventTimeDesc(Long tenantId, Pageable pageable);

    long countByTenantId(Long tenantId);

    /** Raw rows for the security extension app: ip / ua / session / user / time. */
    @Query(value = "SELECT ip, user_agent, session_id, distinct_id, event_time FROM uba_event " +
            "WHERE tenant_id = :tid AND (:appId = 0 OR app_id = :appId) AND event_time BETWEEN :from AND :to",
            nativeQuery = true)
    List<Object[]> extSecurityRows(@Param("tid") Long tid, @Param("appId") Long appId,
                                   @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    /** Raw rows for the geo extension app: country / region / city / device / user / day. */
    @Query(value = "SELECT country, region, city, device, distinct_id, CAST(event_time AS DATE) FROM uba_event " +
            "WHERE tenant_id = :tid AND (:appId = 0 OR app_id = :appId) AND event_time BETWEEN :from AND :to",
            nativeQuery = true)
    List<Object[]> extGeoRows(@Param("tid") Long tid, @Param("appId") Long appId,
                              @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    /** Raw rows for the client-fingerprint extension app: device / os / browser / screen / user. */
    @Query(value = "SELECT device, os, browser, screen, distinct_id FROM uba_event " +
            "WHERE tenant_id = :tid AND (:appId = 0 OR app_id = :appId) AND event_time BETWEEN :from AND :to",
            nativeQuery = true)
    List<Object[]> extFingerprintRows(@Param("tid") Long tid, @Param("appId") Long appId,
                                      @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    /**
     * Wide row set reused by the analytical extension apps (path / funnel / retention /
     * journey / anomaly / page-experience / event-explorer). Columns (index):
     * 0 distinct_id, 1 session_id, 2 event_type, 3 event_name, 4 url_path, 5 title,
     * 6 referrer, 7 device, 8 os, 9 browser, 10 region, 11 city, 12 properties,
     * 13 duration, 14 event_time, 15 day(date).
     */
    @Query(value = "SELECT distinct_id, session_id, event_type, event_name, url_path, title, referrer, " +
            "device, os, browser, region, city, properties, duration, event_time, CAST(event_time AS DATE) " +
            "FROM uba_event WHERE tenant_id = :tid AND (:appId = 0 OR app_id = :appId) " +
            "AND event_time BETWEEN :from AND :to ORDER BY event_time", nativeQuery = true)
    List<Object[]> extWideRows(@Param("tid") Long tid, @Param("appId") Long appId,
                               @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    List<Event> findByTenantIdAndDistinctIdOrderByEventTimeDesc(Long tenantId, String distinctId, Pageable pageable);

    @Query(value = "SELECT CAST(event_time AS DATE) AS d, COUNT(*) AS pv, COUNT(DISTINCT distinct_id) AS uv " +
            "FROM uba_event WHERE tenant_id = :tid AND event_type = 'pageview' " +
            "AND (:appId = 0 OR app_id = :appId) " +
            "AND event_time BETWEEN :from AND :to GROUP BY CAST(event_time AS DATE) ORDER BY d", nativeQuery = true)
    List<Object[]> dailyTrend(@Param("tid") Long tid, @Param("appId") Long appId,
                              @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query(value = "SELECT COUNT(*) FROM uba_event WHERE tenant_id = :tid " +
            "AND (:appId = 0 OR app_id = :appId) AND event_time BETWEEN :from AND :to", nativeQuery = true)
    long countInRange(@Param("tid") Long tid, @Param("appId") Long appId,
                      @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query(value = "SELECT COUNT(*) FROM uba_event WHERE tenant_id = :tid AND event_type = 'pageview' " +
            "AND (:appId = 0 OR app_id = :appId) AND event_time BETWEEN :from AND :to", nativeQuery = true)
    long countPageviews(@Param("tid") Long tid, @Param("appId") Long appId,
                        @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query(value = "SELECT COUNT(DISTINCT distinct_id) FROM uba_event WHERE tenant_id = :tid " +
            "AND (:appId = 0 OR app_id = :appId) AND event_time BETWEEN :from AND :to", nativeQuery = true)
    long uvInRange(@Param("tid") Long tid, @Param("appId") Long appId,
                   @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query(value = "SELECT COUNT(DISTINCT session_id) FROM uba_event WHERE tenant_id = :tid " +
            "AND (:appId = 0 OR app_id = :appId) AND event_time BETWEEN :from AND :to", nativeQuery = true)
    long sessionsInRange(@Param("tid") Long tid, @Param("appId") Long appId,
                         @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query(value = "SELECT event_name, COUNT(*) c, COUNT(DISTINCT distinct_id) u FROM uba_event " +
            "WHERE tenant_id = :tid AND event_name IS NOT NULL AND event_type IN ('event','custom','click') " +
            "AND (:appId = 0 OR app_id = :appId) AND event_time BETWEEN :from AND :to " +
            "GROUP BY event_name ORDER BY c DESC LIMIT :limit", nativeQuery = true)
    List<Object[]> topEvents(@Param("tid") Long tid, @Param("appId") Long appId,
                             @Param("from") LocalDateTime from, @Param("to") LocalDateTime to,
                             @Param("limit") int limit);

    @Query(value = "SELECT url_path, title, COUNT(*) pv, COUNT(DISTINCT distinct_id) uv, AVG(duration) avg_dur " +
            "FROM uba_event WHERE tenant_id = :tid AND event_type = 'pageview' AND url_path IS NOT NULL " +
            "AND (:appId = 0 OR app_id = :appId) AND event_time BETWEEN :from AND :to " +
            "GROUP BY url_path, title ORDER BY pv DESC LIMIT :limit", nativeQuery = true)
    List<Object[]> topPages(@Param("tid") Long tid, @Param("appId") Long appId,
                            @Param("from") LocalDateTime from, @Param("to") LocalDateTime to,
                            @Param("limit") int limit);

    @Query(value = "SELECT device, COUNT(*) c FROM uba_event WHERE tenant_id = :tid AND event_type='pageview' " +
            "AND (:appId = 0 OR app_id = :appId) AND event_time BETWEEN :from AND :to " +
            "GROUP BY device ORDER BY c DESC", nativeQuery = true)
    List<Object[]> deviceDistribution(@Param("tid") Long tid, @Param("appId") Long appId,
                                      @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query(value = "SELECT browser, COUNT(*) c FROM uba_event WHERE tenant_id = :tid AND event_type='pageview' " +
            "AND (:appId = 0 OR app_id = :appId) AND event_time BETWEEN :from AND :to " +
            "GROUP BY browser ORDER BY c DESC", nativeQuery = true)
    List<Object[]> browserDistribution(@Param("tid") Long tid, @Param("appId") Long appId,
                                       @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query(value = "SELECT region, COUNT(*) c, COUNT(DISTINCT distinct_id) u FROM uba_event " +
            "WHERE tenant_id = :tid AND event_type='pageview' AND region IS NOT NULL " +
            "AND (:appId = 0 OR app_id = :appId) AND event_time BETWEEN :from AND :to " +
            "GROUP BY region ORDER BY c DESC LIMIT 15", nativeQuery = true)
    List<Object[]> geoDistribution(@Param("tid") Long tid, @Param("appId") Long appId,
                                   @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query(value = "SELECT referrer, COUNT(*) c FROM uba_event WHERE tenant_id = :tid AND event_type='pageview' " +
            "AND referrer IS NOT NULL AND referrer <> '' AND (:appId = 0 OR app_id = :appId) " +
            "AND event_time BETWEEN :from AND :to GROUP BY referrer ORDER BY c DESC LIMIT 10", nativeQuery = true)
    List<Object[]> referrerDistribution(@Param("tid") Long tid, @Param("appId") Long appId,
                                        @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    /** Count distinct users who performed a given event (funnel step). */
    @Query(value = "SELECT COUNT(DISTINCT distinct_id) FROM uba_event WHERE tenant_id = :tid " +
            "AND event_name = :name AND (:appId = 0 OR app_id = :appId) " +
            "AND event_time BETWEEN :from AND :to", nativeQuery = true)
    long distinctUsersForEvent(@Param("tid") Long tid, @Param("appId") Long appId, @Param("name") String name,
                               @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    /** Users who did step A and later step B (ordered funnel). */
    @Query(value = "SELECT COUNT(DISTINCT a.distinct_id) FROM uba_event a JOIN uba_event b " +
            "ON a.distinct_id = b.distinct_id AND a.tenant_id = b.tenant_id " +
            "WHERE a.tenant_id = :tid AND a.event_name = :stepA AND b.event_name = :stepB " +
            "AND b.event_time >= a.event_time AND a.event_time BETWEEN :from AND :to " +
            "AND b.event_time BETWEEN :from AND :to", nativeQuery = true)
    long funnelStepPair(@Param("tid") Long tid, @Param("stepA") String stepA, @Param("stepB") String stepB,
                        @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    /** Distinct users active in the anchor window. */
    @Query(value = "SELECT DISTINCT distinct_id FROM uba_event WHERE tenant_id = :tid " +
            "AND (:appId = 0 OR app_id = :appId) AND event_time BETWEEN :from AND :to", nativeQuery = true)
    List<String> distinctUsersInRange(@Param("tid") Long tid, @Param("appId") Long appId,
                                      @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    /** Of a given user set, how many were active in a later window (retention). */
    @Query(value = "SELECT COUNT(DISTINCT distinct_id) FROM uba_event WHERE tenant_id = :tid " +
            "AND (:appId = 0 OR app_id = :appId) AND distinct_id IN :users " +
            "AND event_time BETWEEN :from AND :to", nativeQuery = true)
    long retainedUsers(@Param("tid") Long tid, @Param("appId") Long appId, @Param("users") List<String> users,
                       @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query(value = "SELECT (x + COALESCE(scroll_x, 0)) ax, (y + COALESCE(scroll_y, 0)) ay, element, " +
            "COUNT(*) c, AVG(COALESCE(doc_w, 0)) dw, AVG(COALESCE(doc_h, 0)) dh " +
            "FROM uba_event WHERE tenant_id = :tid " +
            "AND event_type = 'click' AND x IS NOT NULL AND (:path = '' OR url_path = :path) " +
            "AND (:appId = 0 OR app_id = :appId) AND event_time BETWEEN :from AND :to " +
            "GROUP BY (x + COALESCE(scroll_x, 0)), (y + COALESCE(scroll_y, 0)), element", nativeQuery = true)
    List<Object[]> heatmapPoints(@Param("tid") Long tid, @Param("appId") Long appId, @Param("path") String path,
                                 @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    /** Most-reported absolute URL for a path, used to snapshot the real page. */
    @Query(value = "SELECT url FROM uba_event WHERE tenant_id = :tid " +
            "AND (:appId = 0 OR app_id = :appId) AND url_path = :path " +
            "AND url IS NOT NULL AND url <> '' AND event_time BETWEEN :from AND :to " +
            "GROUP BY url ORDER BY COUNT(*) DESC LIMIT 1", nativeQuery = true)
    String heatmapRepresentativeUrl(@Param("tid") Long tid, @Param("appId") Long appId, @Param("path") String path,
                                    @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query(value = "SELECT DISTINCT session_id FROM uba_event WHERE tenant_id = :tid " +
            "AND session_id IS NOT NULL AND (:appId = 0 OR app_id = :appId) " +
            "AND event_time BETWEEN :from AND :to ORDER BY session_id DESC LIMIT :limit", nativeQuery = true)
    List<String> recentSessions(@Param("tid") Long tid, @Param("appId") Long appId,
                                @Param("from") LocalDateTime from, @Param("to") LocalDateTime to,
                                @Param("limit") int limit);

    @Query(value = "SELECT session_id, MIN(distinct_id), MIN(event_time) st, MAX(event_time) en, COUNT(*) c, " +
            "MIN(url_path) entry FROM uba_event WHERE tenant_id = :tid AND session_id IS NOT NULL " +
            "AND (:appId = 0 OR app_id = :appId) AND event_time BETWEEN :from AND :to " +
            "GROUP BY session_id ORDER BY st DESC LIMIT :limit", nativeQuery = true)
    List<Object[]> sessionSummaries(@Param("tid") Long tid, @Param("appId") Long appId,
                                    @Param("from") LocalDateTime from, @Param("to") LocalDateTime to,
                                    @Param("limit") int limit);

    List<Event> findByTenantIdAndSessionIdOrderByEventTimeAsc(Long tenantId, String sessionId);

    /** distinct_id -> first occurrence time of an event within range (for ordered funnels). */
    @Query(value = "SELECT distinct_id, MIN(event_time) FROM uba_event WHERE tenant_id = :tid " +
            "AND event_name = :name AND (:appId = 0 OR app_id = :appId) " +
            "AND event_time BETWEEN :from AND :to GROUP BY distinct_id", nativeQuery = true)
    List<Object[]> eventFirstTimes(@Param("tid") Long tid, @Param("appId") Long appId, @Param("name") String name,
                                   @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query(value = "SELECT COUNT(*) FROM (SELECT session_id FROM uba_event WHERE tenant_id = :tid " +
            "AND event_type = 'pageview' AND (:appId = 0 OR app_id = :appId) " +
            "AND event_time BETWEEN :from AND :to GROUP BY session_id HAVING COUNT(*) > 1) t", nativeQuery = true)
    long multiPageviewSessions(@Param("tid") Long tid, @Param("appId") Long appId,
                               @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query(value = "SELECT AVG(duration) FROM uba_event WHERE tenant_id = :tid AND event_type = 'leave' " +
            "AND duration IS NOT NULL AND (:appId = 0 OR app_id = :appId) " +
            "AND event_time BETWEEN :from AND :to", nativeQuery = true)
    Double avgDuration(@Param("tid") Long tid, @Param("appId") Long appId,
                       @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query(value = "SELECT COUNT(*) FROM uba_event WHERE tenant_id = :tid " +
            "AND (:appId = 0 OR app_id = :appId) AND event_time >= :from", nativeQuery = true)
    long countSince(@Param("tid") Long tid, @Param("appId") Long appId, @Param("from") LocalDateTime from);

    @Query(value = "SELECT MIN(event_time) FROM uba_event WHERE tenant_id = :tid AND distinct_id = :distinctId", nativeQuery = true)
    LocalDateTime firstSeen(@Param("tid") Long tid, @Param("distinctId") String distinctId);

    /** New users: distinct users whose first-ever event (within tenant/app scope) falls in the range. */
    @Query(value = "SELECT COUNT(*) FROM (SELECT distinct_id FROM uba_event WHERE tenant_id = :tid " +
            "AND (:appId = 0 OR app_id = :appId) GROUP BY distinct_id " +
            "HAVING MIN(event_time) BETWEEN :from AND :to) t", nativeQuery = true)
    long newUsersInRange(@Param("tid") Long tid, @Param("appId") Long appId,
                         @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
