package com.dtc.core;

import com.dtc.core.bootstrap.config.ServerConfiguration;
import com.dtc.core.extensions.ExtensionManager;
import com.dtc.core.messaging.NetworkMessageHandler;
import com.dtc.core.messaging.NetworkMessageHandler.HandlerStats;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * NetworkService 茅聸聠忙聢聬忙碌聥猫炉聲
 */
public class NetworkServiceIntegrationTest {

    private NetworkService networkService;
    private ServerConfiguration config;

    @BeforeEach
    void setUp() {
        // 氓聢聸氓禄潞忙碌聥猫炉聲茅聟聧莽陆庐
        config = ServerConfiguration.builder()
                .serverName("Integration Test Server")
                .serverVersion("1.0.0")
                .dataFolder("test-data")
                .configFolder("test-conf")
                .extensionsFolder("test-extensions")
                .addListener("HTTP", 8080, "0.0.0.0", true, "HTTP API", "REST API忙聹聧氓聤隆莽芦炉氓聫拢")
                .addListener("WebSocket", 8081, "0.0.0.0", true, "WebSocket", "WebSocket猫驴聻忙聨楼莽芦炉氓聫拢")
                .addListener("TCP", 9999, "0.0.0.0", true, "TCP Server", "TCP忙聹聧氓聤隆氓聶篓莽芦炉氓聫?)
                .addListener("MQTT", 1883, "0.0.0.0", true, "MQTT Broker", "MQTT忙露聢忙聛炉盲禄拢莽聬聠莽芦炉氓聫拢")
                .build();

        networkService = new NetworkService(config);
    }

    @AfterEach
    void tearDown() {
        if (networkService != null && networkService.isStarted()) {
            try {
                networkService.stop().get(5, TimeUnit.SECONDS);
            } catch (Exception e) {
                // 氓驴陆莽聲楼氓聛聹忙颅垄忙聴露莽職聞氓录聜氓赂赂
            }
        }
    }

    @Test
    @DisplayName("忙碌聥猫炉聲忙聹聧氓聤隆氓聬炉氓聤篓氓聮聦氓聛聹忙颅?)
    @Timeout(30)
    void testServiceStartAndStop() throws Exception {
        // 忙碌聥猫炉聲氓聬炉氓聤篓
        CompletableFuture<Void> startFuture = networkService.start();
        assertNotNull(startFuture, "氓聬炉氓聤篓Future盲赂聧氓潞聰猫炉楼盲赂潞null");

        // 莽颅聣氓戮聟氓聬炉氓聤篓氓庐聦忙聢聬
        startFuture.get(10, TimeUnit.SECONDS);

        // 茅陋聦猫炉聛忙聹聧氓聤隆氓路虏氓聬炉氓聤?        assertTrue(networkService.isStarted(), "忙聹聧氓聤隆氓潞聰猫炉楼氓路虏氓聬炉氓聤?);
        assertFalse(networkService.isStopped(), "忙聹聧氓聤隆盲赂聧氓潞聰猫炉楼氓路虏氓聛聹忙颅垄");

        // 忙碌聥猫炉聲氓聛聹忙颅垄
        CompletableFuture<Void> stopFuture = networkService.stop();
        assertNotNull(stopFuture, "氓聛聹忙颅垄Future盲赂聧氓潞聰猫炉楼盲赂潞null");

        // 莽颅聣氓戮聟氓聛聹忙颅垄氓庐聦忙聢聬
        stopFuture.get(10, TimeUnit.SECONDS);

        // 茅陋聦猫炉聛忙聹聧氓聤隆氓路虏氓聛聹忙颅?        assertFalse(networkService.isStarted(), "忙聹聧氓聤隆氓潞聰猫炉楼氓路虏氓聛聹忙颅?);
        assertTrue(networkService.isStopped(), "忙聹聧氓聤隆氓潞聰猫炉楼氓路虏氓聛聹忙颅?);
    }

    @Test
    @DisplayName("忙碌聥猫炉聲忙露聢忙聛炉氓陇聞莽聬聠氓聶?)
    @Timeout(30)
    void testMessageHandler() throws Exception {
        // 氓聬炉氓聤篓忙聹聧氓聤隆
        networkService.start().get(10, TimeUnit.SECONDS);

        // 猫聨路氓聫聳忙露聢忙聛炉氓陇聞莽聬聠氓聶?        NetworkMessageHandler messageHandler = networkService.getMessageHandler();
        assertNotNull(messageHandler, "忙露聢忙聛炉氓陇聞莽聬聠氓聶篓盲赂聧氓潞聰猫炉楼盲赂潞null");

        // 忙碌聥猫炉聲氓陇聞莽聬聠氓聨聼氓搂聥忙聲掳忙聧庐
        byte[] testData = "integration test message".getBytes();
        boolean result = messageHandler.handleRawData(testData);
        assertTrue(result, "氓潞聰猫炉楼忙聢聬氓聤聼氓陇聞莽聬聠忙露聢忙聛炉");

        // 猫聨路氓聫聳莽禄聼猫庐隆盲驴隆忙聛炉
        HandlerStats stats = messageHandler.getStats();
        assertNotNull(stats, "莽禄聼猫庐隆盲驴隆忙聛炉盲赂聧氓潞聰猫炉楼盲赂潞null");
        assertTrue(stats.getReceivedCount() > 0, "氓潞聰猫炉楼忙聨楼忙聰露氓聢掳忙露聢忙聛?);
        assertTrue(stats.getForwardedCount() > 0, "氓潞聰猫炉楼猫陆卢氓聫聭忙露聢忙聛炉");
    }

    @Test
    @DisplayName("忙碌聥猫炉聲忙露聢忙聛炉莽禄聼猫庐隆")
    @Timeout(30)
    void testMessageStatistics() throws Exception {
        // 氓聬炉氓聤篓忙聹聧氓聤隆
        networkService.start().get(10, TimeUnit.SECONDS);

        // 猫聨路氓聫聳忙露聢忙聛炉氓陇聞莽聬聠氓聶?        NetworkMessageHandler messageHandler = networkService.getMessageHandler();

        // 氓陇聞莽聬聠氓陇職忙聺隆忙露聢忙聛炉
        int messageCount = 100;
        for (int i = 0; i < messageCount; i++) {
            byte[] testData = ("test message " + i).getBytes();
            messageHandler.handleRawData(testData);
        }

        // 莽颅聣氓戮聟氓陇聞莽聬聠氓庐聦忙聢聬
        Thread.sleep(1000);

        // 猫聨路氓聫聳莽禄聼猫庐隆盲驴隆忙聛炉
        HandlerStats stats = messageHandler.getStats();
        assertNotNull(stats, "莽禄聼猫庐隆盲驴隆忙聛炉盲赂聧氓潞聰猫炉楼盲赂潞null");
        assertTrue(stats.getReceivedCount() >= messageCount, "氓潞聰猫炉楼忙聨楼忙聰露氓聢掳忙聣聙忙聹聣忙露聢忙聛?);
        assertTrue(stats.getForwardedCount() >= messageCount, "氓潞聰猫炉楼猫陆卢氓聫聭忙聣聙忙聹聣忙露聢忙聛?);
    }

    @Test
    @DisplayName("忙碌聥猫炉聲氓鹿露氓聫聭忙露聢忙聛炉氓陇聞莽聬聠")
    @Timeout(30)
    void testConcurrentMessageHandling() throws Exception {
        // 氓聬炉氓聤篓忙聹聧氓聤隆
        networkService.start().get(10, TimeUnit.SECONDS);

        // 猫聨路氓聫聳忙露聢忙聛炉氓陇聞莽聬聠氓聶?        NetworkMessageHandler messageHandler = networkService.getMessageHandler();

        int threadCount = 10;
        int messagesPerThread = 100;
        Thread[] threads = new Thread[threadCount];

        // 氓聢聸氓禄潞氓陇職盲赂陋莽潞驴莽篓聥氓鹿露氓聫聭氓陇聞莽聬聠忙露聢忙聛炉
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < messagesPerThread; j++) {
                    byte[] testData = ("concurrent test message " + j).getBytes();
                    messageHandler.handleRawData(testData);
                }
            });
        }

        // 氓聬炉氓聤篓忙聣聙忙聹聣莽潞驴莽篓?        for (Thread thread : threads) {
            thread.start();
        }

        // 莽颅聣氓戮聟忙聣聙忙聹聣莽潞驴莽篓聥氓庐聦忙聢?        for (Thread thread : threads) {
            thread.join();
        }

        // 莽颅聣氓戮聟氓陇聞莽聬聠氓庐聦忙聢聬
        Thread.sleep(2000);

        // 猫聨路氓聫聳莽禄聼猫庐隆盲驴隆忙聛炉
        HandlerStats stats = messageHandler.getStats();
        assertNotNull(stats, "莽禄聼猫庐隆盲驴隆忙聛炉盲赂聧氓潞聰猫炉楼盲赂潞null");
        assertTrue(stats.getReceivedCount() >= threadCount * messagesPerThread,
                "氓潞聰猫炉楼忙聨楼忙聰露氓聢掳忙聣聙忙聹聣氓鹿露氓聫聭忙露聢忙聛?);
    }

    @Test
    @DisplayName("忙碌聥猫炉聲忙聹聧氓聤隆茅聟聧莽陆庐")
    void testServiceConfiguration() {
        // 茅陋聦猫炉聛茅聟聧莽陆庐
        assertNotNull(config, "茅聟聧莽陆庐盲赂聧氓潞聰猫炉楼盲赂潞null");
        assertEquals("Integration Test Server", config.getServerName());
        assertEquals("1.0.0", config.getServerVersion());
        assertEquals("test-data", config.getDataFolder());
        assertEquals("test-conf", config.getConfigFolder());
        assertEquals("test-extensions", config.getExtensionsFolder());

        // 茅陋聦猫炉聛莽聸聭氓聬卢氓聶篓茅聟聧莽陆?        assertNotNull(config.getListeners());
        assertTrue(config.getListeners().size() >= 4, "氓潞聰猫炉楼茅聟聧莽陆庐猫聡鲁氓掳聭4盲赂陋莽聸聭氓聬卢氓聶篓");
    }

    @Test
    @DisplayName("忙碌聥猫炉聲忙聣漏氓卤聲莽庐隆莽聬聠氓聶?)
    @Timeout(30)
    void testExtensionManager() throws Exception {
        // 氓聬炉氓聤篓忙聹聧氓聤隆
        networkService.start().get(10, TimeUnit.SECONDS);

        // 猫聨路氓聫聳忙聣漏氓卤聲莽庐隆莽聬聠氓聶?        ExtensionManager extensionManager = networkService.getExtensionManager();
        assertNotNull(extensionManager, "忙聣漏氓卤聲莽庐隆莽聬聠氓聶篓盲赂聧氓潞聰猫炉楼盲赂潞null");

        // 茅陋聦猫炉聛忙聣漏氓卤聲莽庐隆莽聬聠氓聶篓氓聤聼猫聝?        assertNotNull(extensionManager.getAllExtensions());
    }

    @Test
    @DisplayName("忙碌聥猫炉聲盲戮聺猫碌聳忙鲁篓氓聟楼氓庐鹿氓聶篓")
    @Timeout(30)
    void testInjector() throws Exception {
        // 氓聬炉氓聤篓忙聹聧氓聤隆
        networkService.start().get(10, TimeUnit.SECONDS);

        // 猫聨路氓聫聳盲戮聺猫碌聳忙鲁篓氓聟楼氓庐鹿氓聶篓
        com.google.inject.Injector injector = networkService.getInjector();
        assertNotNull(injector, "盲戮聺猫碌聳忙鲁篓氓聟楼氓庐鹿氓聶篓盲赂聧氓潞聰猫炉楼盲赂潞null");

        // 茅陋聦猫炉聛氓聫炉盲禄楼猫聨路氓聫聳忙聽赂氓驴聝莽禄聞盲禄露
        NetworkMessageHandler messageHandler = injector.getInstance(NetworkMessageHandler.class);
        assertNotNull(messageHandler, "氓潞聰猫炉楼猫聝陆氓陇聼猫聨路氓聫聳忙露聢忙聛炉氓陇聞莽聬聠氓聶?);
    }

    @Test
    @DisplayName("忙碌聥猫炉聲忙聹聧氓聤隆茅聡聧氓聬炉")
    @Timeout(60)
    void testServiceRestart() throws Exception {
        // 莽卢卢盲赂聙忙卢隆氓聬炉氓聤?        networkService.start().get(10, TimeUnit.SECONDS);
        assertTrue(networkService.isStarted(), "忙聹聧氓聤隆氓潞聰猫炉楼氓路虏氓聬炉氓聤?);

        // 氓聛聹忙颅垄忙聹聧氓聤隆
        networkService.stop().get(10, TimeUnit.SECONDS);
        assertTrue(networkService.isStopped(), "忙聹聧氓聤隆氓潞聰猫炉楼氓路虏氓聛聹忙颅?);

        // 茅聡聧忙聳掳氓聬炉氓聤篓忙聹聧氓聤隆
        networkService.start().get(10, TimeUnit.SECONDS);
        assertTrue(networkService.isStarted(), "忙聹聧氓聤隆氓潞聰猫炉楼茅聡聧忙聳掳氓聬炉氓聤篓");

        // 氓聠聧忙卢隆氓聛聹忙颅垄忙聹聧氓聤隆
        networkService.stop().get(10, TimeUnit.SECONDS);
        assertTrue(networkService.isStopped(), "忙聹聧氓聤隆氓潞聰猫炉楼氓聠聧忙卢隆氓聛聹忙颅垄");
    }

    @Test
    @DisplayName("忙碌聥猫炉聲忙聙搂猫聝陆")
    @Timeout(30)
    void testPerformance() throws Exception {
        // 氓聬炉氓聤篓忙聹聧氓聤隆
        networkService.start().get(10, TimeUnit.SECONDS);

        // 猫聨路氓聫聳忙露聢忙聛炉氓陇聞莽聬聠氓聶?        NetworkMessageHandler messageHandler = networkService.getMessageHandler();

        int messageCount = 10000;
        long startTime = System.currentTimeMillis();

        // 氓陇聞莽聬聠氓陇搂茅聡聫忙露聢忙聛炉
        for (int i = 0; i < messageCount; i++) {
            byte[] testData = ("performance test message " + i).getBytes();
            messageHandler.handleRawData(testData);
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // 茅陋聦猫炉聛忙聙搂猫聝陆茂录聢氓潞聰猫炉楼猫聝陆氓聹?莽搂聮氓聠聟氓陇聞莽聬聠10000忙聺隆忙露聢忙聛炉茂录聣
        assertTrue(duration < 2000, "氓陇聞莽聬聠10000忙聺隆忙露聢忙聛炉氓潞聰猫炉楼氓聹篓2莽搂聮氓聠聟氓庐聦忙聢聬");

        // 莽颅聣氓戮聟氓陇聞莽聬聠氓庐聦忙聢聬
        Thread.sleep(1000);

        // 猫聨路氓聫聳莽禄聼猫庐隆盲驴隆忙聛炉
        HandlerStats stats = messageHandler.getStats();
        assertNotNull(stats, "莽禄聼猫庐隆盲驴隆忙聛炉盲赂聧氓潞聰猫炉楼盲赂潞null");
        assertTrue(stats.getReceivedCount() >= messageCount, "氓潞聰猫炉楼忙聨楼忙聰露氓聢掳忙聣聙忙聹聣忙露聢忙聛?);
    }

    @Test
    @DisplayName("忙碌聥猫炉聲茅聰聶猫炉炉氓陇聞莽聬聠")
    @Timeout(30)
    void testErrorHandling() throws Exception {
        // 氓聬炉氓聤篓忙聹聧氓聤隆
        networkService.start().get(10, TimeUnit.SECONDS);

        // 猫聨路氓聫聳忙露聢忙聛炉氓陇聞莽聬聠氓聶?        NetworkMessageHandler messageHandler = networkService.getMessageHandler();

        // 忙碌聥猫炉聲氓陇聞莽聬聠莽漏潞忙聲掳忙聧?        byte[] emptyData = new byte[0];
        boolean result = messageHandler.handleRawData(emptyData);
        assertTrue(result, "氓潞聰猫炉楼猫聝陆氓陇聞莽聬聠莽漏潞忙聲掳忙聧庐");

        // 忙碌聥猫炉聲氓陇聞莽聬聠氓陇搂忙聲掳忙聧?        byte[] largeData = new byte[1024 * 1024]; // 1MB
        for (int i = 0; i < largeData.length; i++) {
            largeData[i] = (byte) (i % 256);
        }
        result = messageHandler.handleRawData(largeData);
        assertTrue(result, "氓潞聰猫炉楼猫聝陆氓陇聞莽聬聠氓陇搂忙聲掳忙聧庐");
    }
}
