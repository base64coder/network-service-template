package com.dtc.core.messaging;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

/**
 * NetworkMessageEvent 测试
 */
@DisplayName("网络消息事件测试")
public class NetworkMessageEventTest {

    private NetworkMessageEvent event;

    @BeforeEach
    void setUp() {
        event = new NetworkMessageEvent();
    }

    @Test
    @DisplayName("测试事件创建")
    void testEventCreation() {
        assertNotNull(event);
        // eventId默认为null，需要通过builder设置
    }

    @Test
    @DisplayName("测试Builder模式创建事件")
    void testBuilderPattern() {
        NetworkMessageEvent builtEvent = NetworkMessageEvent.builder()
                .eventId("test-001")
                .protocolType("HTTP")
                .message("test message")
                .build();
        
        assertNotNull(builtEvent);
        assertEquals("test-001", builtEvent.getEventId());
        assertEquals("HTTP", builtEvent.getProtocolType());
        assertEquals("test message", builtEvent.getMessage());
    }

    @Test
    @DisplayName("测试设置协议类型")
    void testSetProtocolType() {
        event.setProtocolType("HTTP");
        assertEquals("HTTP", event.getProtocolType());
    }

    @Test
    @DisplayName("测试设置消息内容")
    void testSetMessage() {
        Object message = "test message";
        event.setMessage(message);
        assertEquals(message, event.getMessage());
    }

    @Test
    @DisplayName("测试事件有效性")
    void testEventValidity() {
        // isValid()检查eventId、message和channelContext都不为null
        io.netty.channel.ChannelHandlerContext mockContext = 
            org.mockito.Mockito.mock(io.netty.channel.ChannelHandlerContext.class);
        
        NetworkMessageEvent validEvent = NetworkMessageEvent.builder()
                .eventId("test-001")
                .protocolType("HTTP")
                .message("test")
                .channelContext(mockContext)
                .build();
        
        assertTrue(validEvent.isValid(), 
            "Event should be valid with all required fields set");
    }

    @Test
    @DisplayName("测试清空事件")
    void testClearEvent() {
        event.setProtocolType("HTTP");
        event.setMessage("test");
        event.clear();
        assertFalse(event.isValid());
    }
}

