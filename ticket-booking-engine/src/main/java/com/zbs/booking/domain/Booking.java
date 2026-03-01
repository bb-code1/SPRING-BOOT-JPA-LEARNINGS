package com.zbs.booking.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "bookings")
@SQLDelete(sql = "UPDATE bookings SET status = 'CANCELLED' WHERE id = ?")
@Where(clause = "status <> 'CANCELLED'")
@Getter
@Setter
public class Booking extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "booking_seq")
    @SequenceGenerator(name = "booking_seq", sequenceName = "bookings_seq", allocationSize = 1)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "status", nullable = false)
    private String status = "PENDING"; // PENDING, CONFIRMED, CANCELLED

    @Version
    private Integer version;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference // Ensures child collection is serialized cleanly
    private List<Ticket> tickets = new ArrayList<>();

    public void addTicket(Ticket ticket) {
        tickets.add(ticket);
        ticket.setBooking(this);
    }
}