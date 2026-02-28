package com.zbs.learning.controller;

import com.zbs.learning.domain.Order;
import com.zbs.learning.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody Order order) {
        return ResponseEntity.ok(orderService.createOrder(order));
    }

    @DeleteMapping("/{orderId}/items/{itemId}")
    public ResponseEntity<Void> removeOrderItem(@PathVariable Long orderId, @PathVariable Long itemId) {
        orderService.removeOrderItem(orderId, itemId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long orderId) {
        orderService.deleteOrder(orderId);
        return ResponseEntity.ok().build();
    }
}
