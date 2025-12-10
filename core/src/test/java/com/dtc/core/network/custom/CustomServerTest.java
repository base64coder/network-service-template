package com.dtc.core.network.custom;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CustomServer 测试
 */
@DisplayName("自定义服务器测试")
public class CustomServerTest {

    private CustomServer customServer;

    @BeforeEach
    void setUp() {
        customServer = new CustomServer();
    }

    @Test
    @DisplayName("测试创建自定义服务器")
    void testCreateCustomServer() {
        assertNotNull(customServer);
    }

    @Test
    @DisplayName("测试获取默认端口")
    void testGetDefaultPort() {
        assertEquals(9999, customServer.getPort());
    }

    @Test
    @DisplayName("测试设置端口")
    void testSetPort() {
        customServer.setPort(7777);
        assertEquals(7777, customServer.getPort());
    }

    @Test
    @DisplayName("测试获取主机地址")
    void testGetHost() {
        assertNotNull(customServer.getHost());
        assertEquals("0.0.0.0", customServer.getHost());
    }

    @Test
    @DisplayName("测试设置主机地址")
    void testSetHost() {
        customServer.setHost("192.168.1.1");
        assertEquals("192.168.1.1", customServer.getHost());
    }

    @Test
    @DisplayName("测试获取协议名称")
    void testGetProtocolName() {
        assertNotNull(customServer.getProtocolName());
        assertEquals("CustomProtocol", customServer.getProtocolName());
    }

    @Test
    @DisplayName("测试设置协议名称")
    void testSetProtocolName() {
        customServer.setProtocolName("MyProtocol");
        assertEquals("MyProtocol", customServer.getProtocolName());
    }
}

