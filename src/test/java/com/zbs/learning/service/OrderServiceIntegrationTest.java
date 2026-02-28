package com.zbs.learning.service;
import com.zbs.learning.domain.Order;
import com.zbs.learning.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
@Transactional
public class OrderServiceIntegrationTest {
    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderRepository orderRepository;
    @Test
    public void testCreateOrderAndCascadeItems() {
        Order order = new Order();
        order.setStatus("PENDING");
        Order savedOrder = orderService.createOrder(order);
        assertNotNull(savedOrder.getId());
        assertEquals("PENDING", savedOrder.getStatus());
        assertNotNull(savedOrder.getCreatedDate());
    }
}