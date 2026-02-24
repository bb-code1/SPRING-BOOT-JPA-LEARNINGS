package com.zbs.booking.repository;

import com.zbs.booking.domain.Event;
import org.springframework.data.jpa.domain.Specification;
import java.time.LocalDateTime;

public class EventSpecifications {

    public static Specification<Event> hasName(String name) {
        return (root, query, cb) -> name == null ? null : cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    public static Specification<Event> hasLocation(String location) {
        return (root, query, cb) -> location == null ? null : cb.like(cb.lower(root.get("location")), "%" + location.toLowerCase() + "%");
    }

    public static Specification<Event> occursAfter(LocalDateTime date) {
        return (root, query, cb) -> date == null ? null : cb.greaterThanOrEqualTo(root.get("eventDate"), date);
    }
}