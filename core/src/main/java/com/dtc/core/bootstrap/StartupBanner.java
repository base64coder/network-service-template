package com.dtc.core.bootstrap;

import com.dtc.api.annotations.NotNull;
import com.dtc.core.config.ListenerConfiguration;
import com.dtc.core.config.ServerConfiguration;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 启动横幅显示器 负责显示服务器启动时的欢迎信息和配置详情
 * 
 * @author Network Service Template
 */
public class StartupBanner {

    private static final Logger log = LoggerFactory.getLogger(StartupBanner.class);
    private static final String BANNER = ""
            + "╔══════════════════════════════════════════════════════════════╗\n"
            + "║                    Network Service Template                  ║\n"
            + "║                    可扩展的网络服务框架                         ║\n"
            + "╚══════════════════════════════════════════════════════════════╝\n";

    private final @NotNull ServerConfiguration configuration;

    @Inject
    public StartupBanner(@NotNull ServerConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * 显示启动横幅
     */
    public void displayBanner() {
        System.out.println(BANNER);
        log.info("Network Service Template - 可扩展的网络服务框架");
    }

    /**
     * 显示服务器信息
     */
    public void displayServerInfo() {
        String startTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        System.out.println("\n" + repeatString("=", 60));
        System.out.println("🚀 服务器启动信息");
        System.out.println(repeatString("=", 60));
        System.out.printf("📛 服务器名称: %s%n", configuration.getServerName());
        System.out.printf("🔢 服务器版本: %s%n", configuration.getServerVersion());
        System.out.printf("🆔 服务器ID: %s%n", configuration.getServerId());
        System.out.printf("⏰ 启动时间: %s%n", startTime);
        System.out.printf("📁 数据目录: %s%n", configuration.getDataFolder().toAbsolutePath());
        System.out.printf("⚙️  配置目录: %s%n", configuration.getConfigFolder().toAbsolutePath());
        System.out.printf("🔌 扩展目录: %s%n", configuration.getExtensionsFolder().toAbsolutePath());
        System.out.printf("🏠 运行模式: %s%n", configuration.isEmbedded() ? "嵌入式" : "独立服务");

        // 显示监听器信息
        displayListenersInfo();

        System.out.println(repeatString("=", 60));

        log.info("服务器信息 - 名称: {}, 版本: {}, ID: {}", configuration.getServerName(), configuration.getServerVersion(),
                configuration.getServerId());
    }

    /**
     * 显示系统信息
     */
    public void displaySystemInfo() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;

        System.out.println("\n" + repeatString("=", 60));
        System.out.println("💻 系统信息");
        System.out.println(repeatString("=", 60));
        System.out.printf("☕ Java版本: %s%n", System.getProperty("java.version"));
        System.out.printf("🏗️  Java厂商: %s%n", System.getProperty("java.vendor"));
        System.out.printf("💾 最大内存: %s%n", formatBytes(maxMemory));
        System.out.printf("📊 已用内存: %s%n", formatBytes(usedMemory));
        System.out.printf("🆓 可用内存: %s%n", formatBytes(freeMemory));
        System.out.printf("🖥️  操作系统: %s %s%n", System.getProperty("os.name"), System.getProperty("os.version"));
        System.out.printf("🏠 用户目录: %s%n", System.getProperty("user.home"));
        System.out.printf("📂 工作目录: %s%n", System.getProperty("user.dir"));
        System.out.println(repeatString("=", 60));

        log.info("系统信息 - Java: {}, 内存: {}/{}", System.getProperty("java.version"), formatBytes(usedMemory),
                formatBytes(maxMemory));
    }

    /**
     * 显示监听器信息
     */
    private void displayListenersInfo() {
        List<ListenerConfiguration> listeners = configuration.getListeners();
        if (listeners.isEmpty()) {
            System.out.println("🔌 监听器: 无配置的监听器");
            return;
        }

        System.out.println("🔌 网络监听器:");
        for (ListenerConfiguration listener : listeners) {
            String status = listener.isEnabled() ? "✅ 启用" : "❌ 禁用";
            String description = listener.getDescription() != null ? " - " + listener.getDescription() : "";
            System.out.printf("  📡 %s (%s) - %s:%d %s%s%n", listener.getName(), listener.getType(),
                    listener.getBindAddress(), listener.getPort(), status, description);
        }
    }

    /**
     * 显示环境变量
     */
    public void displayEnvironmentInfo() {
        Map<String, String> envVars = configuration.getEnvironmentVariables();
        Map<String, String> sysProps = configuration.getSystemProperties();

        if (!envVars.isEmpty() || !sysProps.isEmpty()) {
            System.out.println("\n" + repeatString("=", 60));
            System.out.println("🌍 环境配置");
            System.out.println(repeatString("=", 60));

            if (!envVars.isEmpty()) {
                System.out.println("环境变量:");
                envVars.forEach((key, value) -> System.out.printf("  %s = %s%n", key, value));
            }

            if (!sysProps.isEmpty()) {
                System.out.println("系统属性:");
                sysProps.forEach((key, value) -> System.out.printf("  %s = %s%n", key, value));
            }

            System.out.println(repeatString("=", 60));
        }
    }

    /**
     * 显示启动完成信息
     */
    public void displayStartupComplete() {
        System.out.println("\n" + repeatString("🎉", 20));
        System.out.println("✅ 服务器启动完成！");
        System.out.println("🌐 网络服务已就绪，等待连接...");
        System.out.println("📝 查看日志文件获取详细信息");
        System.out.println("🛑 按 Ctrl+C 停止服务器");
        System.out.println(repeatString("🎉", 20));

        log.info("服务器启动完成 - 所有服务已就绪");
    }

    /**
     * 重复字符串（兼容 Java 8）
     */
    private String repeatString(String str, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
        return sb.toString();
    }

    /**
     * 格式化字节数
     */
    private String formatBytes(long bytes) {
        if (bytes < 1024)
            return bytes + " B";
        if (bytes < 1024 * 1024)
            return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024)
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }
}
