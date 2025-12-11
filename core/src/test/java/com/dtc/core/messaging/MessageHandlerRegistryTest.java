package com.dtc.core.messaging;

import com.dtc.core.messaging.handler.HttpMessageHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * MessageHandlerRegistry 测试
 */
@DisplayName("消息处理器注册表测试")
public class MessageHandlerRegistryTest {

    @Mock
    private HttpMessageHandler mockHandler;

    private MessageHandlerRegistry registry;
    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        registry = new MessageHandlerRegistry();
    }

    @Test
    @DisplayName("测试创建注册表")
    void testCreateRegistry() {
        assertNotNull(registry);
    }
}

