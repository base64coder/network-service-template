package com.dtc.core.messaging;

import com.dtc.core.messaging.handler.*;
import com.dtc.core.statistics.StatisticsCollector;
import io.netty.channel.ChannelHandlerContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * NetworkMessageConsumer 测试
 */
@DisplayName("网络消息消费者测试")
public class NetworkMessageConsumerTest {

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
    @DisplayName("测试创建消费者")
    void testCreateConsumer() {
        assertNotNull(consumer);
    }

    @Test
    @DisplayName("测试消费HTTP消息")
    void testConsumeHttpMessage() {
        NetworkMessageEvent event = NetworkMessageEvent.builder()
                .eventId("test-001")
                .protocolType("HTTP")
                .message("test message")
                .channelContext(mockContext)
                .build();
        
        assertDoesNotThrow(() -> consumer.consume(event, 1L, false));
        
        verify(mockStatisticsCollector, times(1)).onRequestStart();
        verify(mockStatisticsCollector, times(1)).onRequestComplete(anyLong());
    }

    @Test
    @DisplayName("测试消费WebSocket消息")
    void testConsumeWebSocketMessage() {
        NetworkMessageEvent event = NetworkMessageEvent.builder()
                .eventId("test-002")
                .protocolType("WebSocket")
                .message("test message")
                .channelContext(mockContext)
                .build();
        
        assertDoesNotThrow(() -> consumer.consume(event, 1L, false));
        
        verify(mockStatisticsCollector, times(1)).onRequestStart();
    }

    @Test
    @DisplayName("测试消费无效消息")
    void testConsumeInvalidMessage() {
        NetworkMessageEvent event = new NetworkMessageEvent();
        
        assertDoesNotThrow(() -> consumer.consume(event, 1L, false));
        
        // 无效消息不应该调用统计
        verify(mockStatisticsCollector, never()).onRequestStart();
    }

    @Test
    @DisplayName("测试消费null消息")
    void testConsumeNullMessage() {
        assertDoesNotThrow(() -> consumer.consume(null, 1L, false));
        
        verify(mockStatisticsCollector, never()).onRequestStart();
    }

    @Test
    @DisplayName("测试错误处理")
    void testOnError() {
        Throwable error = new RuntimeException("test error");
        
        assertDoesNotThrow(() -> consumer.onError(error));
        
        verify(mockStatisticsCollector, times(1)).onRequestError();
    }
}

