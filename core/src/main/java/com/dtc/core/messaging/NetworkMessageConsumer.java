package com.dtc.core.messaging;

import com.dtc.api.annotations.NotNull;
import com.dtc.core.queue.QueueConsumer;
import com.dtc.core.statistics.StatisticsCollector;
import com.dtc.core.messaging.handler.HttpMessageHandler;
import com.dtc.core.messaging.handler.WebSocketMessageHandler;
import com.dtc.core.messaging.handler.MqttMessageHandler;
import com.dtc.core.messaging.handler.TcpMessageHandler;
import com.dtc.core.messaging.handler.CustomMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * 网络消息消费者
 * 整合DisruptorQueue和NetworkMessageEvent的消息处理器
 * 
 * @author Network Service Template
 */
@Singleton
public class NetworkMessageConsumer implements QueueConsumer<NetworkMessageEvent> {

    private static final Logger log = LoggerFactory.getLogger(NetworkMessageConsumer.class);

    private final StatisticsCollector statisticsCollector;
    private final Map<String, Consumer<NetworkMessageEvent>> protocolHandlers = new ConcurrentHashMap<>();

    // 专门的协议处理器
    private final HttpMessageHandler httpMessageHandler;
    private final WebSocketMessageHandler webSocketMessageHandler;
    private final MqttMessageHandler mqttMessageHandler;
    private final TcpMessageHandler tcpMessageHandler;
    private final CustomMessageHandler customMessageHandler;

    @Inject
    public NetworkMessageConsumer(@NotNull StatisticsCollector statisticsCollector,
            @NotNull HttpMessageHandler httpMessageHandler,
            @NotNull WebSocketMessageHandler webSocketMessageHandler,
            @NotNull MqttMessageHandler mqttMessageHandler,
            @NotNull TcpMessageHandler tcpMessageHandler,
            @NotNull CustomMessageHandler customMessageHandler) {
        this.statisticsCollector = statisticsCollector;
        this.httpMessageHandler = httpMessageHandler;
        this.webSocketMessageHandler = webSocketMessageHandler;
        this.mqttMessageHandler = mqttMessageHandler;
        this.tcpMessageHandler = tcpMessageHandler;
        this.customMessageHandler = customMessageHandler;
        initializeDefaultHandlers();
    }

    /**
     * 初始化默认处理器
     */
    private void initializeDefaultHandlers() {
        // 注册专门的协议处理器
        registerHandler("http", httpMessageHandler::handleMessage);
        registerHandler("websocket", webSocketMessageHandler::handleMessage);
        registerHandler("mqtt", mqttMessageHandler::handleMessage);
        registerHandler("tcp", tcpMessageHandler::handleMessage);
        registerHandler("custom", customMessageHandler::handleMessage);
        log.info("✅ Initialized network message consumer with {} protocol handlers", protocolHandlers.size());
    }

    @Override
    public void consume(@NotNull NetworkMessageEvent event, long sequence, boolean endOfBatch) {
        long startTime = System.currentTimeMillis();

        try {
            statisticsCollector.onRequestStart();

            String protocolType = event.getProtocolType();
            if (protocolType == null) {
                log.warn("⚠️ No protocol type specified for event: {}", event.getEventId());
                return;
            }

            Consumer<NetworkMessageEvent> handler = protocolHandlers.get(protocolType.toLowerCase());
            if (handler != null) {
                handler.accept(event);
                log.debug("🔄 Processed {} message: {}", protocolType, event.getEventId());
            } else {
                log.warn("⚠️ No handler found for protocol: {}, using default handler", protocolType);
                handleUnknownMessage(event);
            }

            long processingTime = System.currentTimeMillis() - startTime;
            statisticsCollector.onRequestComplete(processingTime);

        } catch (Exception e) {
            log.error("❌ Failed to process message event: {}", event.getEventId(), e);
            statisticsCollector.onRequestError();
            handleError(event, e);
        }
    }

    /**
     * 注册协议处理器
     */
    public void registerHandler(@NotNull String protocolType, @NotNull Consumer<NetworkMessageEvent> handler) {
        protocolHandlers.put(protocolType.toLowerCase(), handler);
        log.debug("📋 Registered handler for protocol: {}", protocolType);
    }

    /**
     * 注销协议处理器
     */
    public void unregisterHandler(@NotNull String protocolType) {
        protocolHandlers.remove(protocolType.toLowerCase());
        log.debug("📋 Unregistered handler for protocol: {}", protocolType);
    }

    // ========== 协议特定处理器已移至专门的Handler类 ==========
    // HttpMessageHandler, WebSocketMessageHandler, MqttMessageHandler,
    // TcpMessageHandler, CustomMessageHandler

    private void handleUnknownMessage(@NotNull NetworkMessageEvent event) {
        log.debug("❓ Processing unknown message: {}", event.getEventId());
        // 未知消息处理逻辑
    }

    private void handleError(@NotNull NetworkMessageEvent event, @NotNull Exception error) {
        log.error("💥 Error handling message: {}", event.getEventId(), error);
        // 错误处理逻辑
    }

    /**
     * 获取支持的协议类型
     */
    @NotNull
    public java.util.Set<String> getSupportedProtocols() {
        return java.util.Set.copyOf(protocolHandlers.keySet());
    }

    /**
     * 检查是否支持指定协议
     */
    public boolean supportsProtocol(@NotNull String protocolType) {
        return protocolHandlers.containsKey(protocolType.toLowerCase());
    }
}
