package com.dtc.core.protobuf;

import com.dtc.api.annotations.NotNull;
import com.dtc.core.protobuf.NetworkMessageProtos.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

/**
 * Protobuf 消息使用示例 演示如何创建和使用各种类型的 Protobuf 消息
 * 
 * @author Network Service Template
 */
@Singleton
public class ProtobufMessageExample {

    private static final Logger log = LoggerFactory.getLogger(ProtobufMessageExample.class);

    private final MessageFactory messageFactory;
    private final ClientMessageHandler clientHandler;
    private final ServerMessageHandler serverHandler;

    @Inject
    public ProtobufMessageExample(@NotNull MessageFactory messageFactory, @NotNull ClientMessageHandler clientHandler,
            @NotNull ServerMessageHandler serverHandler) {
        this.messageFactory = messageFactory;
        this.clientHandler = clientHandler;
        this.serverHandler = serverHandler;
    }

    /**
     * 演示客户端消息创建和发送
     */
    public void demonstrateClientMessages() {
        log.info("=== 客户端消息演示 ===");

        String clientId = "client-001";
        String serverId = "server-001";

        // 1. 发送心跳消息
        Map<String, String> metadata = new HashMap<>();
        metadata.put("version", "1.0");
        metadata.put("platform", "java");

        NetworkMessage heartbeatMessage = clientHandler.sendHeartbeat(clientId, serverId, metadata);
        log.info("发送心跳消息: {}", heartbeatMessage.getMessageId());

        // 2. 发送文本数据消息
        NetworkMessage textMessage = clientHandler.sendTextData(clientId, serverId, "chat", "Hello, Server!", null, 1);
        log.info("发送文本消息: {}", textMessage.getMessageId());

        // 3. 发送 JSON 数据消息
        String jsonData = "{\"action\":\"login\",\"username\":\"user123\",\"password\":\"***\"}";
        NetworkMessage jsonMessage = clientHandler.sendJsonData(clientId, serverId, "auth", jsonData, null, 2);
        log.info("发送 JSON 消息: {}", jsonMessage.getMessageId());

        // 4. 发送用户消息
        String[] roles = { "user", "admin" };
        Map<String, String> attributes = new HashMap<>();
        attributes.put("department", "IT");
        attributes.put("location", "Beijing");

        NetworkMessage userMessage = clientHandler.sendUserMessage(clientId, serverId, 1001L, "john_doe",
                "john@example.com", roles, attributes);
        log.info("发送用户消息: {}", userMessage.getMessageId());

        // 5. 发送订单消息
        OrderItem[] items = { messageFactory.createOrderItem(1001L, "Product A", 2, 99.99),
                messageFactory.createOrderItem(1002L, "Product B", 1, 149.99) };

        NetworkMessage orderMessage = clientHandler.sendOrderMessage(clientId, serverId, 2001L, 1001L, items, 349.97,
                "pending");
        log.info("发送订单消息: {}", orderMessage.getMessageId());

        // 6. 发送产品消息
        String[] tags = { "electronics", "smartphone", "android" };
        Map<String, String> properties = new HashMap<>();
        properties.put("brand", "Samsung");
        properties.put("model", "Galaxy S21");
        properties.put("color", "Black");

        NetworkMessage productMessage = clientHandler.sendProductMessage(clientId, serverId, 3001L,
                "Samsung Galaxy S21", "Latest smartphone with 5G", 999.99, "Electronics", tags, properties);
        log.info("发送产品消息: {}", productMessage.getMessageId());
    }

    /**
     * 演示服务器端消息处理
     */
    public void demonstrateServerMessages() {
        log.info("=== 服务器端消息演示 ===");

        String clientId = "client-001";
        String serverId = "server-001";

        // 1. 处理客户端连接
        serverHandler.handleClientConnect(clientId, serverId);

        // 2. 创建并处理心跳消息
        NetworkMessage heartbeatMessage = messageFactory.createHeartbeatMessage(clientId, serverId, null);
        NetworkMessage heartbeatResponse = serverHandler.handleReceivedMessage(heartbeatMessage);
        if (heartbeatResponse != null) {
            log.info("处理心跳消息响应: {}", heartbeatResponse.getMessageId());
        }

        // 3. 创建并处理数据消息
        String jsonData = "{\"action\":\"login\",\"username\":\"user123\"}";
        NetworkMessage dataMessage = messageFactory.createJsonDataMessage(clientId, serverId, "auth", jsonData, null,
                1);
        NetworkMessage dataResponse = serverHandler.handleReceivedMessage(dataMessage);
        if (dataResponse != null) {
            log.info("处理数据消息响应: {}", dataResponse.getMessageId());
        }

        // 4. 广播消息给所有客户端
        NetworkMessage broadcastMessage = messageFactory.createTextDataMessage(serverId, null, "announcement",
                "Server maintenance in 10 minutes", null, 1);
        serverHandler.broadcastMessage(broadcastMessage);
        log.info("广播消息: {}", broadcastMessage.getMessageId());

        // 5. 发送消息给特定客户端
        NetworkMessage privateMessage = messageFactory.createTextDataMessage(serverId, null, "private",
                "Private message for you", null, 1);
        serverHandler.sendMessageToClient(clientId, privateMessage);
        log.info("发送私聊消息: {}", privateMessage.getMessageId());

        // 6. 处理客户端断开连接
        serverHandler.handleClientDisconnect(clientId, serverId);
    }

    /**
     * 演示消息序列化和反序列化
     */
    public void demonstrateSerialization() {
        log.info("=== 消息序列化演示 ===");

        String clientId = "client-001";
        String serverId = "server-001";

        // 1. 创建消息
        NetworkMessage originalMessage = messageFactory.createTextDataMessage(clientId, serverId, "test",
                "Hello, Protobuf!", null, 1);
        log.info("原始消息: {}", originalMessage.getMessageId());

        // 2. 序列化消息
        byte[] serializedData = messageFactory.serializeNetworkMessage(originalMessage);
        log.info("序列化后大小: {} bytes", serializedData.length);

        // 3. 反序列化消息
        try {
            NetworkMessage deserializedMessage = messageFactory.parseNetworkMessage(serializedData);
            log.info("反序列化消息: {}", deserializedMessage.getMessageId());

            // 4. 验证消息内容
            if (originalMessage.getMessageId().equals(deserializedMessage.getMessageId())
                    && originalMessage.getType() == deserializedMessage.getType()
                    && originalMessage.getClientId().equals(deserializedMessage.getClientId())) {
                log.info("✅ 消息序列化/反序列化验证成功");
            } else {
                log.error("❌ 消息序列化/反序列化验证失败");
            }
        } catch (Exception e) {
            log.error("反序列化失败", e);
        }
    }

    /**
     * 演示批量消息处理
     */
    public void demonstrateBatchProcessing() {
        log.info("=== 批量消息处理演示 ===");

        String clientId = "client-001";
        String serverId = "server-001";

        // 创建多个消息
        NetworkMessage[] messages = new NetworkMessage[5];
        for (int i = 0; i < 5; i++) {
            messages[i] = messageFactory.createTextDataMessage(clientId, serverId, "batch", "Batch message " + (i + 1),
                    null, 1);
        }

        // 批量序列化
        byte[][] serializedMessages = new byte[messages.length][];
        for (int i = 0; i < messages.length; i++) {
            serializedMessages[i] = messageFactory.serializeNetworkMessage(messages[i]);
        }

        log.info("批量序列化完成，共 {} 条消息", messages.length);

        // 批量反序列化
        try {
            for (int i = 0; i < serializedMessages.length; i++) {
                NetworkMessage deserializedMessage = messageFactory.parseNetworkMessage(serializedMessages[i]);
                log.debug("反序列化消息 {}: {}", i + 1, deserializedMessage.getMessageId());
            }
            log.info("✅ 批量反序列化完成");
        } catch (Exception e) {
            log.error("批量反序列化失败", e);
        }
    }

    /**
     * 演示错误处理
     */
    public void demonstrateErrorHandling() {
        log.info("=== 错误处理演示 ===");

        String clientId = "client-001";
        String serverId = "server-001";

        // 1. 创建错误消息
        NetworkMessage errorMessage = messageFactory.createErrorMessage(clientId, serverId, 500,
                "Internal server error", "Database connection failed", "java.sql.SQLException: Connection timeout");
        log.info("创建错误消息: {}", errorMessage.getMessageId());

        // 2. 处理错误消息
        serverHandler.handleReceivedMessage(errorMessage);

        // 3. 创建关闭消息
        NetworkMessage closeMessage = messageFactory.createCloseMessage(clientId, serverId, 1000, "Normal closure",
                true);
        log.info("创建关闭消息: {}", closeMessage.getMessageId());

        // 4. 处理关闭消息
        serverHandler.handleReceivedMessage(closeMessage);
    }

    /**
     * 运行所有演示
     */
    public void runAllDemonstrations() {
        log.info("🚀 开始 Protobuf 消息演示");

        try {
            demonstrateClientMessages();
            demonstrateServerMessages();
            demonstrateSerialization();
            demonstrateBatchProcessing();
            demonstrateErrorHandling();

            log.info("✅ 所有演示完成");

            // 输出统计信息
            log.info("客户端统计: {}", clientHandler.getStats());
            log.info("服务器统计: {}", serverHandler.getStats());

        } catch (Exception e) {
            log.error("演示过程中发生错误", e);
        }
    }
}
