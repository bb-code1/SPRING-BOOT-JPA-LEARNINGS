package com.zbs.booking.repository;

import com.zbs.booking.domain.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("SELECT b FROM Booking b JOIN FETCH b.tickets")
    List<Booking> findAllWithTicketsFetch();

    @Query(value = "SELECT * FROM bookings", nativeQuery = true)
    List<Booking> findAllIncludingCancelledNative();
}