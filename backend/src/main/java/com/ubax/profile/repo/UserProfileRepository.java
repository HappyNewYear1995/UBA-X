package com.ubax.profile.repo;

import com.ubax.profile.domain.UserProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    Optional<UserProfile> findByTenantIdAndDistinctId(Long tenantId, String distinctId);

    Page<UserProfile> findByTenantId(Long tenantId, Pageable pageable);

    List<UserProfile> findAllByTenantIdOrderByIdAsc(Long tenantId);

    @Query("SELECT p FROM UserProfile p WHERE p.tenantId = :tid AND " +
            "(:kw IS NULL OR p.distinctId LIKE %:kw% OR p.userId LIKE %:kw% OR p.region LIKE %:kw%)")
    Page<UserProfile> search(@Param("tid") Long tid, @Param("kw") String kw, Pageable pageable);

    long countByTenantId(Long tenantId);

    long countByTenantIdAndFirstSeenBetween(Long tenantId, java.time.LocalDateTime from, java.time.LocalDateTime to);

    @Query("SELECT COUNT(p) FROM UserProfile p WHERE p.tenantId = :tid AND p.tags LIKE %:tag%")
    long countByTag(@Param("tid") Long tid, @Param("tag") String tag);
}
