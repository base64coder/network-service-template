package com.dtc.core.messaging.handler;

import com.dtc.core.messaging.NetworkMessageEvent;
import com.dtc.core.messaging.MessageHandlerRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * TcpMessageHandler 测试
 */
@DisplayName("TCP消息处理器测试")
public class TcpMessageHandlerTest {

    @Mock
    private MessageHandlerRegistry mockRegistry;

    private TcpMessageHandler handler;
    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        handler = new TcpMessageHandler(mockRegistry);
    }

    @Test
    @DisplayName("测试创建TcpMessageHandler")
    void testCreateHandler() {
        assertNotNull(handler);
    }

    @Test
    @DisplayName("测试处理TCP消息")
    void testHandleMessage() {
        NetworkMessageEvent event = NetworkMessageEvent.builder()
                .eventId("test-001")
                .protocolType("TCP")
                .message("test message")
                .build();
        
        assertDoesNotThrow(() -> handler.handleMessage(event));
    }

    @Test
    @DisplayName("测试处理null消息")
    void testHandleNullMessage() {
        assertThrows(NullPointerException.class, 
            () -> handler.handleMessage(null));
    }
}

