package com.dtc.core;

import com.dtc.core.config.ServerConfiguration;
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
 * NetworkService 集成测试
 */
public class NetworkServiceIntegrationTest {

    private NetworkService networkService;
    private ServerConfiguration config;

    @BeforeEach
    void setUp() {
        // 创建测试配置
        config = ServerConfiguration.builder()
                .serverName("Integration Test Server")
                .serverVersion("1.0.0")
                .dataFolder("test-data")
                .configFolder("test-conf")
                .extensionsFolder("test-extensions")
                .addListener("HTTP", 8080, "0.0.0.0", true, "HTTP API", "REST API服务端口")
                .addListener("WebSocket", 8081, "0.0.0.0", true, "WebSocket", "WebSocket连接端口")
                .addListener("TCP", 9999, "0.0.0.0", true, "TCP Server", "TCP服务器端口")
                .addListener("MQTT", 1883, "0.0.0.0", true, "MQTT Broker", "MQTT消息代理端口")
                .build();

        networkService = new NetworkService(config);
    }

    @AfterEach
    void tearDown() {
        if (networkService != null && networkService.isStarted()) {
            try {
                networkService.stop().get(5, TimeUnit.SECONDS);
            } catch (Exception e) {
                // 忽略停止时的异常
            }
        }
    }

    @Test
    @DisplayName("测试服务启动和停止")
    @Timeout(30)
    void testServiceStartAndStop() throws Exception {
        // 测试启动
        CompletableFuture<Void> startFuture = networkService.start();
        assertNotNull(startFuture, "启动Future不应该为null");

        // 等待启动完成
        startFuture.get(10, TimeUnit.SECONDS);

        // 验证服务已启动
        assertTrue(networkService.isStarted(), "服务应该已启动");
        assertFalse(networkService.isStopped(), "服务不应该已停止");

        // 测试停止
        CompletableFuture<Void> stopFuture = networkService.stop();
        assertNotNull(stopFuture, "停止Future不应该为null");

        // 等待停止完成
        stopFuture.get(10, TimeUnit.SECONDS);

        // 验证服务已停止
        assertFalse(networkService.isStarted(), "服务应该已停止");
        assertTrue(networkService.isStopped(), "服务应该已停止");
    }

    @Test
    @DisplayName("测试消息处理器")
    @Timeout(30)
    void testMessageHandler() throws Exception {
        // 启动服务
        networkService.start().get(10, TimeUnit.SECONDS);

        // 获取消息处理器
        NetworkMessageHandler messageHandler = networkService.getMessageHandler();
        assertNotNull(messageHandler, "消息处理器不应该为null");

        // 测试处理原始数据
        byte[] testData = "integration test message".getBytes();
        boolean result = messageHandler.handleRawData(testData);
        assertTrue(result, "应该成功处理消息");

        // 获取统计信息
        HandlerStats stats = messageHandler.getStats();
        assertNotNull(stats, "统计信息不应该为null");
        assertTrue(stats.getReceivedCount() > 0, "应该接收到消息");
        assertTrue(stats.getForwardedCount() > 0, "应该转发消息");
    }

    @Test
    @DisplayName("测试消息统计")
    @Timeout(30)
    void testMessageStatistics() throws Exception {
        // 启动服务
        networkService.start().get(10, TimeUnit.SECONDS);

        // 获取消息处理器
        NetworkMessageHandler messageHandler = networkService.getMessageHandler();

        // 处理多条消息
        int messageCount = 100;
        for (int i = 0; i < messageCount; i++) {
            byte[] testData = ("test message " + i).getBytes();
            messageHandler.handleRawData(testData);
        }

        // 等待处理完成
        Thread.sleep(1000);

        // 获取统计信息
        HandlerStats stats = messageHandler.getStats();
        assertNotNull(stats, "统计信息不应该为null");
        assertTrue(stats.getReceivedCount() >= messageCount, "应该接收到所有消息");
        assertTrue(stats.getForwardedCount() >= messageCount, "应该转发所有消息");
    }

    @Test
    @DisplayName("测试并发消息处理")
    @Timeout(30)
    void testConcurrentMessageHandling() throws Exception {
        // 启动服务
        networkService.start().get(10, TimeUnit.SECONDS);

        // 获取消息处理器
        NetworkMessageHandler messageHandler = networkService.getMessageHandler();

        int threadCount = 10;
        int messagesPerThread = 100;
        Thread[] threads = new Thread[threadCount];

        // 创建多个线程并发处理消息
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < messagesPerThread; j++) {
                    byte[] testData = ("concurrent test message " + j).getBytes();
                    messageHandler.handleRawData(testData);
                }
            });
        }

        // 启动所有线程
        for (Thread thread : threads) {
            thread.start();
        }

        // 等待所有线程完成
        for (Thread thread : threads) {
            thread.join();
        }

        // 等待处理完成
        Thread.sleep(2000);

        // 获取统计信息
        HandlerStats stats = messageHandler.getStats();
        assertNotNull(stats, "统计信息不应该为null");
        assertTrue(stats.getReceivedCount() >= threadCount * messagesPerThread,
                "应该接收到所有并发消息");
    }

    @Test
    @DisplayName("测试服务配置")
    void testServiceConfiguration() {
        // 验证配置
        assertNotNull(config, "配置不应该为null");
        assertEquals("Integration Test Server", config.getServerName());
        assertEquals("1.0.0", config.getServerVersion());
        assertEquals("test-data", config.getDataFolder());
        assertEquals("test-conf", config.getConfigFolder());
        assertEquals("test-extensions", config.getExtensionsFolder());

        // 验证监听器配置
        assertNotNull(config.getListeners());
        assertTrue(config.getListeners().size() >= 4, "应该配置至少4个监听器");
    }

    @Test
    @DisplayName("测试扩展管理器")
    @Timeout(30)
    void testExtensionManager() throws Exception {
        // 启动服务
        networkService.start().get(10, TimeUnit.SECONDS);

        // 获取扩展管理器
        ExtensionManager extensionManager = networkService.getExtensionManager();
        assertNotNull(extensionManager, "扩展管理器不应该为null");

        // 验证扩展管理器功能
        assertNotNull(extensionManager.getAllExtensions());
    }

    @Test
    @DisplayName("测试依赖注入容器")
    @Timeout(30)
    void testInjector() throws Exception {
        // 启动服务
        networkService.start().get(10, TimeUnit.SECONDS);

        // 获取依赖注入容器
        com.google.inject.Injector injector = networkService.getInjector();
        assertNotNull(injector, "依赖注入容器不应该为null");

        // 验证可以获取核心组件
        NetworkMessageHandler messageHandler = injector.getInstance(NetworkMessageHandler.class);
        assertNotNull(messageHandler, "应该能够获取消息处理器");
    }

    @Test
    @DisplayName("测试服务重启")
    @Timeout(60)
    void testServiceRestart() throws Exception {
        // 第一次启动
        networkService.start().get(10, TimeUnit.SECONDS);
        assertTrue(networkService.isStarted(), "服务应该已启动");

        // 停止服务
        networkService.stop().get(10, TimeUnit.SECONDS);
        assertTrue(networkService.isStopped(), "服务应该已停止");

        // 重新启动服务
        networkService.start().get(10, TimeUnit.SECONDS);
        assertTrue(networkService.isStarted(), "服务应该重新启动");

        // 再次停止服务
        networkService.stop().get(10, TimeUnit.SECONDS);
        assertTrue(networkService.isStopped(), "服务应该再次停止");
    }

    @Test
    @DisplayName("测试性能")
    @Timeout(30)
    void testPerformance() throws Exception {
        // 启动服务
        networkService.start().get(10, TimeUnit.SECONDS);

        // 获取消息处理器
        NetworkMessageHandler messageHandler = networkService.getMessageHandler();

        int messageCount = 10000;
        long startTime = System.currentTimeMillis();

        // 处理大量消息
        for (int i = 0; i < messageCount; i++) {
            byte[] testData = ("performance test message " + i).getBytes();
            messageHandler.handleRawData(testData);
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // 验证性能（应该能在2秒内处理10000条消息）
        assertTrue(duration < 2000, "处理10000条消息应该在2秒内完成");

        // 等待处理完成
        Thread.sleep(1000);

        // 获取统计信息
        HandlerStats stats = messageHandler.getStats();
        assertNotNull(stats, "统计信息不应该为null");
        assertTrue(stats.getReceivedCount() >= messageCount, "应该接收到所有消息");
    }

    @Test
    @DisplayName("测试错误处理")
    @Timeout(30)
    void testErrorHandling() throws Exception {
        // 启动服务
        networkService.start().get(10, TimeUnit.SECONDS);

        // 获取消息处理器
        NetworkMessageHandler messageHandler = networkService.getMessageHandler();

        // 测试处理空数据
        byte[] emptyData = new byte[0];
        boolean result = messageHandler.handleRawData(emptyData);
        assertTrue(result, "应该能处理空数据");

        // 测试处理大数据
        byte[] largeData = new byte[1024 * 1024]; // 1MB
        for (int i = 0; i < largeData.length; i++) {
            largeData[i] = (byte) (i % 256);
        }
        result = messageHandler.handleRawData(largeData);
        assertTrue(result, "应该能处理大数据");
    }
}
