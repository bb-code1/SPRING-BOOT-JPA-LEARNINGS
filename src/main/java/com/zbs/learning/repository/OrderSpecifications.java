package com.zbs.learning.repository;
import com.zbs.learning.domain.Order;
import org.springframework.data.jpa.domain.Specification;

public class OrderSpecifications {
    public static Specification<Order> hasStatus(String status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }
}
