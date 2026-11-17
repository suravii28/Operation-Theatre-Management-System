
package com.otms.repository;

import com.otms.model.AuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuditEventRepository
        extends JpaRepository<AuditEvent, Long> {

    List<AuditEvent> findAllByOrderByCreatedAtDesc();

    List<AuditEvent> findByActorEmailContainingIgnoreCaseOrderByCreatedAtDesc(
            String actorEmail);

    List<AuditEvent> findByActionIgnoreCaseOrderByCreatedAtDesc(
            String action);

    @Query("""
            SELECT e FROM AuditEvent e
            WHERE (:actor IS NULL OR LOWER(e.actorEmail) LIKE LOWER(CONCAT('%', :actor, '%')))
              AND (:action IS NULL OR LOWER(e.action) = LOWER(:action))
              AND (:fromTime IS NULL OR e.createdAt >= :fromTime)
              AND (:toTime IS NULL OR e.createdAt <= :toTime)
            ORDER BY e.createdAt DESC
            """)
    List<AuditEvent> search(
            @Param("actor") String actor,
            @Param("action") String action,
            @Param("fromTime") LocalDateTime fromTime,
            @Param("toTime") LocalDateTime toTime);
}
