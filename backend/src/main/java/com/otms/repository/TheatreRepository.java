package com.otms.repository;

import com.otms.model.Theatre;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TheatreRepository extends JpaRepository<Theatre, Long> {

    List<Theatre> findByActiveTrueOrderByName();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Theatre t where t.id = :id")
    Optional<Theatre> findByIdForUpdate(@Param("id") Long id);
}
