package com.dtc.core.messaging;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dtc.api.annotations.NotNull;
import com.dtc.core.messaging.handler.CustomMessageHandler;
import com.dtc.core.messaging.handler.HttpMessageHandler;
import com.dtc.core.messaging.handler.MqttMessageHandler;
import com.dtc.core.messaging.handler.TcpMessageHandler;
import com.dtc.core.messaging.handler.UdpMessageHandler;
import com.dtc.core.messaging.handler.WebSocketMessageHandler;
import com.dtc.core.queue.QueueConsumer;
import com.dtc.core.statistics.StatisticsCollector;

/**
 * 网络消息消费者
 * 从DisruptorQueue消费NetworkMessageEvent并分发给协议处理器
 * 
 * @author Network Service Template
 */
@Singleton
public class NetworkMessageConsumer implements QueueConsumer<NetworkMessageEvent> {

    private static final Logger log = LoggerFactory.getLogger(NetworkMessageConsumer.class);

    private final StatisticsCollector statisticsCollector;
    private final Map<String, Consumer<NetworkMessageEvent>> protocolHandlers = new ConcurrentHashMap<>();

    // 注入的协议处理器
    private final HttpMessageHandler httpMessageHandler;
    private final WebSocketMessageHandler webSocketMessageHandler;
    private final MqttMessageHandler mqttMessageHandler;
    private final TcpMessageHandler tcpMessageHandler;
    private final UdpMessageHandler udpMessageHandler;
    private final CustomMessageHandler customMessageHandler;

    @Inject
    public NetworkMessageConsumer(@NotNull StatisticsCollector statisticsCollector,
            @NotNull HttpMessageHandler httpMessageHandler,
            @NotNull WebSocketMessageHandler webSocketMessageHandler,
            @NotNull MqttMessageHandler mqttMessageHandler,
            @NotNull TcpMessageHandler tcpMessageHandler,
            @NotNull UdpMessageHandler udpMessageHandler,
            @NotNull CustomMessageHandler customMessageHandler) {
        this.statisticsCollector = statisticsCollector;
        this.httpMessageHandler = httpMessageHandler;
        this.webSocketMessageHandler = webSocketMessageHandler;
        this.mqttMessageHandler = mqttMessageHandler;
        this.tcpMessageHandler = tcpMessageHandler;
        this.udpMessageHandler = udpMessageHandler;
        this.customMessageHandler = customMessageHandler;

        // 注册协议处理器
        registerProtocolHandlers();
    }

    /**
     * 注册协议处理器
     */
    private void registerProtocolHandlers() {
        protocolHandlers.put("HTTP", httpMessageHandler::handleMessage);
        protocolHandlers.put("HTTPS", httpMessageHandler::handleMessage);
        protocolHandlers.put("WebSocket", webSocketMessageHandler::handleMessage);
        protocolHandlers.put("WS", webSocketMessageHandler::handleMessage);
        protocolHandlers.put("WSS", webSocketMessageHandler::handleMessage);
        protocolHandlers.put("MQTT", mqttMessageHandler::handleMessage);
        protocolHandlers.put("TCP", tcpMessageHandler::handleMessage);
        protocolHandlers.put("UDP", udpMessageHandler::handleMessage);
        protocolHandlers.put("Custom", customMessageHandler::handleMessage);
    }

    @Override
    public void consume(NetworkMessageEvent event) {
        if (event == null || !event.isValid()) {
            log.warn("Invalid event received, skipping");
            return;
        }

        try {
            String protocolType = event.getProtocolType();
            if (protocolType == null || protocolType.isEmpty()) {
                log.warn("Event has no protocol type, skipping: {}", event.getEventId());
                return;
            }

            Consumer<NetworkMessageEvent> handler = protocolHandlers.get(protocolType);
            if (handler == null) {
                log.warn("No handler found for protocol: {}", protocolType);
                return;
            }

            // 更新统计信息
            statisticsCollector.recordMessageReceived(protocolType, event.getMessageSize());

            // 处理消息
            handler.accept(event);

        } catch (Exception e) {
            log.error("Error processing message event: {}", event.getEventId(), e);
            statisticsCollector.recordError(event.getProtocolType(), "CONSUME_ERROR");
        }
    }

    @Override
    public void onError(Throwable error) {
        log.error("Error in NetworkMessageConsumer", error);
        statisticsCollector.recordError("UNKNOWN", "CONSUMER_ERROR");
    }
}
