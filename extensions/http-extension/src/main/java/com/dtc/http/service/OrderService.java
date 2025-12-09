package com.dtc.http.service;

import com.dtc.http.model.Order;
import com.dtc.http.repository.OrderRepository;
import com.dtc.annotations.ioc.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

/**
 * 订单服务
 * 处理订单相关的业务逻辑
 * 
 * @author Network Service Template
 */
@Service
@Singleton
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;

    @Inject
    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    /**
     * 获取所有订单
     */
    public List<Order> getAllOrders() {
        log.debug("Getting all orders");
        return orderRepository.findAll();
    }

    /**
     * 根据ID获取订单
     */
    public Order getOrderById(String id) {
        log.debug("Getting order with id: {}", id);
        return orderRepository.findById(id);
    }

    /**
     * 创建订单
     */
    public Order createOrder(Order order) {
        log.info("Creating order: {}", order);
        return orderRepository.save(order);
    }
}
