package com.dtc.core.messaging.handler;

import com.dtc.core.messaging.NetworkMessageEvent;
import com.dtc.core.network.http.HttpRequestHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * HttpMessageHandler 测试
 */
@DisplayName("HTTP消息处理器测试")
public class HttpMessageHandlerTest {

    @Mock
    private HttpRequestHandler mockRequestHandler;

    private HttpMessageHandler httpMessageHandler;
    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        httpMessageHandler = new HttpMessageHandler(mockRequestHandler);
    }

    @Test
    @DisplayName("测试创建HttpMessageHandler")
    void testCreateHandler() {
        assertNotNull(httpMessageHandler);
    }

    @Test
    @DisplayName("测试处理HTTP消息事件")
    void testHandleMessage() {
        NetworkMessageEvent event = NetworkMessageEvent.builder()
                .eventId("test-001")
                .protocolType("HTTP")
                .message("test message")
                .build();
        
        assertDoesNotThrow(() -> httpMessageHandler.handleMessage(event));
    }

    @Test
    @DisplayName("测试处理空消息事件")
    void testHandleNullMessage() {
        // 处理null消息会抛出NullPointerException，这是预期的
        assertThrows(NullPointerException.class, 
            () -> httpMessageHandler.handleMessage(null));
    }

    @Test
    @DisplayName("测试处理无效消息")
    void testHandleInvalidMessage() {
        NetworkMessageEvent event = new NetworkMessageEvent();
        
        assertDoesNotThrow(() -> httpMessageHandler.handleMessage(event));
    }
}

