package com.zbs.learning.service;

import com.zbs.learning.domain.Order;
import com.zbs.learning.repository.OrderRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    @Transactional
    public Order createOrder(Order order) {
        // CascadeType.ALL ensures saving the order automatically saves its children
        return orderRepository.save(order);
    }

    @Transactional
    public void removeOrderItem(Long orderId, Long itemId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new EntityNotFoundException("Order not found"));
        
        // orphanRemoval = true automatically schedules a database DELETE for the removed item
        order.getItems().removeIf(item -> item.getId().equals(itemId));
    }

    @Transactional
    public void deleteOrder(Long orderId) {
        // CascadeType.ALL/REMOVE automatically deletes child items in the database
        orderRepository.deleteById(orderId);
    }
}
