package com.dtc.core.messaging;

import com.dtc.core.statistics.StatisticsCollector;
import com.dtc.core.messaging.handler.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * NetworkMessageConsumer 单元测试
 */
public class NetworkMessageConsumerTest {

    @Mock
    private StatisticsCollector statisticsCollector;

    @Mock
    private HttpMessageHandler httpMessageHandler;

    @Mock
    private WebSocketMessageHandler webSocketMessageHandler;

    @Mock
    private MqttMessageHandler mqttMessageHandler;

    @Mock
    private TcpMessageHandler tcpMessageHandler;

    @Mock
    private CustomMessageHandler customMessageHandler;

    @Mock
    private ChannelHandlerContext channelContext;

    private NetworkMessageConsumer consumer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        consumer = new NetworkMessageConsumer(
                statisticsCollector,
                httpMessageHandler,
                webSocketMessageHandler,
                mqttMessageHandler,
                tcpMessageHandler,
                customMessageHandler);
    }

    @Test
    @DisplayName("测试消费HTTP消息")
    void testConsumeHttpMessage() {
        // 创建HTTP消息事件
        NetworkMessageEvent event = NetworkMessageEvent.builder()
                .protocolType("http")
                .message(mock(FullHttpRequest.class))
                .channelContext(channelContext)
                .messageType("HTTP_REQUEST")
                .build();

        // 消费消息
        consumer.consume(event, 1L, true);

        // 验证HTTP处理器被调用
        verify(httpMessageHandler).handleMessage(event);
    }

    @Test
    @DisplayName("测试消费WebSocket消息")
    void testConsumeWebSocketMessage() {
        // 创建WebSocket消息事件
        NetworkMessageEvent event = NetworkMessageEvent.builder()
                .protocolType("websocket")
                .message(mock(TextWebSocketFrame.class))
                .channelContext(channelContext)
                .messageType("WEBSOCKET_FRAME")
                .build();

        // 消费消息
        consumer.consume(event, 1L, true);

        // 验证WebSocket处理器被调用
        verify(webSocketMessageHandler).handleMessage(event);
    }

    @Test
    @DisplayName("测试消费MQTT消息")
    void testConsumeMqttMessage() {
        // 创建MQTT消息事件
        NetworkMessageEvent event = NetworkMessageEvent.builder()
                .protocolType("mqtt")
                .message("MQTT message")
                .channelContext(channelContext)
                .messageType("MQTT_MESSAGE")
                .build();

        // 消费消息
        consumer.consume(event, 1L, true);

        // 验证MQTT处理器被调用
        verify(mqttMessageHandler).handleMessage(event);
    }

    @Test
    @DisplayName("测试消费TCP消息")
    void testConsumeTcpMessage() {
        // 创建TCP消息事件
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeBytes("TCP message".getBytes());

        NetworkMessageEvent event = NetworkMessageEvent.builder()
                .protocolType("tcp")
                .message(byteBuf)
                .channelContext(channelContext)
                .messageType("TCP_MESSAGE")
                .build();

        // 消费消息
        consumer.consume(event, 1L, true);

        // 验证TCP处理器被调用
        verify(tcpMessageHandler).handleMessage(event);
    }

    @Test
    @DisplayName("测试消费自定义协议消息")
    void testConsumeCustomMessage() {
        // 创建自定义协议消息事件
        NetworkMessageEvent event = NetworkMessageEvent.builder()
                .protocolType("custom")
                .message("Custom message")
                .channelContext(channelContext)
                .messageType("CUSTOM_MESSAGE")
                .build();

        // 消费消息
        consumer.consume(event, 1L, true);

        // 验证自定义协议处理器被调用
        verify(customMessageHandler).handleMessage(event);
    }

    @Test
    @DisplayName("测试消费未知协议消息")
    void testConsumeUnknownProtocolMessage() {
        // 创建未知协议消息事件
        NetworkMessageEvent event = NetworkMessageEvent.builder()
                .protocolType("unknown")
                .message("Unknown message")
                .channelContext(channelContext)
                .messageType("UNKNOWN_MESSAGE")
                .build();

        // 消费消息（不应该抛出异常）
        assertDoesNotThrow(() -> consumer.consume(event, 1L, true));

        // 验证没有处理器被调用
        verify(httpMessageHandler, never()).handleMessage(any());
        verify(webSocketMessageHandler, never()).handleMessage(any());
        verify(mqttMessageHandler, never()).handleMessage(any());
        verify(tcpMessageHandler, never()).handleMessage(any());
        verify(customMessageHandler, never()).handleMessage(any());
    }

    @Test
    @DisplayName("测试消费空消息")
    void testConsumeNullMessage() {
        // 创建空消息事件
        NetworkMessageEvent event = NetworkMessageEvent.builder()
                .protocolType("http")
                .message(null)
                .channelContext(channelContext)
                .messageType("NULL_MESSAGE")
                .build();

        // 消费消息（不应该抛出异常）
        assertDoesNotThrow(() -> consumer.consume(event, 1L, true));
    }

    @Test
    @DisplayName("测试处理器异常处理")
    void testHandlerExceptionHandling() {
        // Mock处理器抛出异常
        doThrow(new RuntimeException("Handler error"))
                .when(httpMessageHandler).handleMessage(any());

        // 创建HTTP消息事件
        NetworkMessageEvent event = NetworkMessageEvent.builder()
                .protocolType("http")
                .message(mock(FullHttpRequest.class))
                .channelContext(channelContext)
                .messageType("HTTP_REQUEST")
                .build();

        // 消费消息（不应该抛出异常）
        assertDoesNotThrow(() -> consumer.consume(event, 1L, true));
    }

    @Test
    @DisplayName("测试并发消费")
    void testConcurrentConsumption() throws InterruptedException {
        int threadCount = 10;
        int messagesPerThread = 100;
        Thread[] threads = new Thread[threadCount];

        // 创建多个线程并发消费消息
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < messagesPerThread; j++) {
                    NetworkMessageEvent event = NetworkMessageEvent.builder()
                            .protocolType("http")
                            .message(mock(FullHttpRequest.class))
                            .channelContext(channelContext)
                            .messageType("HTTP_REQUEST")
                            .build();

                    consumer.consume(event, 1L, true);
                }
            });
        }

        // 启动所有线程
        for (Thread thread : threads) {
            thread.start();
        }

        // 等待所有线程完成
        for (Thread thread : threads) {
            thread.join();
        }

        // 验证所有消息都被处理
        verify(httpMessageHandler, times(threadCount * messagesPerThread))
                .handleMessage(any(NetworkMessageEvent.class));
    }

    @Test
    @DisplayName("测试不同消息类型的处理")
    void testDifferentMessageTypes() {
        // 测试HTTP消息
        NetworkMessageEvent httpEvent = NetworkMessageEvent.builder()
                .protocolType("http")
                .message(mock(FullHttpRequest.class))
                .channelContext(channelContext)
                .messageType("HTTP_REQUEST")
                .build();
        consumer.consume(httpEvent, 1L, true);
        verify(httpMessageHandler).handleMessage(httpEvent);

        // 测试WebSocket消息
        NetworkMessageEvent wsEvent = NetworkMessageEvent.builder()
                .protocolType("websocket")
                .message(mock(TextWebSocketFrame.class))
                .channelContext(channelContext)
                .messageType("WEBSOCKET_FRAME")
                .build();
        consumer.consume(wsEvent, 1L, true);
        verify(webSocketMessageHandler).handleMessage(wsEvent);

        // 测试MQTT消息
        NetworkMessageEvent mqttEvent = NetworkMessageEvent.builder()
                .protocolType("mqtt")
                .message("MQTT message")
                .channelContext(channelContext)
                .messageType("MQTT_MESSAGE")
                .build();
        consumer.consume(mqttEvent, 1L, true);
        verify(mqttMessageHandler).handleMessage(mqttEvent);

        // 测试TCP消息
        ByteBuf tcpData = Unpooled.buffer();
        tcpData.writeBytes("TCP data".getBytes());
        NetworkMessageEvent tcpEvent = NetworkMessageEvent.builder()
                .protocolType("tcp")
                .message(tcpData)
                .channelContext(channelContext)
                .messageType("TCP_MESSAGE")
                .build();
        consumer.consume(tcpEvent, 1L, true);
        verify(tcpMessageHandler).handleMessage(tcpEvent);

        // 测试自定义协议消息
        NetworkMessageEvent customEvent = NetworkMessageEvent.builder()
                .protocolType("custom")
                .message("Custom message")
                .channelContext(channelContext)
                .messageType("CUSTOM_MESSAGE")
                .build();
        consumer.consume(customEvent, 1L, true);
        verify(customMessageHandler).handleMessage(customEvent);
    }

    @Test
    @DisplayName("测试性能")
    void testPerformance() {
        int messageCount = 10000;
        long startTime = System.currentTimeMillis();

        // 处理大量消息
        for (int i = 0; i < messageCount; i++) {
            NetworkMessageEvent event = NetworkMessageEvent.builder()
                    .protocolType("http")
                    .message(mock(FullHttpRequest.class))
                    .channelContext(channelContext)
                    .messageType("HTTP_REQUEST")
                    .build();

            consumer.consume(event, 1L, true);
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // 验证性能（应该能在1秒内处理10000条消息）
        assertTrue(duration < 1000, "处理10000条消息应该在1秒内完成");

        // 验证所有消息都被处理
        verify(httpMessageHandler, times(messageCount)).handleMessage(any(NetworkMessageEvent.class));
    }
}
