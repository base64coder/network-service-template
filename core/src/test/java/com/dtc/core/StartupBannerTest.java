package com.dtc.core;

 param($m) 'import com.dtc.core.bootstrap.launcher.' + $m.Groups[1].Value ;
import com.dtc.core.bootstrap.config.ServerConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

/**
 * éîå§©å¦¯îç®å¨´å¬­ç¯
 */
public class StartupBannerTest {

    @Test
    @DisplayName("å¨´å¬­ç¯éîå§©å¦¯îç®éå§ã")
    void testDisplayBanner() {
        // éæ¶ç¼å¨´å¬­ç¯é°å¶ç
        ServerConfiguration config = ServerConfiguration.builder().serverName("å¨´å¬­ç¯éå¶å§é£?).serverVersion("1.0.0")
                .serverId("test-server-001").addListener("HTTP", 8080, "0.0.0.0", true, "HTTP API", "REST API éå¶å§ç»îå½")
                .addListener("WebSocket", 8081, "0.0.0.0", true, "WebSocket", "WebSocket æ©ç´å¸´ç»îå½")
                .addListener("TCP", 9999, "0.0.0.0", true, "TCP Server", "TCP éå¶å§é£ã§î¬é?)
                .addListener("MQTT", 1883, "0.0.0.0", false, "MQTT Broker", "MQTT å¨å ä¼æµ ï½æç»îå½").build();

        // éæ¶ç¼éîå§©å¦¯îç®
        StartupBanner banner = new StartupBanner(config);

        // éå§ãå¦¯îç®
        System.out.println("=== å¨´å¬­ç¯éîå§©å¦¯îç®éå§ã ===");
        banner.displayBanner();
        banner.displayServerInfo();
        banner.displaySystemInfo();
        banner.displayStartupComplete();
        System.out.println("=== å¨´å¬­ç¯ç¹å±¾å ===");
    }
}
