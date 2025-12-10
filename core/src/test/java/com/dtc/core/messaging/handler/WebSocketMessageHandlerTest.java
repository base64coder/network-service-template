package com.dtc.core.messaging.handler;

import com.dtc.core.messaging.NetworkMessageEvent;
import com.dtc.core.network.websocket.WebSocketConnectionManager;
import com.dtc.core.network.websocket.WebSocketMessageHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * WebSocketMessageHandler 测试
 */
@DisplayName("WebSocket消息处理器测试")
public class WebSocketMessageHandlerTest {

    @Mock
    private WebSocketConnectionManager mockConnectionManager;

    @Mock
    private WebSocketMessageHelper mockMessageHelper;

    private WebSocketMessageHandler handler;
    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        com.dtc.core.messaging.MessageHandlerRegistry mockRegistry = 
            mock(com.dtc.core.messaging.MessageHandlerRegistry.class);
        handler = new WebSocketMessageHandler(mockRegistry);
    }

    @Test
    @DisplayName("测试创建WebSocketMessageHandler")
    void testCreateHandler() {
        assertNotNull(handler);
    }

    @Test
    @DisplayName("测试处理WebSocket消息")
    void testHandleMessage() {
        NetworkMessageEvent event = NetworkMessageEvent.builder()
                .eventId("test-001")
                .protocolType("WebSocket")
                .message("test message")
                .build();
        
        assertDoesNotThrow(() -> handler.handleMessage(event));
    }

    @Test
    @DisplayName("测试处理空消息")
    void testHandleNullMessage() {
        // 处理null消息会抛出NullPointerException，这是预期的
        assertThrows(NullPointerException.class, 
            () -> handler.handleMessage(null));
    }
}

