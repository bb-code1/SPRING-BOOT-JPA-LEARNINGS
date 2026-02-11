package com.zbs.learning.repository;
import com.zbs.learning.domain.Order;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface OrderRepositoryCustom extends JpaRepository<Order, Long> {
    @Query("SELECT o FROM Order o JOIN FETCH o.items")
    List<Order> findAllWithItemsFetch();

    @EntityGraph(attributePaths = {"items"})
    @Query("SELECT o FROM Order o")
    List<Order> findAllWithItemsGraph();
}
