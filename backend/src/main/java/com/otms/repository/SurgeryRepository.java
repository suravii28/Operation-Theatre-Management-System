package com.otms.repository;

import com.otms.model.Surgery;
import com.otms.model.SurgeryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SurgeryRepository extends JpaRepository<Surgery, Long> {

    @Query("""
            SELECT s FROM Surgery s
            WHERE s.status IN :statuses
              AND s.startTime < :end
              AND s.endTime > :start
            """)
    List<Surgery> findOverlapping(@Param("statuses") List<SurgeryStatus> statuses,
                                  @Param("start") LocalDateTime start,
                                  @Param("end") LocalDateTime end);

    List<Surgery> findByStartTimeBetweenOrderByStartTime(LocalDateTime from, LocalDateTime to);

    List<Surgery> findByEmergencyTrueAndStatusInOrderByStartTime(List<SurgeryStatus> statuses);
}
