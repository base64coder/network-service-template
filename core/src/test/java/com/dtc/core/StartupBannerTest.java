package com.dtc.core;

import com.dtc.core.bootstrap.launcher.StartupBanner;
import com.dtc.core.bootstrap.config.ServerConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

/**
 * 启动横幅测试
 */
public class StartupBannerTest {

    @Test
    @DisplayName("测试启动横幅显示")
    void testDisplayBanner() {
        // 创建测试配置
        ServerConfiguration config = ServerConfiguration.builder()
                .serverName("测试服务器")
                .serverVersion("1.0.0")
                .serverId("test-server-001")
                .addListener("HTTP", 8080, "0.0.0.0", true, "HTTP API", "REST API 服务端口")
                .addListener("WebSocket", 8081, "0.0.0.0", true, "WebSocket", "WebSocket 协议端口")
                .addListener("TCP", 9999, "0.0.0.0", true, "TCP Server", "TCP 服务器端口")
                .addListener("MQTT", 1883, "0.0.0.0", false, "MQTT Broker", "MQTT 消息代理端口")
                .build();

        // 创建启动横幅
        StartupBanner banner = new StartupBanner(config);

        // 显示横幅
        System.out.println("=== 测试启动横幅显示 ===");
        banner.displayBanner();
        banner.displayServerInfo();
        banner.displaySystemInfo();
        banner.displayStartupComplete();
        System.out.println("=== 测试完成 ===");
    }
}
