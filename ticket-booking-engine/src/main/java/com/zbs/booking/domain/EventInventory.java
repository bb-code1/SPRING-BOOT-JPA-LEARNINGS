package com.zbs.booking.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "event_inventories")
@Getter
@Setter
public class EventInventory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "inventory_seq")
    @SequenceGenerator(name = "inventory_seq", sequenceName = "inventories_seq", allocationSize = 1)
    private Long id;

    @Column(name = "event_id", nullable = false, unique = true)
    private Long eventId;

    @Column(name = "total_available", nullable = false)
    private Integer totalAvailable;

    @Version
    private Integer version; // Day 15: Optimistic locking version
}