package com.zbs.booking.controller;

import com.zbs.booking.domain.Event;
import com.zbs.booking.repository.EventRepository;
import com.zbs.booking.repository.EventSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class EventSearchController {

    private final EventRepository eventRepository;

    @GetMapping("/search")
    public ResponseEntity<Page<Event>> searchEvents(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String afterDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        LocalDateTime date = (afterDate != null) ? LocalDateTime.parse(afterDate) : null;
        
        // Combines dynamic specifications criteria (Day 17)
        Specification<Event> spec = Specification.where(EventSpecifications.hasName(name))
                .and(EventSpecifications.hasLocation(location))
                .and(EventSpecifications.occursAfter(date));

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        return ResponseEntity.ok(eventRepository.findAll(spec, pageable));
    }

    @GetMapping("/search/keyset")
    public ResponseEntity<List<Event>> searchEventsKeyset(
            @RequestParam(required = false) Long lastId,
            @RequestParam(defaultValue = "10") int size) {
        
        // Simulates high-performance keyset range seeks (Day 19)
        Pageable pageable = PageRequest.of(0, size, Sort.by("id").ascending());
        Specification<Event> spec = (root, query, cb) -> 
            (lastId == null) ? null : cb.greaterThan(root.get("id"), lastId);
            
        return ResponseEntity.ok(eventRepository.findAll(spec, pageable).getContent());
    }
}