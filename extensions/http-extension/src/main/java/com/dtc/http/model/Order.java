package com.dtc.http.model;

import java.util.Objects;

/**
 * 订单实体类
 * 
 * @author Network Service Template
 */
public class Order {
    private String id;
    private String userId;
    private Double total;
    private String status;

    public Order() {
    }

    public Order(String id, String userId, Double total, String status) {
        this.id = id;
        this.userId = userId;
        this.total = total;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return Objects.equals(id, order.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Order{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", total=" + total +
                ", status='" + status + '\'' +
                '}';
    }
}
