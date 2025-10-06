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
 * 服务器端消息处理器 处理服务器接收和发送的 Protobuf 消息
 * 
 * @author Network Service Template
 */
@Singleton
public class ServerMessageHandler {

    private static final Logger log = LoggerFactory.getLogger(ServerMessageHandler.class);

    private final MessageFactory messageFactory;
    private final AtomicLong receivedCount = new AtomicLong(0);
    private final AtomicLong sentCount = new AtomicLong(0);
    private final AtomicLong errorCount = new AtomicLong(0);
    private final Map<String, NetworkMessage> clientSessions = new ConcurrentHashMap<>();

    @Inject
    public ServerMessageHandler(@NotNull MessageFactory messageFactory) {
        this.messageFactory = messageFactory;
    }

    /**
     * 处理客户端连接
     */
    public void handleClientConnect(@NotNull String clientId, @NotNull String serverId) {
        log.info("Client connected: {} to server: {}", clientId, serverId);

        // 发送欢迎消息
        NetworkMessage welcomeMessage = createWelcomeMessage(clientId, serverId);
        sendMessage(welcomeMessage);
    }

    /**
     * 处理客户端断开连接
     */
    public void handleClientDisconnect(@NotNull String clientId, @NotNull String serverId) {
        log.info("Client disconnected: {} from server: {}", clientId, serverId);
        clientSessions.remove(clientId);
    }

    /**
     * 处理接收到的消息
     */
    @NotNull
    public NetworkMessage handleReceivedMessage(
            @NotNull NetworkMessage message) {
        log.debug("Handling received message: {} of type: {}", message.getMessageId(), message.getType());

        receivedCount.incrementAndGet();

        switch (message.getType()) {
            case HEARTBEAT:
                return handleHeartbeatMessage(message);
            case DATA:
                return handleDataMessage(message);
            case ACK:
                return handleAckMessage(message);
            case ERROR:
                return handleErrorMessage(message);
            case CLOSE:
                return handleCloseMessage(message);
            default:
                log.warn("Unknown message type received: {}", message.getType());
                return createErrorMessage(message.getClientId(), message.getServerId(), 400, "Unknown message type",
                        null, null);
        }
    }

    /**
     * 处理心跳消息
     */
    @NotNull
    private NetworkMessage handleHeartbeatMessage(
            @NotNull NetworkMessage message) {
        if (message.hasHeartbeat()) {
            HeartbeatMessage heartbeat = message.getHeartbeat();
            log.debug("Received heartbeat from client: {} at {}", message.getClientId(),
                    heartbeat.getLastHeartbeat());

            // 更新客户端会话
            clientSessions.put(message.getClientId(), message);

            // 发送心跳确认
            return createHeartbeatAck(message.getClientId(), message.getServerId());
        }

        return createErrorMessage(message.getClientId(), message.getServerId(), 400, "Invalid heartbeat message", null,
                null);
    }

    /**
     * 处理数据消息
     */
    @NotNull
    private NetworkMessage handleDataMessage(
            @NotNull NetworkMessage message) {
        if (message.hasData()) {
            DataMessage data = message.getData();
            log.debug("Received data message on topic: {} from client: {}", data.getTopic(), message.getClientId());

            try {
                // 处理数据消息
                boolean success = processDataMessage(data, message.getClientId());

                if (success) {
                    // 发送处理成功确认
                    return createAckMessage(message.getClientId(), message.getServerId(), message.getMessageId(), true,
                            null, 0);
                } else {
                    // 发送处理失败确认
                    return createAckMessage(message.getClientId(), message.getServerId(), message.getMessageId(), false,
                            "Processing failed", 0);
                }
            } catch (Exception e) {
                log.error("Error processing data message", e);
                return createErrorMessage(message.getClientId(), message.getServerId(), 500, "Internal server error",
                        e.getMessage(), java.util.Arrays.toString(e.getStackTrace()));
            }
        }

        return createErrorMessage(message.getClientId(), message.getServerId(), 400, "Invalid data message", null,
                null);
    }

    /**
     * 处理数据消息的具体逻辑
     */
    private boolean processDataMessage(@NotNull DataMessage data, @NotNull String clientId) {
        try {
            switch (data.getTopic()) {
                case "user":
                    return processUserData(data, clientId);
                case "order":
                    return processOrderData(data, clientId);
                case "product":
                    return processProductData(data, clientId);
                default:
                    log.warn("Unknown topic: {}", data.getTopic());
                    return false;
            }
        } catch (Exception e) {
            log.error("Error processing data for topic: {}", data.getTopic(), e);
            return false;
        }
    }

    /**
     * 处理用户数据
     */
    private boolean processUserData(@NotNull DataMessage data, @NotNull String clientId) {
        try {
            String jsonContent = data.getContent().toStringUtf8();
            log.debug("Processing user data: {}", jsonContent);

            // 这里可以添加具体的用户数据处理逻辑
            // 例如：保存到数据库、验证数据等

            return true;
        } catch (Exception e) {
            log.error("Error processing user data", e);
            return false;
        }
    }

    /**
     * 处理订单数据
     */
    private boolean processOrderData(@NotNull DataMessage data, @NotNull String clientId) {
        try {
            String jsonContent = data.getContent().toStringUtf8();
            log.debug("Processing order data: {}", jsonContent);

            // 这里可以添加具体的订单数据处理逻辑
            // 例如：创建订单、更新库存等

            return true;
        } catch (Exception e) {
            log.error("Error processing order data", e);
            return false;
        }
    }

    /**
     * 处理产品数据
     */
    private boolean processProductData(@NotNull DataMessage data, @NotNull String clientId) {
        try {
            String jsonContent = data.getContent().toStringUtf8();
            log.debug("Processing product data: {}", jsonContent);

            // 这里可以添加具体的产品数据处理逻辑
            // 例如：更新产品信息、同步到搜索引擎等

            return true;
        } catch (Exception e) {
            log.error("Error processing product data", e);
            return false;
        }
    }

    /**
     * 处理确认消息
     */
    @Nullable
    private NetworkMessage handleAckMessage(@NotNull NetworkMessage message) {
        if (message.hasAck()) {
            AckMessage ack = message.getAck();
            log.debug("Received ACK for message: {} success: {}", ack.getOriginalMessageId(), ack.getSuccess());
        }

        // ACK 消息通常不需要回复
        return null;
    }

    /**
     * 处理错误消息
     */
    @Nullable
    private NetworkMessage handleErrorMessage(
            @NotNull NetworkMessage message) {
        if (message.hasError()) {
            ErrorMessage error = message.getError();
            log.error("Received error message: {} - {}", error.getErrorCode(), error.getErrorMessage());
        }

        // 错误消息通常不需要回复
        return null;
    }

    /**
     * 处理关闭消息
     */
    @Nullable
    private NetworkMessage handleCloseMessage(
            @NotNull NetworkMessage message) {
        if (message.hasClose()) {
            CloseMessage close = message.getClose();
            log.info("Received close message: {} - {}", close.getCloseReason(), close.getGraceful());

            // 清理客户端会话
            clientSessions.remove(message.getClientId());
        }

        // 关闭消息通常不需要回复
        return null;
    }

    /**
     * 发送消息
     */
    public void sendMessage(@NotNull NetworkMessage message) {
        log.debug("Sending message: {} of type: {}", message.getMessageId(), message.getType());
        sentCount.incrementAndGet();

        // 这里可以添加实际的消息发送逻辑
        // 例如：通过 WebSocket、TCP 等发送
    }

    /**
     * 广播消息给所有客户端
     */
    public void broadcastMessage(@NotNull NetworkMessage message) {
        log.debug("Broadcasting message: {} to {} clients", message.getMessageId(), clientSessions.size());

        for (String clientId : clientSessions.keySet()) {
            NetworkMessage broadcastMessage = message.toBuilder()
                    .setClientId(message.getClientId())
                    .setServerId(clientId)
                    .setMessageId(java.util.UUID.randomUUID().toString())
                    .build();
            sendMessage(broadcastMessage);
        }
    }

    /**
     * 发送消息给特定客户端
     */
    public void sendMessageToClient(@NotNull String clientId, @NotNull NetworkMessage message) {
        if (clientSessions.containsKey(clientId)) {
            NetworkMessage targetMessage = message.toBuilder()
                    .setServerId(clientId)
                    .setMessageId(java.util.UUID.randomUUID().toString())
                    .build();
            sendMessage(targetMessage);
        } else {
            log.warn("Client not found: {}", clientId);
        }
    }

    // 消息创建方法

    @NotNull
    private NetworkMessage createWelcomeMessage(@NotNull String clientId,
                                                @NotNull String serverId) {
        return messageFactory.createTextDataMessage(clientId, serverId, "welcome",
                "Welcome to the server!", null, 1);
    }

    @NotNull
    private NetworkMessage createHeartbeatAck(@NotNull String clientId, @NotNull String serverId) {
        return messageFactory.createAckMessage(clientId, serverId, "heartbeat",
                true, null, 0);
    }

    @NotNull
    private NetworkMessage createAckMessage(@NotNull String clientId, @NotNull String serverId,
            @NotNull String originalMessageId, boolean success, @Nullable String errorMessage, long processingTime) {
        return messageFactory.createAckMessage(clientId, serverId,
                originalMessageId, success, errorMessage,
                processingTime);
    }

    @NotNull
    private NetworkMessage createErrorMessage(@NotNull String clientId, @NotNull String serverId,
                                              int errorCode,
            @NotNull String errorMessage, @Nullable String details, @Nullable String stackTrace) {
        return messageFactory.createErrorMessage(clientId, serverId, errorCode,
                errorMessage, details, stackTrace);
    }

    /**
     * 获取服务器统计信息
     */
    @NotNull
    public ServerStats getStats() {
        return new ServerStats(receivedCount.get(), sentCount.get(), errorCount.get(), clientSessions.size());
    }

    /**
     * 获取连接的客户端列表
     */
    @NotNull
    public java.util.Set<String> getConnectedClients() {
        return clientSessions.keySet();
    }

    /**
     * 检查客户端是否连接
     */
    public boolean isClientConnected(@NotNull String clientId) {
        return clientSessions.containsKey(clientId);
    }

    /**
     * 服务器统计信息
     */
    public static class ServerStats {
        private final long receivedCount;
        private final long sentCount;
        private final long errorCount;
        private final int connectedClients;

        public ServerStats(long receivedCount, long sentCount, long errorCount, int connectedClients) {
            this.receivedCount = receivedCount;
            this.sentCount = sentCount;
            this.errorCount = errorCount;
            this.connectedClients = connectedClients;
        }

        public long getReceivedCount() {
            return receivedCount;
        }

        public long getSentCount() {
            return sentCount;
        }

        public long getErrorCount() {
            return errorCount;
        }

        public int getConnectedClients() {
            return connectedClients;
        }

        @Override
        public String toString() {
            return String.format("ServerStats{received=%d, sent=%d, errors=%d, clients=%d}", receivedCount, sentCount,
                    errorCount, connectedClients);
        }
    }
}