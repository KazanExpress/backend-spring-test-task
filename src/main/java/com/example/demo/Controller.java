package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/order")
@CrossOrigin("*")
@RestController
public class Controller {

    private final OrderService orderService;

    @Autowired
    public Controller(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Создание заказа
     *
     * @param productIds Список id продуктов в заказе
     */
    @PostMapping("/create")
    public void createOrder(@RequestBody List<Long> productIds) {
        orderService.createOrder(productIds);
    }

    /**
     * Оформление возврата на один из продуктов в заказе
     *
     * @param orderId Id заказа
     * @param returnedProductId Id продукта, на который оформляется возврат
     * @return Id продуктов в заказе, на которые еще не оформлен возврат
     */
    @PostMapping("/{orderId}/return")
    public List<Long> returnOrder(@PathVariable Long orderId,
                                  @RequestBody Long returnedProductId) {
        return orderService.returnOrder(orderId, returnedProductId);
    }

    /**
     * Выдача заказа получателю
     *
     * @param orderId Id заказа
     */
    @PostMapping("/{orderId}/return")
    public void issueOrder(@PathVariable Long orderId) {
        orderService.issueOrder(orderId);
    }

    /**
     * Получение всех существующих заказов
     *
     * @return Список заказов с продуктами(невозвращенными)
     */
    @GetMapping("/all")
    public List<OrderDto> getAllOrders() {
        return orderService.getAllOrders();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handle(IllegalArgumentException e) {
        return new ResponseEntity<>(e.getMessage(), new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }
}
