package com.dtc.core.network.mqtt;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MqttServer 测试
 */
@DisplayName("MQTT服务器测试")
public class MqttServerTest {

    private MqttServer mqttServer;

    @BeforeEach
    void setUp() {
        mqttServer = new MqttServer();
    }

    @Test
    @DisplayName("测试创建MQTT服务器")
    void testCreateMqttServer() {
        assertNotNull(mqttServer);
    }

    @Test
    @DisplayName("测试获取默认端口")
    void testGetDefaultPort() {
        assertEquals(1883, mqttServer.getPort());
    }

    @Test
    @DisplayName("测试设置端口")
    void testSetPort() {
        mqttServer.setPort(1884);
        assertEquals(1884, mqttServer.getPort());
    }

    @Test
    @DisplayName("测试获取主机地址")
    void testGetHost() {
        assertNotNull(mqttServer.getHost());
        assertEquals("0.0.0.0", mqttServer.getHost());
    }

    @Test
    @DisplayName("测试设置主机地址")
    void testSetHost() {
        mqttServer.setHost("localhost");
        assertEquals("localhost", mqttServer.getHost());
    }
}

