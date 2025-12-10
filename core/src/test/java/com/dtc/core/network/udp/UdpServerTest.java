package com.dtc.core.network.udp;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UdpServer 测试
 */
@DisplayName("UDP服务器测试")
public class UdpServerTest {

    private UdpServer udpServer;

    @BeforeEach
    void setUp() {
        udpServer = new UdpServer();
    }

    @Test
    @DisplayName("测试创建UDP服务器")
    void testCreateUdpServer() {
        assertNotNull(udpServer);
    }

    @Test
    @DisplayName("测试获取默认端口")
    void testGetDefaultPort() {
        int port = udpServer.getPort();
        assertTrue(port > 0);
    }

    @Test
    @DisplayName("测试设置端口")
    void testSetPort() {
        udpServer.setPort(5555);
        assertEquals(5555, udpServer.getPort());
    }

    @Test
    @DisplayName("测试获取主机地址")
    void testGetHost() {
        assertNotNull(udpServer.getHost());
        assertEquals("0.0.0.0", udpServer.getHost());
    }

    @Test
    @DisplayName("测试设置主机地址")
    void testSetHost() {
        udpServer.setHost("127.0.0.1");
        assertEquals("127.0.0.1", udpServer.getHost());
    }
}

