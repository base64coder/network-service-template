package com.dtc.core.network.websocket;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

/**
 * WebSocketServer 测试
 */
@DisplayName("WebSocket服务器测试")
public class WebSocketServerTest {

    private WebSocketServer webSocketServer;

    @BeforeEach
    void setUp() {
        webSocketServer = new WebSocketServer();
    }

    @Test
    @DisplayName("测试创建WebSocket服务器")
    void testCreateWebSocketServer() {
        assertNotNull(webSocketServer);
    }

    @Test
    @DisplayName("测试获取默认端口")
    void testGetDefaultPort() {
        assertEquals(8081, webSocketServer.getPort());
    }

    @Test
    @DisplayName("测试设置端口")
    void testSetPort() {
        webSocketServer.setPort(8082);
        assertEquals(8082, webSocketServer.getPort());
    }

    @Test
    @DisplayName("测试获取主机地址")
    void testGetHost() {
        assertNotNull(webSocketServer.getHost());
        assertEquals("0.0.0.0", webSocketServer.getHost());
    }

    @Test
    @DisplayName("测试设置主机地址")
    void testSetHost() {
        webSocketServer.setHost("localhost");
        assertEquals("localhost", webSocketServer.getHost());
    }

    @Test
    @DisplayName("测试获取WebSocket路径")
    void testGetPath() {
        assertNotNull(webSocketServer.getPath());
        assertEquals("/websocket", webSocketServer.getPath());
    }

    @Test
    @DisplayName("测试设置WebSocket路径")
    void testSetPath() {
        webSocketServer.setPath("/ws");
        assertEquals("/ws", webSocketServer.getPath());
    }
}

