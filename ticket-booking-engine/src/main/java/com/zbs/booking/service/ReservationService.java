package com.zbs.booking.service;

import com.zbs.booking.domain.Booking;
import com.zbs.booking.domain.EventInventory;
import com.zbs.booking.domain.Ticket;
import com.zbs.booking.repository.BookingRepository;
import com.zbs.booking.repository.EventInventoryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final EventInventoryRepository inventoryRepository;
    private final BookingRepository bookingRepository;
    private final ApplicationContext applicationContext;

    @Transactional
    public Booking reserveTickets(Long userId, Long eventId, Integer quantity) {
        // Step 1: Acquire physical database-level Pessimistic Lock on the event inventory row
        EventInventory inventory = inventoryRepository.findByEventIdForUpdate(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event inventory not found"));

        // Step 2: Validate available tickets
        if (inventory.getTotalAvailable() < quantity) {
            throw new IllegalArgumentException("Requested tickets exceed available seats (" + inventory.getTotalAvailable() + ")");
        }

        // Step 3: Deduct inventory stock
        inventory.setTotalAvailable(inventory.getTotalAvailable() - quantity);
        inventoryRepository.save(inventory);

        // Step 4: Create booking and associate tickets (demonstrates cascading)
        Booking booking = new Booking();
        booking.setUserId(userId);
        booking.setStatus("PENDING");

        for (int i = 1; i <= quantity; i++) {
            Ticket ticket = new Ticket();
            ticket.setSeatNumber("SEAT-" + eventId + "-" + (inventory.getTotalAvailable() + i));
            ticket.setPrice(150.00); // Fixed ticket price for catalog lookup
            booking.addTicket(ticket);
        }

        Booking savedBooking = bookingRepository.save(booking);

        // Step 5: Trigger internal log (resolves the AOP self-invocation trap using proxy lookup)
        ReservationService self = applicationContext.getBean(ReservationService.class);
        self.saveBookingAuditLog(savedBooking.getId(), "RESERVATION_CREATED");

        return savedBooking;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveBookingAuditLog(Long bookingId, String action) {
        // Runs in an isolated transaction to audit bookings even if parent fails
        Booking booking = bookingRepository.findById(bookingId).orElseThrow();
        booking.setStatus("CONFIRMED");
        bookingRepository.save(booking);
    }
}