package com.dtc.core.bootstrap;

import com.dtc.api.ServiceConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 服务配置测试
 * 
 * @author Network Service Template
 */
public class ServiceConfigTest {

    private ServiceConfigManager configManager;

    @BeforeEach
    void setUp() {
        configManager = new ServiceConfigManager();
    }

    @Test
    @DisplayName("测试服务配置初始化")
    void testServiceConfigInitialization() {
        // 验证所有服务配置都已初始化
        assertEquals(5, configManager.getAllServiceConfigs().size());

        // 验证每个服务都有配置
        assertNotNull(configManager.getServiceConfig("HTTP"));
        assertNotNull(configManager.getServiceConfig("WebSocket"));
        assertNotNull(configManager.getServiceConfig("MQTT"));
        assertNotNull(configManager.getServiceConfig("TCP"));
        assertNotNull(configManager.getServiceConfig("CustomProtocol"));
    }

    @Test
    @DisplayName("测试端口配置")
    void testPortConfiguration() {
        // 验证端口配置
        assertEquals(8080, ServiceConfig.HTTP.getDefaultPort());
        assertEquals(8081, ServiceConfig.WEBSOCKET.getDefaultPort());
        assertEquals(1883, ServiceConfig.MQTT.getDefaultPort());
        assertEquals(9999, ServiceConfig.TCP.getDefaultPort());
        assertEquals(9998, ServiceConfig.CUSTOM.getDefaultPort());
    }

    @Test
    @DisplayName("测试启动优先级")
    void testStartupPriority() {
        // 验证启动优先级（数字越小优先级越高）
        assertTrue(ServiceConfig.HTTP.getStartupPriority() < ServiceConfig.WEBSOCKET.getStartupPriority());
        assertTrue(ServiceConfig.WEBSOCKET.getStartupPriority() < ServiceConfig.MQTT.getStartupPriority());
        assertTrue(ServiceConfig.MQTT.getStartupPriority() < ServiceConfig.TCP.getStartupPriority());
        assertTrue(ServiceConfig.TCP.getStartupPriority() < ServiceConfig.CUSTOM.getStartupPriority());
    }

    @Test
    @DisplayName("测试服务优先级")
    void testServicePriority() {
        // 验证服务优先级（数字越大优先级越高）
        assertTrue(ServiceConfig.HTTP.getServicePriority() > ServiceConfig.WEBSOCKET.getServicePriority());
        assertTrue(ServiceConfig.WEBSOCKET.getServicePriority() > ServiceConfig.MQTT.getServicePriority());
        assertTrue(ServiceConfig.MQTT.getServicePriority() > ServiceConfig.TCP.getServicePriority());
        assertTrue(ServiceConfig.TCP.getServicePriority() > ServiceConfig.CUSTOM.getServicePriority());
    }

    @Test
    @DisplayName("测试启动顺序")
    void testStartupOrder() {
        var startupOrder = configManager.getStartupOrder();

        // 验证启动顺序
        assertEquals("HTTP", startupOrder.get(0).getServiceId());
        assertEquals("WebSocket", startupOrder.get(1).getServiceId());
        assertEquals("MQTT", startupOrder.get(2).getServiceId());
        assertEquals("TCP", startupOrder.get(3).getServiceId());
        assertEquals("CustomProtocol", startupOrder.get(4).getServiceId());
    }

    @Test
    @DisplayName("测试服务优先级顺序")
    void testServicePriorityOrder() {
        var priorityOrder = configManager.getPriorityOrder();

        // 验证服务优先级顺序
        assertEquals("HTTP", priorityOrder.get(0).getServiceId());
        assertEquals("WebSocket", priorityOrder.get(1).getServiceId());
        assertEquals("MQTT", priorityOrder.get(2).getServiceId());
        assertEquals("TCP", priorityOrder.get(3).getServiceId());
        assertEquals("CustomProtocol", priorityOrder.get(4).getServiceId());
    }

    @Test
    @DisplayName("测试端口冲突检查")
    void testPortConflictCheck() {
        // 验证没有端口冲突
        assertFalse(configManager.hasPortConflicts());
        assertNull(configManager.getPortConflictInfo());
    }

    @Test
    @DisplayName("测试服务状态管理")
    void testServiceStatusManagement() {
        // 初始状态：所有服务都未启动
        assertFalse(configManager.isServiceStarted("HTTP"));
        assertFalse(configManager.isServiceStarted("WebSocket"));

        // 标记服务为已启动
        configManager.setServiceStartupStatus("HTTP", true);
        configManager.setServiceStartupStatus("WebSocket", true);

        // 验证状态
        assertTrue(configManager.isServiceStarted("HTTP"));
        assertTrue(configManager.isServiceStarted("WebSocket"));
        assertFalse(configManager.isServiceStarted("MQTT"));

        // 验证已启动服务列表
        var startedServices = configManager.getStartedServices();
        assertEquals(2, startedServices.size());
        assertTrue(startedServices.stream().anyMatch(s -> s.getServiceId().equals("HTTP")));
        assertTrue(startedServices.stream().anyMatch(s -> s.getServiceId().equals("WebSocket")));
    }

    @Test
    @DisplayName("测试服务配置查找")
    void testServiceConfigLookup() {
        // 测试根据服务ID查找
        ServiceConfig httpConfig = ServiceConfig.getByServiceId("HTTP");
        assertNotNull(httpConfig);
        assertEquals("HTTP", httpConfig.getServiceId());
        assertEquals("http", httpConfig.getServiceName());

        // 测试根据端口查找
        ServiceConfig portConfig = ServiceConfig.getByPort(8080);
        assertNotNull(portConfig);
        assertEquals("HTTP", portConfig.getServiceId());

        // 测试查找不存在的服务
        assertNull(ServiceConfig.getByServiceId("NONEXISTENT"));
        assertNull(ServiceConfig.getByPort(99999));
    }

    @Test
    @DisplayName("测试服务配置摘要")
    void testServiceConfigSummary() {
        String summary = configManager.getServiceConfigSummary();
        assertNotNull(summary);
        assertTrue(summary.contains("HTTP"));
        assertTrue(summary.contains("WebSocket"));
        assertTrue(summary.contains("MQTT"));
        assertTrue(summary.contains("TCP"));
        assertTrue(summary.contains("CustomProtocol"));
    }

    @Test
    @DisplayName("测试服务统计信息")
    void testServiceStatistics() {
        String stats = configManager.getServiceStatistics();
        assertNotNull(stats);
        assertTrue(stats.contains("总服务数: 5"));
        assertTrue(stats.contains("已启动: 0"));
        assertTrue(stats.contains("未启动: 5"));
        assertTrue(stats.contains("启动率: 0.0%"));
    }

    @Test
    @DisplayName("测试服务配置重置")
    void testServiceConfigReset() {
        // 设置一些服务状态
        configManager.setServiceStartupStatus("HTTP", true);
        configManager.setServiceStartupStatus("WebSocket", true);

        // 验证状态
        assertTrue(configManager.isServiceStarted("HTTP"));
        assertTrue(configManager.isServiceStarted("WebSocket"));

        // 重置状态
        configManager.resetAllServiceStatus();

        // 验证所有服务都未启动
        assertFalse(configManager.isServiceStarted("HTTP"));
        assertFalse(configManager.isServiceStarted("WebSocket"));
        assertFalse(configManager.isServiceStarted("MQTT"));
        assertFalse(configManager.isServiceStarted("TCP"));
        assertFalse(configManager.isServiceStarted("CustomProtocol"));
    }
}
