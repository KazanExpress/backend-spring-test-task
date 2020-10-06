package com.example.demo;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final EntityManager entityManager;

    @Autowired
    public OrderService(OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        EntityManager entityManager) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.entityManager = entityManager;
    }

    public void createOrder(List<Long> productIds) {
        if (ObjectUtils.isEmpty(productIds)) {
            throw new IllegalArgumentException("Product ids can not be empty");
        }
        var orderItems = productIds.stream()
                .map(id -> OrderItemEntity.builder()
                        .productId(id)
                        .build())
                .collect(Collectors.toSet());
        createOrderFromItems(orderItems);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void createOrderFromItems(Set<OrderItemEntity> orderItems) {
        var order = OrderEntity.builder()
                .orderItems(orderItems)
                .build();
        entityManager.persist(order);
        publishOrderCreation(order);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public List<Long> returnOrder(Long orderId,
                                  Long returnedProductId) {
        if (returnedProductId == null) {
            throw new IllegalArgumentException("Product id can not be null");
        }
        var order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        if (order.isIssued()) {
            throw new IllegalArgumentException("Order was already issued");
        }
        for (var orderItem : order.getOrderItems()) {
            if (orderItem.getProductId().equals(returnedProductId)) {
                if (orderItem.isReturned()) {
                    throw new IllegalArgumentException("Product already returned");
                }
                orderItem.setReturned(true);
                publishOrderReturn(orderId, returnedProductId);
                return orderItemRepository.getNotReturnedProductIds(orderId);
            }
        }
        throw new IllegalArgumentException("Product not found in order");
    }

    @Transactional
    public void issueOrder(Long orderId) {
        var order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        if (order.isIssued()) {
            throw new IllegalArgumentException("Order was already issued");
        }
        order.setIssued(true);
        entityManager.persist(order);
    }

    public List<OrderDto> getAllOrders() {
        return orderRepository.findAll().parallelStream()
                .map(order -> {
                    var productIds = order.getOrderItems().parallelStream()
                            .filter(orderItem -> !orderItem.isReturned())
                            .map(OrderItemEntity::getProductId)
                            .collect(Collectors.toList());
                    return new OrderDto(order.getId(), productIds);
                })
                .collect(Collectors.toList());
    }

    private void publishOrderCreation(OrderEntity order) {
        //Внутри метода происходит отправка данных о создания заказа в платежную систему
    }

    private void publishOrderReturn(long orderId,
                                    long productId) {
        //Внутри метода происходит запрос на возврат средств по товару в заказе
    }
}
