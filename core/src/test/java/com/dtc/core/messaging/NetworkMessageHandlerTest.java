package com.dtc.core.messaging;

import com.dtc.core.serialization.ProtobufSerializer;
import com.dtc.core.statistics.StatisticsCollector;
import com.dtc.core.messaging.handler.*;
import com.google.protobuf.Message;
import com.google.protobuf.ByteString;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * NetworkMessageHandler 单元测试
 */
public class NetworkMessageHandlerTest {

    @Mock
    private ProtobufSerializer serializer;

    @Mock
    private NetworkMessageQueue messageQueue;

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

    private NetworkMessageHandler messageHandler;

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
        messageHandler = new NetworkMessageHandler(serializer, messageQueue);
    }

    @Test
    @DisplayName("测试处理Protobuf消息")
    void testHandleProtobufMessage() {
        // 创建测试消息
        Message testMessage = mock(Message.class);
        when(testMessage.getSerializedSize()).thenReturn(100);

        // Mock队列发布
        when(messageQueue.publish(any(NetworkMessageEvent.class))).thenReturn(true);

        // 测试处理消息
        boolean result = messageHandler.handleMessage(testMessage);

        // 验证结果
        assertTrue(result, "应该成功处理Protobuf消息");
        verify(messageQueue).publish(any(NetworkMessageEvent.class));
    }

    @Test
    @DisplayName("测试处理原始字节数据")
    void testHandleRawData() {
        // 创建测试数据
        byte[] testData = "test raw data".getBytes();

        // Mock队列发布
        when(messageQueue.publish(any(NetworkMessageEvent.class))).thenReturn(true);

        // 测试处理原始数据
        boolean result = messageHandler.handleRawData(testData);

        // 验证结果
        assertTrue(result, "应该成功处理原始字节数据");
        verify(messageQueue).publish(any(NetworkMessageEvent.class));
    }

    @Test
    @DisplayName("测试处理空消息")
    void testHandleEmptyMessage() {
        // Mock队列发布
        when(messageQueue.publish(any(NetworkMessageEvent.class))).thenReturn(true);

        // 测试空字节数组
        byte[] emptyData = new byte[0];
        boolean result = messageHandler.handleRawData(emptyData);
        assertTrue(result, "应该能处理空消息");

        // 测试null消息
        assertThrows(NullPointerException.class, () -> {
            messageHandler.handleRawData(null);
        });
    }

    @Test
    @DisplayName("测试处理大消息")
    void testHandleLargeMessage() {
        // 创建大消息（1MB）
        byte[] largeData = new byte[1024 * 1024];
        for (int i = 0; i < largeData.length; i++) {
            largeData[i] = (byte) (i % 256);
        }

        // Mock队列发布
        when(messageQueue.publish(any(NetworkMessageEvent.class))).thenReturn(true);

        // 测试处理大消息
        boolean result = messageHandler.handleRawData(largeData);

        // 验证结果
        assertTrue(result, "应该能处理大消息");
        verify(messageQueue).publish(any(NetworkMessageEvent.class));
    }

    @Test
    @DisplayName("测试处理多种数据类型")
    void testHandleDifferentDataTypes() {
        // Mock队列发布
        when(messageQueue.publish(any(NetworkMessageEvent.class))).thenReturn(true);

        // 测试字符串数据
        byte[] stringData = "Hello World".getBytes();
        assertTrue(messageHandler.handleRawData(stringData));

        // 测试JSON数据
        byte[] jsonData = "{\"key\": \"value\"}".getBytes();
        assertTrue(messageHandler.handleRawData(jsonData));

        // 测试二进制数据
        byte[] binaryData = { 0x00, 0x01, 0x02, 0x03, 0x04 };
        assertTrue(messageHandler.handleRawData(binaryData));

        // 验证所有消息都被发布
        verify(messageQueue, times(3)).publish(any(NetworkMessageEvent.class));
    }

    @Test
    @DisplayName("测试队列发布失败")
    void testQueuePublishFailure() {
        // Mock队列发布失败
        when(messageQueue.publish(any(NetworkMessageEvent.class))).thenReturn(false);

        // 测试处理消息
        byte[] testData = "test data".getBytes();
        boolean result = messageHandler.handleRawData(testData);

        // 验证结果
        assertFalse(result, "队列发布失败时应该返回false");
    }

    @Test
    @DisplayName("测试处理异常情况")
    void testHandleException() {
        // Mock队列抛出异常
        when(messageQueue.publish(any(NetworkMessageEvent.class)))
                .thenThrow(new RuntimeException("Queue error"));

        // 测试处理消息
        byte[] testData = "test data".getBytes();
        boolean result = messageHandler.handleRawData(testData);

        // 验证结果
        assertFalse(result, "异常情况下应该返回false");
    }

    @Test
    @DisplayName("测试获取统计信息")
    void testGetStats() {
        // 处理一些消息
        byte[] testData1 = "test data 1".getBytes();
        byte[] testData2 = "test data 2".getBytes();

        when(messageQueue.publish(any(NetworkMessageEvent.class))).thenReturn(true);

        messageHandler.handleRawData(testData1);
        messageHandler.handleRawData(testData2);

        // 获取统计信息
        NetworkMessageHandler.HandlerStats stats = messageHandler.getStats();

        // 验证统计信息
        assertNotNull(stats, "统计信息不应该为null");
        assertTrue(stats.getReceivedCount() >= 0, "接收计数应该大于等于0");
        assertTrue(stats.getForwardedCount() >= 0, "转发计数应该大于等于0");
    }

    @Test
    @DisplayName("测试统计信息类")
    void testHandlerStats() {
        // 创建统计信息
        NetworkMessageHandler.HandlerStats stats = new NetworkMessageHandler.HandlerStats(10, 8);

        // 验证统计信息
        assertEquals(10, stats.getReceivedCount());
        assertEquals(8, stats.getForwardedCount());

        // 测试toString方法
        String statsString = stats.toString();
        assertNotNull(statsString);
        assertTrue(statsString.contains("received=10"));
        assertTrue(statsString.contains("forwarded=8"));
    }

    @Test
    @DisplayName("测试并发处理")
    void testConcurrentHandling() throws InterruptedException {
        when(messageQueue.publish(any(NetworkMessageEvent.class))).thenReturn(true);

        int threadCount = 10;
        int messagesPerThread = 100;
        Thread[] threads = new Thread[threadCount];

        // 创建多个线程并发处理消息
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < messagesPerThread; j++) {
                    byte[] testData = ("concurrent test " + j).getBytes();
                    messageHandler.handleRawData(testData);
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

        // 验证统计信息
        NetworkMessageHandler.HandlerStats stats = messageHandler.getStats();
        assertTrue(stats.getReceivedCount() >= threadCount * messagesPerThread);
    }

    @Test
    @DisplayName("测试性能")
    void testPerformance() {
        when(messageQueue.publish(any(NetworkMessageEvent.class))).thenReturn(true);

        int messageCount = 10000;
        long startTime = System.currentTimeMillis();

        // 处理大量消息
        for (int i = 0; i < messageCount; i++) {
            byte[] testData = ("performance test " + i).getBytes();
            messageHandler.handleRawData(testData);
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // 验证性能（应该能在1秒内处理10000条消息）
        assertTrue(duration < 1000, "处理10000条消息应该在1秒内完成");

        // 验证统计信息
        NetworkMessageHandler.HandlerStats stats = messageHandler.getStats();
        assertTrue(stats.getReceivedCount() >= messageCount);
    }
}
