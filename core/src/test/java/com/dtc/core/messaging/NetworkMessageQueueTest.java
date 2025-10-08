package com.dtc.core.messaging;

import com.dtc.core.statistics.StatisticsCollector;
import com.dtc.core.messaging.handler.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Timeout;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * NetworkMessageQueue 单元测试
 */
public class NetworkMessageQueueTest {

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
    private NetworkMessageConsumer consumer;

    private NetworkMessageQueue messageQueue;

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
        messageQueue = new NetworkMessageQueue(consumer);
    }

    @Test
    @DisplayName("测试队列启动和停止")
    void testQueueStartAndStop() {
        // 测试启动
        assertDoesNotThrow(() -> messageQueue.start());

        // 测试停止
        assertDoesNotThrow(() -> messageQueue.stop());
    }

    @Test
    @DisplayName("测试消息发布")
    @Timeout(5)
    void testMessagePublish() throws InterruptedException {
        // 启动队列
        messageQueue.start();

        // 创建测试消息
        NetworkMessageEvent event = NetworkMessageEvent.builder()
                .protocolType("test")
                .message("test message")
                .messageType("TEST_MESSAGE")
                .build();

        // 发布消息
        boolean published = messageQueue.publish(event);
        assertTrue(published, "消息应该成功发布");

        // 等待处理
        Thread.sleep(100);

        // 停止队列
        messageQueue.stop();
    }

    @Test
    @DisplayName("测试队列状态")
    void testQueueStatus() {
        // 启动前状态
        NetworkMessageQueue.QueueStatus status = messageQueue.getStatus();
        assertNotNull(status);

        // 启动队列
        messageQueue.start();

        // 启动后状态
        status = messageQueue.getStatus();
        assertNotNull(status);

        // 停止队列
        messageQueue.stop();
    }

    @Test
    @DisplayName("测试高并发消息发布")
    @Timeout(10)
    void testConcurrentMessagePublish() throws InterruptedException {
        messageQueue.start();

        int messageCount = 1000;
        CountDownLatch latch = new CountDownLatch(messageCount);
        AtomicInteger successCount = new AtomicInteger(0);

        // 并发发布消息
        for (int i = 0; i < messageCount; i++) {
            new Thread(() -> {
                NetworkMessageEvent event = NetworkMessageEvent.builder()
                        .protocolType("test")
                        .message("concurrent test message")
                        .messageType("CONCURRENT_TEST")
                        .build();

                if (messageQueue.publish(event)) {
                    successCount.incrementAndGet();
                }
                latch.countDown();
            }).start();
        }

        // 等待所有消息处理完成
        assertTrue(latch.await(5, TimeUnit.SECONDS), "所有消息应该在5秒内处理完成");

        // 验证成功发布的消息数量
        assertTrue(successCount.get() > 0, "应该有消息成功发布");

        messageQueue.stop();
    }

    @Test
    @DisplayName("测试队列满时的处理")
    void testQueueFullHandling() {
        messageQueue.start();

        // 发布大量消息直到队列满
        int publishedCount = 0;
        for (int i = 0; i < 10000; i++) {
            NetworkMessageEvent event = NetworkMessageEvent.builder()
                    .protocolType("test")
                    .message("test message " + i)
                    .messageType("TEST_MESSAGE")
                    .build();

            if (messageQueue.publish(event)) {
                publishedCount++;
            } else {
                break; // 队列满，停止发布
            }
        }

        assertTrue(publishedCount > 0, "应该有一些消息成功发布");

        messageQueue.stop();
    }

    @Test
    @DisplayName("测试不同协议类型的消息")
    void testDifferentProtocolMessages() {
        messageQueue.start();

        // 测试HTTP消息
        NetworkMessageEvent httpEvent = NetworkMessageEvent.builder()
                .protocolType("http")
                .message("HTTP message")
                .messageType("HTTP_REQUEST")
                .build();
        assertTrue(messageQueue.publish(httpEvent));

        // 测试WebSocket消息
        NetworkMessageEvent wsEvent = NetworkMessageEvent.builder()
                .protocolType("websocket")
                .message("WebSocket message")
                .messageType("WEBSOCKET_FRAME")
                .build();
        assertTrue(messageQueue.publish(wsEvent));

        // 测试MQTT消息
        NetworkMessageEvent mqttEvent = NetworkMessageEvent.builder()
                .protocolType("mqtt")
                .message("MQTT message")
                .messageType("MQTT_MESSAGE")
                .build();
        assertTrue(messageQueue.publish(mqttEvent));

        messageQueue.stop();
    }

    @Test
    @DisplayName("测试消息事件构建器")
    void testNetworkMessageEventBuilder() {
        NetworkMessageEvent event = NetworkMessageEvent.builder()
                .protocolType("test")
                .clientId("test-client")
                .message("test message")
                .messageType("TEST_MESSAGE")
                .messageSize(12)
                .isRequest(true)
                .priority(1)
                .build();

        assertNotNull(event);
        assertEquals("test", event.getProtocolType());
        assertEquals("test-client", event.getClientId());
        assertEquals("test message", event.getMessage());
        assertEquals("TEST_MESSAGE", event.getMessageType());
        assertEquals(12, event.getMessageSize());
        assertTrue(event.isRequest());
        assertEquals(1, event.getPriority());
    }

    @Test
    @DisplayName("测试队列性能")
    @Timeout(10)
    void testQueuePerformance() throws InterruptedException {
        messageQueue.start();

        long startTime = System.currentTimeMillis();
        int messageCount = 10000;

        // 发布大量消息
        for (int i = 0; i < messageCount; i++) {
            NetworkMessageEvent event = NetworkMessageEvent.builder()
                    .protocolType("test")
                    .message("performance test message " + i)
                    .messageType("PERFORMANCE_TEST")
                    .build();

            messageQueue.publish(event);
        }

        // 等待处理完成
        Thread.sleep(1000);

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // 验证性能（应该能在合理时间内处理10000条消息）
        assertTrue(duration < 5000, "处理10000条消息应该在5秒内完成");

        messageQueue.stop();
    }
}
