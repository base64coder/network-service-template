package com.dtc.http.controller;

import com.dtc.http.model.Order;
import com.dtc.http.service.OrderService;
import com.dtc.annotations.web.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Map;

/**
 * 订单控制器
 * 提供订单相关的 REST API 接口，包括创建、查询订单等功能
 * 
 * @author Network Service Template
 */
@RestController
@RequestMapping("/api/orders")
@Singleton
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    private final OrderService orderService;

    @Inject
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * 获取所有订单
     * 返回系统中所有订单的列表，以 JSON 格式返回
     */
    @GetMapping
    public Map<String, List<Order>> getOrders() {
        List<Order> orders = orderService.getAllOrders();
        return Map.of("orders", orders);
    }

    /**
     * 创建订单
     */
    @PostMapping
    public Map<String, Object> createOrder(@RequestBody Order order) {
        Order created = orderService.createOrder(order);
        return Map.of(
                "message", "Order created successfully",
                "id", created.getId());
    }

    /**
     * 根据ID获取订单
     */
    @GetMapping("/{id}")
    public Order getOrder(@PathVariable("id") String id) {
        return orderService.getOrderById(id);
    }
}
