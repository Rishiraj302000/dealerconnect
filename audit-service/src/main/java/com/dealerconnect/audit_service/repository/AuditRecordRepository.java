package com.dealerconnect.audit_service.repository;

import com.dealerconnect.audit_service.entity.AuditRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AuditRecordRepository extends JpaRepository<AuditRecord, Long> {

    /**
     * Returns audit history newest-first, optionally narrowed by action, entity type
     * and/or the user who performed it. Any criterion left null is ignored.
     */
    @Query("""
            SELECT a FROM AuditRecord a
            WHERE (:action IS NULL OR a.action = :action)
              AND (:entityType IS NULL OR a.entityType = :entityType)
              AND (:performedBy IS NULL OR a.performedBy = :performedBy)
            ORDER BY a.timestamp DESC
            """)
    List<AuditRecord> findHistory(@Param("action") String action,
                                  @Param("entityType") String entityType,
                                  @Param("performedBy") String performedBy);
}
