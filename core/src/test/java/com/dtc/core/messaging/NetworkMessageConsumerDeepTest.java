package com.dtc.core.messaging;

import com.dtc.core.messaging.handler.*;
import com.dtc.core.statistics.StatisticsCollector;
import io.netty.channel.ChannelHandlerContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * NetworkMessageConsumer 深度测试
 * 测试边界条件、异常场景、并发情况
 */
@DisplayName("网络消息消费者深度测试")
public class NetworkMessageConsumerDeepTest {

    @Mock
    private StatisticsCollector mockStatisticsCollector;

    @Mock
    private HttpMessageHandler mockHttpHandler;

    @Mock
    private WebSocketMessageHandler mockWebSocketHandler;

    @Mock
    private MqttMessageHandler mockMqttHandler;

    @Mock
    private TcpMessageHandler mockTcpHandler;

    @Mock
    private UdpMessageHandler mockUdpHandler;

    @Mock
    private CustomMessageHandler mockCustomHandler;

    @Mock
    private ChannelHandlerContext mockContext;

    private NetworkMessageConsumer consumer;
    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        consumer = new NetworkMessageConsumer(
                mockStatisticsCollector,
                mockHttpHandler,
                mockWebSocketHandler,
                mockMqttHandler,
                mockTcpHandler,
                mockUdpHandler,
                mockCustomHandler);
    }

    @Test
    @DisplayName("测试并发消费消息")
    void testConcurrentConsume() throws InterruptedException {
        int threadCount = 10;
        int messagesPerThread = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount * messagesPerThread);
        
        doAnswer(invocation -> {
            latch.countDown();
            return null;
        }).when(mockHttpHandler).handleMessage(any());
        
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                for (int j = 0; j < messagesPerThread; j++) {
                    NetworkMessageEvent event = NetworkMessageEvent.builder()
                            .eventId("test-" + j)
                            .protocolType("HTTP")
                            .message("test message")
                            .channelContext(mockContext)
                            .build();
                    consumer.consume(event, j, false);
                }
            });
        }
        
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        executor.shutdown();
        
        verify(mockStatisticsCollector, atLeast(threadCount * messagesPerThread)).onRequestStart();
    }

    @Test
    @DisplayName("测试消费大量消息")
    void testConsumeLargeBatch() {
        int messageCount = 10000;
        
        for (int i = 0; i < messageCount; i++) {
            NetworkMessageEvent event = NetworkMessageEvent.builder()
                    .eventId("test-" + i)
                    .protocolType("HTTP")
                    .message("test message " + i)
                    .channelContext(mockContext)
                    .build();
            consumer.consume(event, i, i == messageCount - 1);
        }
        
        verify(mockStatisticsCollector, times(messageCount)).onRequestStart();
    }

    @Test
    @DisplayName("测试处理所有支持的协议类型")
    void testAllProtocolTypes() {
        String[] protocols = {"HTTP", "HTTPS", "WebSocket", "WS", "WSS", "MQTT", "TCP", "UDP", "Custom"};
        
        for (String protocol : protocols) {
            NetworkMessageEvent event = NetworkMessageEvent.builder()
                    .eventId("test-" + protocol)
                    .protocolType(protocol)
                    .message("test message")
                    .channelContext(mockContext)
                    .build();
            
            assertDoesNotThrow(() -> consumer.consume(event, 1L, false));
        }
    }

    @Test
    @DisplayName("测试处理不支持的协议类型")
    void testUnsupportedProtocol() {
        NetworkMessageEvent event = NetworkMessageEvent.builder()
                .eventId("test-unsupported")
                .protocolType("UNSUPPORTED_PROTOCOL")
                .message("test message")
                .channelContext(mockContext)
                .build();
        
        assertDoesNotThrow(() -> consumer.consume(event, 1L, false));
        
        // 不应该调用统计
        verify(mockStatisticsCollector, never()).onRequestStart();
    }

    @Test
    @DisplayName("测试处理器抛出异常")
    void testHandlerThrowsException() {
        doThrow(new RuntimeException("Handler error")).when(mockHttpHandler).handleMessage(any());
        
        NetworkMessageEvent event = NetworkMessageEvent.builder()
                .eventId("test-error")
                .protocolType("HTTP")
                .message("test message")
                .channelContext(mockContext)
                .build();
        
        assertDoesNotThrow(() -> consumer.consume(event, 1L, false));
        
        verify(mockStatisticsCollector, times(1)).onRequestError();
    }

    @Test
    @DisplayName("测试endOfBatch标志")
    void testEndOfBatch() {
        NetworkMessageEvent event = NetworkMessageEvent.builder()
                .eventId("test-batch")
                .protocolType("HTTP")
                .message("test message")
                .channelContext(mockContext)
                .build();
        
        // 测试非批次结束
        consumer.consume(event, 1L, false);
        
        // 测试批次结束
        consumer.consume(event, 2L, true);
        
        verify(mockHttpHandler, times(2)).handleMessage(any());
    }
}

