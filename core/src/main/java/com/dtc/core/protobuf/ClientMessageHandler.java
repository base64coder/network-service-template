package com.dtc.core.protobuf;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import com.dtc.core.protobuf.NetworkMessageProtos.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 客户端消息处理器 处理客户端发送和接收的 Protobuf 消息
 * 
 * @author Network Service Template
 */
@Singleton
public class ClientMessageHandler {

    private static final Logger log = LoggerFactory.getLogger(ClientMessageHandler.class);

    private final MessageFactory messageFactory;
    private final AtomicLong messageCounter = new AtomicLong(0);
    private final Map<String, NetworkMessage> pendingMessages = new ConcurrentHashMap<>();

    @Inject
    public ClientMessageHandler(@NotNull MessageFactory messageFactory) {
        this.messageFactory = messageFactory;
    }

    /**
     * 发送心跳消息
     */
    @NotNull
    public NetworkMessage sendHeartbeat(@NotNull String clientId, @Nullable String serverId,
            @Nullable Map<String, String> metadata) {
        NetworkMessage message = messageFactory.createHeartbeatMessage(clientId, serverId, metadata);
        log.debug("Sending heartbeat message: {}", message.getMessageId());
        return message;
    }

    /**
     * 发送数据消息
     */
    @NotNull
    public NetworkMessage sendData(@NotNull String clientId, @Nullable String serverId, @NotNull String topic,
            @NotNull byte[] content, @Nullable String contentType, @Nullable Map<String, String> headers,
            int priority) {
        NetworkMessage message = messageFactory.createDataMessage(clientId, serverId, topic, content, contentType,
                headers, priority);
        log.debug("Sending data message: {} to topic: {}", message.getMessageId(), topic);
        return message;
    }

    /**
     * 发送文本数据消息
     */
    @NotNull
    public NetworkMessage sendTextData(@NotNull String clientId, @Nullable String serverId, @NotNull String topic,
            @NotNull String content, @Nullable Map<String, String> headers, int priority) {
        NetworkMessage message = messageFactory.createTextDataMessage(clientId, serverId, topic, content, headers,
                priority);
        log.debug("Sending text data message: {} to topic: {}", message.getMessageId(), topic);
        return message;
    }

    /**
     * 发送 JSON 数据消息
     */
    @NotNull
    public NetworkMessage sendJsonData(@NotNull String clientId, @Nullable String serverId, @NotNull String topic,
            @NotNull String jsonContent, @Nullable Map<String, String> headers, int priority) {
        NetworkMessage message = messageFactory.createJsonDataMessage(clientId, serverId, topic, jsonContent, headers,
                priority);
        log.debug("Sending JSON data message: {} to topic: {}", message.getMessageId(), topic);
        return message;
    }

    /**
     * 发送用户消息
     */
    @NotNull
    public NetworkMessage sendUserMessage(@NotNull String clientId, @Nullable String serverId, long userId,
            @NotNull String username, @NotNull String email, @NotNull String[] roles,
            @Nullable Map<String, String> attributes) {
        UserMessage userMessage = messageFactory.createUserMessage(userId, username, email, roles, attributes);

        // 将用户消息包装在数据消息中
        String jsonContent = convertUserMessageToJson(userMessage);
        return sendJsonData(clientId, serverId, "user", jsonContent, null, 1);
    }

    /**
     * 发送订单消息
     */
    @NotNull
    public NetworkMessage sendOrderMessage(@NotNull String clientId, @Nullable String serverId, long orderId,
            long userId, @NotNull OrderItem[] items, double totalAmount, @NotNull String status) {
        OrderMessage orderMessage = messageFactory.createOrderMessage(orderId, userId, items, totalAmount, status);

        // 将订单消息包装在数据消息中
        String jsonContent = convertOrderMessageToJson(orderMessage);
        return sendJsonData(clientId, serverId, "order", jsonContent, null, 2);
    }

    /**
     * 发送产品消息
     */
    @NotNull
    public NetworkMessage sendProductMessage(@NotNull String clientId, @Nullable String serverId, long productId,
            @NotNull String name, @NotNull String description, double price, @NotNull String category,
            @NotNull String[] tags, @Nullable Map<String, String> properties) {
        ProductMessage productMessage = messageFactory.createProductMessage(productId, name, description, price,
                category, tags, properties);

        // 将产品消息包装在数据消息中
        String jsonContent = convertProductMessageToJson(productMessage);
        return sendJsonData(clientId, serverId, "product", jsonContent, null, 1);
    }

    /**
     * 处理接收到的消息
     */
    public void handleReceivedMessage(@NotNull NetworkMessage message) {
        log.debug("Handling received message: {} of type: {}", message.getMessageId(), message.getType());

        switch (message.getType()) {
        case HEARTBEAT:
            handleHeartbeatMessage(message);
            break;
        case DATA:
            handleDataMessage(message);
            break;
        case ACK:
            handleAckMessage(message);
            break;
        case ERROR:
            handleErrorMessage(message);
            break;
        case CLOSE:
            handleCloseMessage(message);
            break;
        default:
            log.warn("Unknown message type received: {}", message.getType());
        }

        messageCounter.incrementAndGet();
    }

    /**
     * 处理心跳消息
     */
    private void handleHeartbeatMessage(@NotNull NetworkMessage message) {
        if (message.hasHeartbeat()) {
            HeartbeatMessage heartbeat = message.getHeartbeat();
            log.debug("Received heartbeat from client: {} at {}", heartbeat.getClientId(),
                    heartbeat.getLastHeartbeat());
        }
    }

    /**
     * 处理数据消息
     */
    private void handleDataMessage(@NotNull NetworkMessage message) {
        if (message.hasData()) {
            DataMessage data = message.getData();
            log.debug("Received data message on topic: {} with content type: {}", data.getTopic(),
                    data.getContentType());

            // 根据内容类型处理数据
            switch (data.getContentType()) {
            case "application/json":
                handleJsonData(data);
                break;
            case "text/plain":
                handleTextData(data);
                break;
            default:
                handleBinaryData(data);
            }
        }
    }

    /**
     * 处理 JSON 数据
     */
    private void handleJsonData(@NotNull DataMessage data) {
        String jsonContent = data.getContent().toStringUtf8();
        log.debug("Received JSON data: {}", jsonContent);

        // 根据主题处理不同的 JSON 数据
        switch (data.getTopic()) {
        case "user":
            handleUserJsonData(jsonContent);
            break;
        case "order":
            handleOrderJsonData(jsonContent);
            break;
        case "product":
            handleProductJsonData(jsonContent);
            break;
        default:
            log.debug("Unknown topic for JSON data: {}", data.getTopic());
        }
    }

    /**
     * 处理文本数据
     */
    private void handleTextData(@NotNull DataMessage data) {
        String textContent = data.getContent().toStringUtf8();
        log.debug("Received text data: {}", textContent);
    }

    /**
     * 处理二进制数据
     */
    private void handleBinaryData(@NotNull DataMessage data) {
        byte[] binaryContent = data.getContent().toByteArray();
        log.debug("Received binary data: {} bytes", binaryContent.length);
    }

    /**
     * 处理用户 JSON 数据
     */
    private void handleUserJsonData(@NotNull String jsonContent) {
        log.debug("Processing user JSON data: {}", jsonContent);
        // 这里可以添加具体的用户数据处理逻辑
    }

    /**
     * 处理订单 JSON 数据
     */
    private void handleOrderJsonData(@NotNull String jsonContent) {
        log.debug("Processing order JSON data: {}", jsonContent);
        // 这里可以添加具体的订单数据处理逻辑
    }

    /**
     * 处理产品 JSON 数据
     */
    private void handleProductJsonData(@NotNull String jsonContent) {
        log.debug("Processing product JSON data: {}", jsonContent);
        // 这里可以添加具体的产品数据处理逻辑
    }

    /**
     * 处理确认消息
     */
    private void handleAckMessage(@NotNull NetworkMessage message) {
        if (message.hasAck()) {
            AckMessage ack = message.getAck();
            log.debug("Received ACK for message: {} success: {}", ack.getOriginalMessageId(), ack.getSuccess());

            // 从待处理消息中移除
            pendingMessages.remove(ack.getOriginalMessageId());
        }
    }

    /**
     * 处理错误消息
     */
    private void handleErrorMessage(@NotNull NetworkMessage message) {
        if (message.hasError()) {
            ErrorMessage error = message.getError();
            log.error("Received error message: {} - {}", error.getErrorCode(), error.getErrorMessage());
        }
    }

    /**
     * 处理关闭消息
     */
    private void handleCloseMessage(@NotNull NetworkMessage message) {
        if (message.hasClose()) {
            CloseMessage close = message.getClose();
            log.info("Received close message: {} - {}", close.getCloseCode(), close.getCloseReason());
        }
    }

    /**
     * 等待确认消息
     */
    public boolean waitForAck(@NotNull String messageId, long timeoutMs) {
        long startTime = System.currentTimeMillis();

        while (System.currentTimeMillis() - startTime < timeoutMs) {
            if (!pendingMessages.containsKey(messageId)) {
                return true; // 收到确认
            }
            try {
                Thread.sleep(10); // 短暂等待
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }

        return false; // 超时
    }

    /**
     * 添加待处理消息
     */
    public void addPendingMessage(@NotNull NetworkMessage message) {
        pendingMessages.put(message.getMessageId(), message);
    }

    /**
     * 获取统计信息
     */
    @NotNull
    public ClientStats getStats() {
        return new ClientStats(messageCounter.get(), pendingMessages.size());
    }

    // 辅助方法

    private String convertUserMessageToJson(@NotNull UserMessage userMessage) {
        // 简化的 JSON 转换，实际项目中可以使用 Jackson 等库
        return String.format("{\"userId\":%d,\"username\":\"%s\",\"email\":\"%s\",\"roles\":%s,\"attributes\":%s}",
                userMessage.getUserId(), userMessage.getUsername(), userMessage.getEmail(),
                java.util.Arrays.toString(userMessage.getRolesList().toArray()),
                userMessage.getAttributesMap().toString());
    }

    private String convertOrderMessageToJson(@NotNull OrderMessage orderMessage) {
        // 简化的 JSON 转换
        return String.format("{\"orderId\":%d,\"userId\":%d,\"totalAmount\":%.2f,\"status\":\"%s\"}",
                orderMessage.getOrderId(), orderMessage.getUserId(), orderMessage.getTotalAmount(),
                orderMessage.getStatus());
    }

    private String convertProductMessageToJson(@NotNull ProductMessage productMessage) {
        // 简化的 JSON 转换
        return String.format(
                "{\"productId\":%d,\"name\":\"%s\",\"description\":\"%s\",\"price\":%.2f,\"category\":\"%s\"}",
                productMessage.getProductId(), productMessage.getName(), productMessage.getDescription(),
                productMessage.getPrice(), productMessage.getCategory());
    }

    /**
     * 客户端统计信息
     */
    public static class ClientStats {
        private final long totalMessages;
        private final int pendingMessages;

        public ClientStats(long totalMessages, int pendingMessages) {
            this.totalMessages = totalMessages;
            this.pendingMessages = pendingMessages;
        }

        public long getTotalMessages() {
            return totalMessages;
        }

        public int getPendingMessages() {
            return pendingMessages;
        }

        @Override
        public String toString() {
            return String.format("ClientStats{totalMessages=%d, pendingMessages=%d}", totalMessages, pendingMessages);
        }
    }
}
