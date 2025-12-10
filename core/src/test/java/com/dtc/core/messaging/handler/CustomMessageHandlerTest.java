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
 * CustomMessageHandler 测试
 */
@DisplayName("自定义消息处理器测试")
public class CustomMessageHandlerTest {

    @Mock
    private MessageHandlerRegistry mockRegistry;

    private CustomMessageHandler handler;
    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        handler = new CustomMessageHandler(mockRegistry);
    }

    @Test
    @DisplayName("测试创建CustomMessageHandler")
    void testCreateHandler() {
        assertNotNull(handler);
    }

    @Test
    @DisplayName("测试处理自定义消息")
    void testHandleMessage() {
        NetworkMessageEvent event = NetworkMessageEvent.builder()
                .eventId("test-001")
                .protocolType("Custom")
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

