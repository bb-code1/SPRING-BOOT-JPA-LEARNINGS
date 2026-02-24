package com.zbs.booking.repository;

import com.zbs.booking.domain.EventInventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface EventInventoryRepository extends JpaRepository<EventInventory, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ei FROM EventInventory ei WHERE ei.eventId = :eventId")
    Optional<EventInventory> findByEventIdForUpdate(Long eventId);
}