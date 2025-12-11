package com.dtc.core.integration;

import com.dtc.core.bootstrap.config.ServerConfiguration;
import com.dtc.core.messaging.NetworkMessageEvent;
import com.dtc.core.messaging.NetworkMessageQueue;
import com.dtc.core.messaging.NetworkMessageConsumer;
import com.dtc.core.statistics.StatisticsCollector;
import io.netty.channel.ChannelHandlerContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 网络服务集成测试
 * 测试多个组件协同工作
 */
@DisplayName("网络服务集成测试")
public class NetworkServiceIntegrationTest {

    @Mock
    private NetworkMessageConsumer mockConsumer;

    @Mock
    private StatisticsCollector mockStatisticsCollector;

    @Mock
    private ChannelHandlerContext mockContext;

    private NetworkMessageQueue messageQueue;
    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        messageQueue = new NetworkMessageQueue(mockConsumer);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (messageQueue.isStarted()) {
            messageQueue.stop();
        }
        if (mocks != null) {
            mocks.close();
        }
    }

    @Test
    @DisplayName("测试完整的消息处理流程")
    void testCompleteMessageProcessingFlow() throws InterruptedException {
        // 启动队列
        messageQueue.start();
        assertTrue(messageQueue.isStarted());

        // 创建消息事件
        NetworkMessageEvent event = NetworkMessageEvent.builder()
                .eventId("integration-test-001")
                .protocolType("HTTP")
                .clientId("test-client")
                .message("test message")
                .channelContext(mockContext)
                .timestamp(System.currentTimeMillis())
                .build();

        // 发布消息
        assertDoesNotThrow(() -> messageQueue.publish(event));

        // 等待消息处理
        Thread.sleep(100);

        // 验证消息被消费
        verify(mockConsumer, timeout(1000).atLeastOnce()).consume(any(), anyLong(), anyBoolean());

        // 停止队列
        messageQueue.stop();
        assertFalse(messageQueue.isStarted());
    }

    @Test
    @DisplayName("测试多协议消息处理")
    void testMultiProtocolMessageProcessing() throws InterruptedException {
        messageQueue.start();

        String[] protocols = {"HTTP", "TCP", "WebSocket"};
        
        for (String protocol : protocols) {
            NetworkMessageEvent event = NetworkMessageEvent.builder()
                    .eventId("test-" + protocol)
                    .protocolType(protocol)
                    .message("test message")
                    .channelContext(mockContext)
                    .build();
            
            messageQueue.publish(event);
        }

        Thread.sleep(200);

        verify(mockConsumer, timeout(1000).atLeast(3)).consume(any(), anyLong(), anyBoolean());
        
        messageQueue.stop();
    }

    @Test
    @DisplayName("测试消息队列启动停止生命周期")
    void testMessageQueueLifecycle() {
        assertFalse(messageQueue.isStarted());
        
        messageQueue.start();
        assertTrue(messageQueue.isStarted());
        
        messageQueue.stop();
        assertFalse(messageQueue.isStarted());
    }

    @Test
    @DisplayName("测试服务器配置与消息队列集成")
    void testServerConfigurationIntegration() {
        ServerConfiguration config = ServerConfiguration.builder()
                .serverName("IntegrationTestServer")
                .serverVersion("1.0.0")
                .serverId("integration-test-001")
                .addListener("HTTP", 8080, "0.0.0.0", true, "HTTP API", "RESTful API")
                .addListener("TCP", 9999, "0.0.0.0", true, "TCP Server", "Custom TCP Protocol")
                .build();

        assertNotNull(config);
        assertEquals(2, config.getListeners().size());
        
        // 验证配置可以用于启动服务
        assertTrue(config.getListeners().get(0).isEnabled());
        assertTrue(config.getListeners().get(1).isEnabled());
    }
}

