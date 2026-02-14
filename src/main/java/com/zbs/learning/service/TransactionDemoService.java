package com.zbs.learning.service;
import com.zbs.learning.domain.Order;
import com.zbs.learning.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
@Service
@RequiredArgsConstructor
public class TransactionDemoService {
    private final OrderRepository orderRepository;
    private final ApplicationContext applicationContext;
    public void processOrderSelfInvocation(Long orderId) {
        saveOrderLog(orderId);
    }
    public void processOrderCorrectProxy(Long orderId) {
        TransactionDemoService self = applicationContext.getBean(TransactionDemoService.class);
        self.saveOrderLog(orderId);
    }
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveOrderLog(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        order.setStatus("PROCESSED_LOGGED");
        orderRepository.save(order);
    }
}