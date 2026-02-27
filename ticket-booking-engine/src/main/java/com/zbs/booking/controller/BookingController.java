package com.zbs.booking.controller;

import com.zbs.booking.domain.Booking;
import com.zbs.booking.repository.BookingRepository;
import com.zbs.booking.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BookingController {

    private final ReservationService reservationService;
    private final BookingRepository bookingRepository;

    @PostMapping
    public ResponseEntity<Booking> createBooking(
            @RequestParam Long userId,
            @RequestParam Long eventId,
            @RequestParam Integer quantity) {
        return ResponseEntity.ok(reservationService.reserveTickets(userId, eventId, quantity));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelBooking(@PathVariable Long id) {
        // Triggers soft-delete mapping (@SQLDelete)
        bookingRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/fetch-join")
    public ResponseEntity<List<Booking>> getActiveBookingsWithFetch() {
        // Prevents N+1 SELECT queries using JOIN FETCH (Day 11)
        return ResponseEntity.ok(bookingRepository.findAllWithTicketsFetch());
    }

    @GetMapping("/admin/all")
    public ResponseEntity<List<Booking>> getAdminBookingsRaw() {
        // Native query bypasses active @Where filters to show soft-deleted records (Day 18 & 24)
        return ResponseEntity.ok(bookingRepository.findAllIncludingCancelledNative());
    }
}