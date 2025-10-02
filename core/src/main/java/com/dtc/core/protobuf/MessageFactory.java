package com.dtc.core.protobuf;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import com.dtc.core.protobuf.NetworkMessageProtos.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.Map;
import java.util.UUID;

/**
 * Protobuf 消息工厂 提供各种类型消息的创建方法
 * 
 * @author Network Service Template
 */
@Singleton
public class MessageFactory {

    private static final Logger log = LoggerFactory.getLogger(MessageFactory.class);

    /**
     * 创建基础网络消息
     */
    @NotNull
    public NetworkMessage createNetworkMessage(@NotNull MessageType type, @NotNull String clientId,
            @Nullable String serverId, @NotNull Object payload) {
        String messageId = UUID.randomUUID().toString();
        long timestamp = System.currentTimeMillis();

        NetworkMessage.Builder builder = NetworkMessage.newBuilder().setMessageId(messageId).setType(type)
                .setTimestamp(timestamp).setClientId(clientId);

        if (serverId != null) {
            builder.setServerId(serverId);
        }

        // 设置载荷
        switch (type) {
        case HEARTBEAT:
            if (payload instanceof HeartbeatMessage) {
                builder.setHeartbeat((HeartbeatMessage) payload);
            }
            break;
        case DATA:
            if (payload instanceof DataMessage) {
                builder.setData((DataMessage) payload);
            }
            break;
        case ACK:
            if (payload instanceof AckMessage) {
                builder.setAck((AckMessage) payload);
            }
            break;
        case ERROR:
            if (payload instanceof ErrorMessage) {
                builder.setError((ErrorMessage) payload);
            }
            break;
        case CLOSE:
            if (payload instanceof CloseMessage) {
                builder.setClose((CloseMessage) payload);
            }
            break;
        default:
            log.warn("Unknown message type: {}", type);
        }

        return builder.build();
    }

    /**
     * 创建心跳消息
     */
    @NotNull
    public NetworkMessage createHeartbeatMessage(@NotNull String clientId, @Nullable String serverId,
            @Nullable Map<String, String> metadata) {
        HeartbeatMessage heartbeat = HeartbeatMessage.newBuilder().setClientId(clientId)
                .setLastHeartbeat(System.currentTimeMillis()).putAllMetadata(metadata != null ? metadata : Map.of())
                .build();

        return createNetworkMessage(MessageType.HEARTBEAT, clientId, serverId, heartbeat);
    }

    /**
     * 创建数据消息
     */
    @NotNull
    public NetworkMessage createDataMessage(@NotNull String clientId, @Nullable String serverId, @NotNull String topic,
            @NotNull byte[] content, @Nullable String contentType, @Nullable Map<String, String> headers,
            int priority) {
        DataMessage data = DataMessage.newBuilder().setTopic(topic)
                .setContent(com.google.protobuf.ByteString.copyFrom(content))
                .setContentType(contentType != null ? contentType : "application/octet-stream")
                .putAllHeaders(headers != null ? headers : Map.of()).setPriority(priority).build();

        return createNetworkMessage(MessageType.DATA, clientId, serverId, data);
    }

    /**
     * 创建文本数据消息
     */
    @NotNull
    public NetworkMessage createTextDataMessage(@NotNull String clientId, @Nullable String serverId,
            @NotNull String topic, @NotNull String content, @Nullable Map<String, String> headers, int priority) {
        return createDataMessage(clientId, serverId, topic, content.getBytes(), "text/plain", headers, priority);
    }

    /**
     * 创建 JSON 数据消息
     */
    @NotNull
    public NetworkMessage createJsonDataMessage(@NotNull String clientId, @Nullable String serverId,
            @NotNull String topic, @NotNull String jsonContent, @Nullable Map<String, String> headers, int priority) {
        return createDataMessage(clientId, serverId, topic, jsonContent.getBytes(), "application/json", headers,
                priority);
    }

    /**
     * 创建确认消息
     */
    @NotNull
    public NetworkMessage createAckMessage(@NotNull String clientId, @Nullable String serverId,
            @NotNull String originalMessageId, boolean success, @Nullable String errorMessage, long processingTime) {
        AckMessage ack = AckMessage.newBuilder().setOriginalMessageId(originalMessageId).setSuccess(success)
                .setErrorMessage(errorMessage != null ? errorMessage : "").setProcessingTime(processingTime).build();

        return createNetworkMessage(MessageType.ACK, clientId, serverId, ack);
    }

    /**
     * 创建错误消息
     */
    @NotNull
    public NetworkMessage createErrorMessage(@NotNull String clientId, @Nullable String serverId, int errorCode,
            @NotNull String errorMessage, @Nullable String details, @Nullable String stackTrace) {
        ErrorMessage error = ErrorMessage.newBuilder().setErrorCode(errorCode).setErrorMessage(errorMessage)
                .setDetails(details != null ? details : "").setStackTrace(stackTrace != null ? stackTrace : "").build();

        return createNetworkMessage(MessageType.ERROR, clientId, serverId, error);
    }

    /**
     * 创建关闭消息
     */
    @NotNull
    public NetworkMessage createCloseMessage(@NotNull String clientId, @Nullable String serverId, int closeCode,
            @NotNull String closeReason, boolean graceful) {
        CloseMessage close = CloseMessage.newBuilder().setCloseCode(closeCode).setCloseReason(closeReason)
                .setGraceful(graceful).build();

        return createNetworkMessage(MessageType.CLOSE, clientId, serverId, close);
    }

    /**
     * 创建用户消息
     */
    @NotNull
    public UserMessage createUserMessage(long userId, @NotNull String username, @NotNull String email,
            @NotNull String[] roles, @Nullable Map<String, String> attributes) {
        return UserMessage.newBuilder().setUserId(userId).setUsername(username).setEmail(email)
                .addAllRoles(java.util.Arrays.asList(roles))
                .putAllAttributes(attributes != null ? attributes : Map.of()).setCreatedAt(System.currentTimeMillis())
                .setUpdatedAt(System.currentTimeMillis()).build();
    }

    /**
     * 创建订单消息
     */
    @NotNull
    public OrderMessage createOrderMessage(long orderId, long userId, @NotNull OrderItem[] items, double totalAmount,
            @NotNull String status) {
        return OrderMessage.newBuilder().setOrderId(orderId).setUserId(userId)
                .addAllItems(java.util.Arrays.asList(items)).setTotalAmount(totalAmount).setStatus(status)
                .setCreatedAt(System.currentTimeMillis()).setUpdatedAt(System.currentTimeMillis()).build();
    }

    /**
     * 创建订单项
     */
    @NotNull
    public OrderItem createOrderItem(long productId, @NotNull String productName, int quantity, double unitPrice) {
        return OrderItem.newBuilder().setProductId(productId).setProductName(productName).setQuantity(quantity)
                .setUnitPrice(unitPrice).setTotalPrice(quantity * unitPrice).build();
    }

    /**
     * 创建产品消息
     */
    @NotNull
    public ProductMessage createProductMessage(long productId, @NotNull String name, @NotNull String description,
            double price, @NotNull String category, @NotNull String[] tags, @Nullable Map<String, String> properties) {
        return ProductMessage.newBuilder().setProductId(productId).setName(name).setDescription(description)
                .setPrice(price).setCategory(category).addAllTags(java.util.Arrays.asList(tags))
                .putAllProperties(properties != null ? properties : Map.of()).setCreatedAt(System.currentTimeMillis())
                .setUpdatedAt(System.currentTimeMillis()).build();
    }

    /**
     * 解析网络消息
     */
    @NotNull
    public NetworkMessage parseNetworkMessage(@NotNull byte[] data) throws Exception {
        return NetworkMessage.parseFrom(data);
    }

    /**
     * 序列化网络消息
     */
    @NotNull
    public byte[] serializeNetworkMessage(@NotNull NetworkMessage message) {
        return message.toByteArray();
    }

    /**
     * 获取消息类型
     */
    @NotNull
    public MessageType getMessageType(@NotNull NetworkMessage message) {
        return message.getType();
    }

    /**
     * 检查消息是否为特定类型
     */
    public boolean isMessageType(@NotNull NetworkMessage message, @NotNull MessageType type) {
        return message.getType() == type;
    }

    /**
     * 获取消息载荷
     */
    @Nullable
    public Object getMessagePayload(@NotNull NetworkMessage message) {
        switch (message.getType()) {
        case HEARTBEAT:
            return message.hasHeartbeat() ? message.getHeartbeat() : null;
        case DATA:
            return message.hasData() ? message.getData() : null;
        case ACK:
            return message.hasAck() ? message.getAck() : null;
        case ERROR:
            return message.hasError() ? message.getError() : null;
        case CLOSE:
            return message.hasClose() ? message.getClose() : null;
        default:
            return null;
        }
    }

    /**
     * 创建消息构建器
     */
    @NotNull
    public NetworkMessage.Builder createNetworkMessageBuilder(@NotNull String clientId, @Nullable String serverId) {
        return NetworkMessage.newBuilder().setMessageId(UUID.randomUUID().toString())
                .setTimestamp(System.currentTimeMillis()).setClientId(clientId)
                .setServerId(serverId != null ? serverId : "");
    }

    /**
     * 复制消息并修改类型
     */
    @NotNull
    public NetworkMessage copyMessageWithNewType(@NotNull NetworkMessage original, @NotNull MessageType newType) {
        NetworkMessage.Builder builder = original.toBuilder().setType(newType)
                .setMessageId(UUID.randomUUID().toString()).setTimestamp(System.currentTimeMillis());

        return builder.build();
    }
}
