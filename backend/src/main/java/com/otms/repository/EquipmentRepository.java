package com.otms.repository;

import com.otms.model.Equipment;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

    List<Equipment> findByAvailableTrueOrderByName();

    List<Equipment> findAllByOrderByName();

    /**
     * Pessimistic lock over a set of equipment rows. The fixed theatre →
     * surgeon → equipment lock order (and ascending ids here) keeps
     * concurrent scheduling transactions deadlock-free (OTMS-NF-006).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Equipment e where e.id in :ids order by e.id")
    List<Equipment> findAllByIdForUpdate(@Param("ids") Collection<Long> ids);
}
