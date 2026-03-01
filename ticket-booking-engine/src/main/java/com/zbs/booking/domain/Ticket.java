package com.zbs.booking.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "tickets")
@Getter
@Setter
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ticket_seq")
    @SequenceGenerator(name = "ticket_seq", sequenceName = "tickets_seq", allocationSize = 1)
    private Long id;

    @Column(name = "seat_number", nullable = false)
    private String seatNumber;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    @JsonBackReference // Prevents infinite recursion during Jackson JSON serialization
    private Booking booking;
}