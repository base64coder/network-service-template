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
        assertNotNull(event.getEventId());
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
        event.setProtocolType("HTTP");
        event.setMessage("test");
        assertTrue(event.isValid());
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

