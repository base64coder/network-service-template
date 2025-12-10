package com.dtc.core.network.tcp;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TcpServer 测试
 */
@DisplayName("TCP服务器测试")
public class TcpServerTest {

    private TcpServer tcpServer;

    @BeforeEach
    void setUp() {
        tcpServer = new TcpServer();
    }

    @Test
    @DisplayName("测试创建TCP服务器")
    void testCreateTcpServer() {
        assertNotNull(tcpServer);
    }

    @Test
    @DisplayName("测试获取默认端口")
    void testGetDefaultPort() {
        assertEquals(9999, tcpServer.getPort());
    }

    @Test
    @DisplayName("测试设置端口")
    void testSetPort() {
        tcpServer.setPort(8888);
        assertEquals(8888, tcpServer.getPort());
    }

    @Test
    @DisplayName("测试获取主机地址")
    void testGetHost() {
        assertNotNull(tcpServer.getHost());
        assertEquals("0.0.0.0", tcpServer.getHost());
    }

    @Test
    @DisplayName("测试设置主机地址")
    void testSetHost() {
        tcpServer.setHost("127.0.0.1");
        assertEquals("127.0.0.1", tcpServer.getHost());
    }
}

