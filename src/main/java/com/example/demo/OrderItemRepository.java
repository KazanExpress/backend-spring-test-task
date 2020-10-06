package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderEntity, Long> {
    @Query(nativeQuery = true, value = "" +
            "SELECT product_id FROM order_item_entity " +
            "WHERE order_id = ?1 " +
            "AND returned = 'false'")
    List<Long> getNotReturnedProductIds(long orderId);
}
