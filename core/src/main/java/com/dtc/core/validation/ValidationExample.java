package com.dtc.core.validation;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * 验证示例
 * 演示如何使用 @NotNull 和 @Nullable 注解进行参数验证
 * 
 * @author Network Service Template
 */
@Singleton
public class ValidationExample {

    private static final Logger log = LoggerFactory.getLogger(ValidationExample.class);

    @Inject
    public ValidationExample() {
        log.info("ValidationExample initialized");
    }

    /**
     * 示例方法：处理用户信息
     * 
     * @param userId   用户ID，不能为null
     * @param username 用户名，不能为null
     * @param email    邮箱，可以为null
     * @return 处理结果
     */
    @NotNull
    public String processUser(@NotNull String userId, @NotNull String username, @Nullable String email) {
        log.info("Processing user: {} - {} (email: {})", userId, username, email);

        StringBuilder result = new StringBuilder();
        result.append("User processed: ").append(username);
        result.append(" (ID: ").append(userId).append(")");

        if (email != null) {
            result.append(" [Email: ").append(email).append("]");
        }

        return result.toString();
    }

    /**
     * 示例方法：创建订单
     * 
     * @param orderId    订单ID，不能为null
     * @param customerId 客户ID，不能为null
     * @param amount     金额，不能为null
     * @param notes      备注，可以为null
     * @return 订单信息
     */
    @NotNull
    public String createOrder(@NotNull String orderId, @NotNull String customerId,
            @NotNull Double amount, @Nullable String notes) {
        log.info("Creating order: {} for customer {} with amount {}", orderId, customerId, amount);

        StringBuilder order = new StringBuilder();
        order.append("Order created: ").append(orderId);
        order.append(" for customer ").append(customerId);
        order.append(" with amount $").append(amount);

        if (notes != null && !notes.trim().isEmpty()) {
            order.append(" (Notes: ").append(notes).append(")");
        }

        return order.toString();
    }

    /**
     * 示例方法：发送消息
     * 
     * @param recipient 接收者，不能为null
     * @param message   消息内容，不能为null
     * @param priority  优先级，可以为null
     * @return 发送结果
     */
    @NotNull
    public String sendMessage(@NotNull String recipient, @NotNull String message, @Nullable Integer priority) {
        log.info("Sending message to {}: {}", recipient, message);

        StringBuilder result = new StringBuilder();
        result.append("Message sent to ").append(recipient);
        result.append(": ").append(message);

        if (priority != null) {
            result.append(" (Priority: ").append(priority).append(")");
        }

        return result.toString();
    }

    /**
     * 示例方法：获取用户信息
     * 
     * @param userId 用户ID，不能为null
     * @return 用户信息，可能为null
     */
    @Nullable
    public String getUserInfo(@NotNull String userId) {
        log.info("Getting user info for: {}", userId);

        // 简单查找用户
        if ("admin".equals(userId)) {
            return "Admin User - Full Access";
        } else if ("user".equals(userId)) {
            return "Regular User - Limited Access";
        } else {
            return null; // 用户不存在
        }
    }

    /**
     * 演示验证功能
     */
    public void demonstrateValidation() {
        log.info("=== 演示注解验证功能 ===");

        try {
            // 正常调用
            String result1 = processUser("123", "John Doe", "john@example.com");
            log.info("Result 1: {}", result1);

            // 正常调用，email为null
            String result2 = processUser("456", "Jane Smith", null);
            log.info("Result 2: {}", result2);

            // 创建订单
            String order = createOrder("ORD-001", "CUST-123", 99.99, "Express delivery");
            log.info("Order: {}", order);

            // 发送消息
            String message = sendMessage("admin", "System maintenance scheduled", 1);
            log.info("Message: {}", message);

            // 获取用户信息
            String userInfo = getUserInfo("admin");
            log.info("User info: {}", userInfo);

            log.info("✅ 所有验证测试通过");

        } catch (Exception e) {
            log.error("❌ 验证测试失败", e);
        }
    }
}
