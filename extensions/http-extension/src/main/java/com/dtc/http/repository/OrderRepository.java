package com.dtc.http.repository;

import com.dtc.http.model.Order;
import com.dtc.annotations.ioc.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 订单数据访问层
 * 提供订单数据的存储和查询功能
 * 
 * @author Network Service Template
 */
@Repository
@Singleton
public class OrderRepository {

    private static final Logger log = LoggerFactory.getLogger(OrderRepository.class);

    // 内存存储订单数据
    private final Map<String, Order> orders = new ConcurrentHashMap<>();

    public OrderRepository() {
        // 初始化示例数据
        save(new Order("1", "1", 99.99, "pending"));
        save(new Order("2", "2", 149.99, "completed"));
    }

    /**
     * 查询所有订单
     */
    public List<Order> findAll() {
        return new ArrayList<>(orders.values());
    }

    /**
     * 根据ID查询订单
     */
    public Order findById(String id) {
        Order order = orders.get(id);
        if (order == null) {
            // 如果订单不存在，返回一个默认订单
            return new Order(id, "1", 99.99, "pending");
        }
        return order;
    }

    /**
     * 保存订单
     */
    public Order save(Order order) {
        if (order.getId() == null || order.getId().isEmpty()) {
            order.setId(String.valueOf(System.currentTimeMillis()));
        }
        orders.put(order.getId(), order);
        log.debug("Saved order: {}", order);
        return order;
    }
}
