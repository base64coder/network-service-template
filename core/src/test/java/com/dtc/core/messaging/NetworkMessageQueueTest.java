package com.dtc.core.messaging;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * NetworkMessageQueue 测试
 */
@DisplayName("网络消息队列测试")
public class NetworkMessageQueueTest {

    @Mock
    private NetworkMessageConsumer mockConsumer;

    private NetworkMessageQueue queue;
    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        queue = new NetworkMessageQueue(mockConsumer);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (queue != null) {
            queue.stop();
        }
        if (mocks != null) {
            mocks.close();
        }
    }

    @Test
    @DisplayName("测试队列启动")
    void testQueueStart() {
        assertDoesNotThrow(() -> queue.start());
    }

    @Test
    @DisplayName("测试队列停止")
    void testQueueStop() {
        queue.start();
        assertDoesNotThrow(() -> queue.stop());
    }

    @Test
    @DisplayName("测试发布事件")
    void testPublishEvent() {
        queue.start();
        
        NetworkMessageEvent event = NetworkMessageEvent.builder()
                .eventId("test-001")
                .protocolType("HTTP")
                .message("test message")
                .build();
        
        assertDoesNotThrow(() -> queue.publish(event));
    }

    @Test
    @DisplayName("测试获取支持的协议")
    void testGetSupportedProtocols() {
        assertNotNull(queue.getSupportedProtocols());
        assertFalse(queue.getSupportedProtocols().isEmpty());
        assertTrue(queue.getSupportedProtocols().contains("HTTP"));
    }

    @Test
    @DisplayName("测试协议支持检查")
    void testSupportsProtocol() {
        assertTrue(queue.supportsProtocol("HTTP"));
        assertTrue(queue.supportsProtocol("WebSocket"));
        assertTrue(queue.supportsProtocol("MQTT"));
    }
}

