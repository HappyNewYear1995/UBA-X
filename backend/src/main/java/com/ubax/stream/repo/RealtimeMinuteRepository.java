package com.ubax.stream.repo;

import com.ubax.stream.domain.RealtimeMinute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RealtimeMinuteRepository extends JpaRepository<RealtimeMinute, Long> {

    Optional<RealtimeMinute> findByTenantIdAndAppIdAndMinute(Long tenantId, Long appId, LocalDateTime minute);

    @Query(value = "SELECT minute, SUM(pv) pv, SUM(uv) uv, SUM(events) ev FROM uba_realtime_minute " +
            "WHERE tenant_id = :tid AND (:appId = 0 OR app_id = :appId) AND minute >= :from " +
            "GROUP BY minute ORDER BY minute", nativeQuery = true)
    List<Object[]> series(@Param("tid") Long tid, @Param("appId") Long appId, @Param("from") LocalDateTime from);

    @Query(value = "SELECT COALESCE(SUM(pv),0), COALESCE(SUM(uv),0), COALESCE(SUM(events),0) " +
            "FROM uba_realtime_minute WHERE tenant_id = :tid AND (:appId = 0 OR app_id = :appId) " +
            "AND minute >= :from", nativeQuery = true)
    List<Object[]> totalsSince(@Param("tid") Long tid, @Param("appId") Long appId, @Param("from") LocalDateTime from);
}
