package com.zbs.booking.service;

import com.zbs.booking.domain.Booking;
import com.zbs.booking.domain.Event;
import com.zbs.booking.domain.EventInventory;
import com.zbs.booking.domain.User;
import com.zbs.booking.repository.BookingRepository;
import com.zbs.booking.repository.EventInventoryRepository;
import com.zbs.booking.repository.EventRepository;
import com.zbs.booking.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Testcontainers
public class BookingConcurrencyTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
    }

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventInventoryRepository inventoryRepository;

    @Autowired
    private BookingRepository bookingRepository;

    private Long eventId;
    private final List<Long> userIds = new ArrayList<>();

    @BeforeEach
    public void setup() {
        bookingRepository.deleteAll();
        inventoryRepository.deleteAll();
        eventRepository.deleteAll();
        userRepository.deleteAll();

        Event event = new Event();
        event.setName("Concurrency Show");
        event.setLocation("Theater");
        event.setEventDate(LocalDateTime.now().plusDays(2));
        Event savedEvent = eventRepository.save(event);
        this.eventId = savedEvent.getId();

        EventInventory inventory = new EventInventory();
        inventory.setEventId(eventId);
        inventory.setTotalAvailable(5);
        inventoryRepository.save(inventory);

        for (int i = 1; i <= 10; i++) {
            User user = new User();
            user.setName("User-" + i);
            user.setEmail("user" + i + "@test.com");
            User savedUser = userRepository.save(user);
            userIds.add(savedUser.getId());
        }
    }

    @Test
    public void testConcurrentReservationsPessimisticLock() throws InterruptedException {
        int numberOfThreads = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        for (int i = 0; i < numberOfThreads; i++) {
            final Long userId = userIds.get(i);
            executorService.submit(() -> {
                try {
                    latch.await();
                    reservationService.reserveTickets(userId, eventId, 1);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                }
            });
        }

        latch.countDown();
        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.SECONDS);

        assertEquals(5, successCount.get(), "Exactly 5 bookings should succeed");
        assertEquals(5, failureCount.get(), "Exactly 5 bookings should fail due to stock depletion");

        EventInventory inventory = inventoryRepository.findByEventIdForUpdate(eventId).orElseThrow();
        assertEquals(0, inventory.getTotalAvailable(), "Stock should be completely depleted (0)");
    }
}