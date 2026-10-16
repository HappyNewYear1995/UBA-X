package com.ubax.system.repo;

import com.ubax.system.domain.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /** Operation log: everything except login/logout. tid null => all tenants (super admin). */
    @Query("SELECT a FROM AuditLog a WHERE (:tid IS NULL OR a.tenantId = :tid) " +
            "AND a.type NOT IN ('LOGIN','LOGOUT') " +
            "AND (:kw IS NULL OR a.username LIKE %:kw% OR a.action LIKE %:kw% OR a.detail LIKE %:kw%) " +
            "AND (:type IS NULL OR a.type = :type) " +
            "AND (:module IS NULL OR a.module = :module) " +
            "AND (:from IS NULL OR a.createdAt >= :from) AND (:to IS NULL OR a.createdAt <= :to) " +
            "ORDER BY a.createdAt DESC")
    Page<AuditLog> searchOperations(@Param("tid") Long tid, @Param("kw") String kw, @Param("type") String type,
                                    @Param("module") String module, @Param("from") LocalDateTime from,
                                    @Param("to") LocalDateTime to, Pageable pageable);

    /** Login log: only login/logout events. */
    @Query("SELECT a FROM AuditLog a WHERE (:tid IS NULL OR a.tenantId = :tid) " +
            "AND a.type IN ('LOGIN','LOGOUT') " +
            "AND (:kw IS NULL OR a.username LIKE %:kw% OR a.ip LIKE %:kw%) " +
            "AND (:success IS NULL OR a.success = :success) " +
            "AND (:from IS NULL OR a.createdAt >= :from) AND (:to IS NULL OR a.createdAt <= :to) " +
            "ORDER BY a.createdAt DESC")
    Page<AuditLog> searchLogins(@Param("tid") Long tid, @Param("kw") String kw, @Param("success") Boolean success,
                                @Param("from") LocalDateTime from, @Param("to") LocalDateTime to, Pageable pageable);

    /** A single user's own login history (for 个人中心). */
    @Query("SELECT a FROM AuditLog a WHERE a.userId = :uid AND a.type IN ('LOGIN','LOGOUT') " +
            "ORDER BY a.createdAt DESC")
    List<AuditLog> findLoginsByUser(@Param("uid") Long uid, Pageable pageable);

    @Query("SELECT DISTINCT a.module FROM AuditLog a WHERE a.module IS NOT NULL " +
            "AND (:tid IS NULL OR a.tenantId = :tid) AND a.type NOT IN ('LOGIN','LOGOUT')")
    List<String> distinctModules(@Param("tid") Long tid);

    long countByTenantId(Long tenantId);

    @Modifying
    @Query("DELETE FROM AuditLog a WHERE a.createdAt < :cutoff")
    int deleteBefore(@Param("cutoff") LocalDateTime cutoff);
}
