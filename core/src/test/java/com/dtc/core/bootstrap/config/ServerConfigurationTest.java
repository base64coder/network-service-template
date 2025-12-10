package com.dtc.core.bootstrap.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ServerConfiguration 测试
 */
@DisplayName("服务器配置测试")
public class ServerConfigurationTest {

    @Test
    @DisplayName("测试Builder模式创建配置")
    void testBuilderPattern() {
        ServerConfiguration config = ServerConfiguration.builder()
                .serverName("测试服务器")
                .serverVersion("1.0.0")
                .serverId("test-001")
                .build();

        assertNotNull(config);
        assertEquals("测试服务器", config.getServerName());
        assertEquals("1.0.0", config.getServerVersion());
        assertEquals("test-001", config.getServerId());
    }

    @Test
    @DisplayName("测试添加监听器")
    void testAddListener() {
        ServerConfiguration config = ServerConfiguration.builder()
                .serverName("测试服务器")
                .addListener("HTTP", 8080, "0.0.0.0", true, "HTTP", "HTTP服务")
                .build();

        assertNotNull(config);
        assertNotNull(config.getListeners());
        assertFalse(config.getListeners().isEmpty());
    }

    @Test
    @DisplayName("测试默认值")
    void testDefaultValues() {
        ServerConfiguration config = ServerConfiguration.builder()
                .build();

        assertNotNull(config);
        assertNotNull(config.getListeners());
    }
}

