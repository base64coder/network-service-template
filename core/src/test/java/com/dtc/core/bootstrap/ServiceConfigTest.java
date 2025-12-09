package com.dtc.core.bootstrap;

import com.dtc.api.ServiceConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 忙聹聧氓聤隆茅聟聧莽陆庐忙碌聥猫炉聲
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
    @DisplayName("忙碌聥猫炉聲忙聹聧氓聤隆茅聟聧莽陆庐氓聢聺氓搂聥氓聦?)
    void testServiceConfigInitialization() {
        // 茅陋聦猫炉聛忙聣聙忙聹聣忙聹聧氓聤隆茅聟聧莽陆庐茅聝陆氓路虏氓聢聺氓搂聥氓聦聳
        assertEquals(5, configManager.getAllServiceConfigs().size());

        // 茅陋聦猫炉聛忙炉聫盲赂陋忙聹聧氓聤隆茅聝陆忙聹聣茅聟聧莽陆庐
        assertNotNull(configManager.getServiceConfig("HTTP"));
        assertNotNull(configManager.getServiceConfig("WebSocket"));
        assertNotNull(configManager.getServiceConfig("MQTT"));
        assertNotNull(configManager.getServiceConfig("TCP"));
        assertNotNull(configManager.getServiceConfig("CustomProtocol"));
    }

    @Test
    @DisplayName("忙碌聥猫炉聲莽芦炉氓聫拢茅聟聧莽陆庐")
    void testPortConfiguration() {
        // 茅陋聦猫炉聛莽芦炉氓聫拢茅聟聧莽陆庐
        assertEquals(8080, ServiceConfig.HTTP.getDefaultPort());
        assertEquals(8081, ServiceConfig.WEBSOCKET.getDefaultPort());
        assertEquals(1883, ServiceConfig.MQTT.getDefaultPort());
        assertEquals(9999, ServiceConfig.TCP.getDefaultPort());
        assertEquals(9998, ServiceConfig.CUSTOM.getDefaultPort());
    }

    @Test
    @DisplayName("忙碌聥猫炉聲氓聬炉氓聤篓盲录聵氓聟聢莽潞?)
    void testStartupPriority() {
        // 茅陋聦猫炉聛氓聬炉氓聤篓盲录聵氓聟聢莽潞搂茂录聢忙聲掳氓颅聴猫露聤氓掳聫盲录聵氓聟聢莽潞搂猫露聤茅芦聵茂录聣
        assertTrue(ServiceConfig.HTTP.getStartupPriority() < ServiceConfig.WEBSOCKET.getStartupPriority());
        assertTrue(ServiceConfig.WEBSOCKET.getStartupPriority() < ServiceConfig.MQTT.getStartupPriority());
        assertTrue(ServiceConfig.MQTT.getStartupPriority() < ServiceConfig.TCP.getStartupPriority());
        assertTrue(ServiceConfig.TCP.getStartupPriority() < ServiceConfig.CUSTOM.getStartupPriority());
    }

    @Test
    @DisplayName("忙碌聥猫炉聲忙聹聧氓聤隆盲录聵氓聟聢莽潞?)
    void testServicePriority() {
        // 茅陋聦猫炉聛忙聹聧氓聤隆盲录聵氓聟聢莽潞搂茂录聢忙聲掳氓颅聴猫露聤氓陇搂盲录聵氓聟聢莽潞搂猫露聤茅芦聵茂录聣
        assertTrue(ServiceConfig.HTTP.getServicePriority() > ServiceConfig.WEBSOCKET.getServicePriority());
        assertTrue(ServiceConfig.WEBSOCKET.getServicePriority() > ServiceConfig.MQTT.getServicePriority());
        assertTrue(ServiceConfig.MQTT.getServicePriority() > ServiceConfig.TCP.getServicePriority());
        assertTrue(ServiceConfig.TCP.getServicePriority() > ServiceConfig.CUSTOM.getServicePriority());
    }

    @Test
    @DisplayName("忙碌聥猫炉聲氓聬炉氓聤篓茅隆潞氓潞聫")
    void testStartupOrder() {
        var startupOrder = configManager.getStartupOrder();

        // 茅陋聦猫炉聛氓聬炉氓聤篓茅隆潞氓潞聫
        assertEquals("HTTP", startupOrder.get(0).getServiceId());
        assertEquals("WebSocket", startupOrder.get(1).getServiceId());
        assertEquals("MQTT", startupOrder.get(2).getServiceId());
        assertEquals("TCP", startupOrder.get(3).getServiceId());
        assertEquals("CustomProtocol", startupOrder.get(4).getServiceId());
    }

    @Test
    @DisplayName("忙碌聥猫炉聲忙聹聧氓聤隆盲录聵氓聟聢莽潞搂茅隆潞氓潞?)
    void testServicePriorityOrder() {
        var priorityOrder = configManager.getPriorityOrder();

        // 茅陋聦猫炉聛忙聹聧氓聤隆盲录聵氓聟聢莽潞搂茅隆潞氓潞?        assertEquals("HTTP", priorityOrder.get(0).getServiceId());
        assertEquals("WebSocket", priorityOrder.get(1).getServiceId());
        assertEquals("MQTT", priorityOrder.get(2).getServiceId());
        assertEquals("TCP", priorityOrder.get(3).getServiceId());
        assertEquals("CustomProtocol", priorityOrder.get(4).getServiceId());
    }

    @Test
    @DisplayName("忙碌聥猫炉聲莽芦炉氓聫拢氓聠虏莽陋聛忙拢聙忙聼?)
    void testPortConflictCheck() {
        // 茅陋聦猫炉聛忙虏隆忙聹聣莽芦炉氓聫拢氓聠虏莽陋聛
        assertFalse(configManager.hasPortConflicts());
        assertNull(configManager.getPortConflictInfo());
    }

    @Test
    @DisplayName("忙碌聥猫炉聲忙聹聧氓聤隆莽聤露忙聙聛莽庐隆莽聬?)
    void testServiceStatusManagement() {
        // 氓聢聺氓搂聥莽聤露忙聙聛茂录職忙聣聙忙聹聣忙聹聧氓聤隆茅聝陆忙聹陋氓聬炉氓聤?        assertFalse(configManager.isServiceStarted("HTTP"));
        assertFalse(configManager.isServiceStarted("WebSocket"));

        // 忙聽聡猫庐掳忙聹聧氓聤隆盲赂潞氓路虏氓聬炉氓聤篓
        configManager.setServiceStartupStatus("HTTP", true);
        configManager.setServiceStartupStatus("WebSocket", true);

        // 茅陋聦猫炉聛莽聤露忙聙?        assertTrue(configManager.isServiceStarted("HTTP"));
        assertTrue(configManager.isServiceStarted("WebSocket"));
        assertFalse(configManager.isServiceStarted("MQTT"));

        // 茅陋聦猫炉聛氓路虏氓聬炉氓聤篓忙聹聧氓聤隆氓聢聴猫隆?        var startedServices = configManager.getStartedServices();
        assertEquals(2, startedServices.size());
        assertTrue(startedServices.stream().anyMatch(s -> s.getServiceId().equals("HTTP")));
        assertTrue(startedServices.stream().anyMatch(s -> s.getServiceId().equals("WebSocket")));
    }

    @Test
    @DisplayName("忙碌聥猫炉聲忙聹聧氓聤隆茅聟聧莽陆庐忙聼楼忙聣戮")
    void testServiceConfigLookup() {
        // 忙碌聥猫炉聲忙聽鹿忙聧庐忙聹聧氓聤隆ID忙聼楼忙聣戮
        ServiceConfig httpConfig = ServiceConfig.getByServiceId("HTTP");
        assertNotNull(httpConfig);
        assertEquals("HTTP", httpConfig.getServiceId());
        assertEquals("http", httpConfig.getServiceName());

        // 忙碌聥猫炉聲忙聽鹿忙聧庐莽芦炉氓聫拢忙聼楼忙聣戮
        ServiceConfig portConfig = ServiceConfig.getByPort(8080);
        assertNotNull(portConfig);
        assertEquals("HTTP", portConfig.getServiceId());

        // 忙碌聥猫炉聲忙聼楼忙聣戮盲赂聧氓颅聵氓聹篓莽職聞忙聹聧氓聤隆
        assertNull(ServiceConfig.getByServiceId("NONEXISTENT"));
        assertNull(ServiceConfig.getByPort(99999));
    }

    @Test
    @DisplayName("忙碌聥猫炉聲忙聹聧氓聤隆茅聟聧莽陆庐忙聭聵猫娄聛")
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
    @DisplayName("忙碌聥猫炉聲忙聹聧氓聤隆莽禄聼猫庐隆盲驴隆忙聛炉")
    void testServiceStatistics() {
        String stats = configManager.getServiceStatistics();
        assertNotNull(stats);
        assertTrue(stats.contains("忙聙禄忙聹聧氓聤隆忙聲掳: 5"));
        assertTrue(stats.contains("氓路虏氓聬炉氓聤? 0"));
        assertTrue(stats.contains("忙聹陋氓聬炉氓聤? 5"));
        assertTrue(stats.contains("氓聬炉氓聤篓莽聨? 0.0%"));
    }

    @Test
    @DisplayName("忙碌聥猫炉聲忙聹聧氓聤隆茅聟聧莽陆庐茅聡聧莽陆庐")
    void testServiceConfigReset() {
        // 猫庐戮莽陆庐盲赂聙盲潞聸忙聹聧氓聤隆莽聤露忙聙?        configManager.setServiceStartupStatus("HTTP", true);
        configManager.setServiceStartupStatus("WebSocket", true);

        // 茅陋聦猫炉聛莽聤露忙聙?        assertTrue(configManager.isServiceStarted("HTTP"));
        assertTrue(configManager.isServiceStarted("WebSocket"));

        // 茅聡聧莽陆庐莽聤露忙聙?        configManager.resetAllServiceStatus();

        // 茅陋聦猫炉聛忙聣聙忙聹聣忙聹聧氓聤隆茅聝陆忙聹陋氓聬炉氓聤?        assertFalse(configManager.isServiceStarted("HTTP"));
        assertFalse(configManager.isServiceStarted("WebSocket"));
        assertFalse(configManager.isServiceStarted("MQTT"));
        assertFalse(configManager.isServiceStarted("TCP"));
        assertFalse(configManager.isServiceStarted("CustomProtocol"));
    }
}
