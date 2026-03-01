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
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final EventInventoryRepository inventoryRepository;
    private final BookingRepository bookingRepository;
    private final ApplicationContext applicationContext;

    @Transactional
    public Booking reserveTickets(Long userId, Long eventId, Integer quantity) {
        EventInventory inventory = inventoryRepository.findByEventIdForUpdate(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event inventory not found"));

        if (inventory.getTotalAvailable() < quantity) {
            throw new IllegalArgumentException("Requested tickets exceed available seats (" + inventory.getTotalAvailable() + ")");
        }

        inventory.setTotalAvailable(inventory.getTotalAvailable() - quantity);
        inventoryRepository.save(inventory);

        Booking booking = new Booking();
        booking.setUserId(userId);
        booking.setStatus("PENDING");

        for (int i = 1; i <= quantity; i++) {
            Ticket ticket = new Ticket();
            ticket.setSeatNumber("SEAT-" + eventId + "-" + (inventory.getTotalAvailable() + i));
            ticket.setPrice(new BigDecimal("150.00"));
            booking.addTicket(ticket);
        }

        Booking savedBooking = bookingRepository.save(booking);

        ReservationService self = applicationContext.getBean(ReservationService.class);
        self.saveBookingAuditLog(savedBooking.getId(), "RESERVATION_CREATED");

        return savedBooking;
    }

    @Transactional(propagation = Propagation.REQUIRED) // Fixed READ_COMMITTED isolation desync
    public void saveBookingAuditLog(Long bookingId, String action) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow();
        booking.setStatus("CONFIRMED");
        bookingRepository.save(booking);
    }
}