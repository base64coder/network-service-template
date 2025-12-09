package com.dtc.core.bootstrap.launcher;

import com.dtc.api.annotations.NotNull;
import com.dtc.core.bootstrap.config.ListenerConfiguration;
import com.dtc.core.bootstrap.config.ServerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 服务器状态显示器
 * 定期显示服务器状态信息和监控数据
 * 
 * @author Network Service Template
 */
public class ServerStatusDisplay {

    private static final Logger log = LoggerFactory.getLogger(ServerStatusDisplay.class);
    private final @NotNull ServerConfiguration configuration;
    private final @NotNull ScheduledExecutorService scheduler;
    private volatile boolean running = false;

    public ServerStatusDisplay(@NotNull ServerConfiguration configuration) {
        this.configuration = configuration;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "ServerStatusDisplay");
            t.setDaemon(true);
            return t;
        });
    }

    /**
     * 启动状态显示
     */
    public void startStatusDisplay() {
        if (running) {
            return;
        }

        running = true;

        // 立即显示一次状态
        displayStatus();

        // 每30秒显示一次状态
        scheduler.scheduleAtFixedRate(this::displayStatus, 30, 30, TimeUnit.SECONDS);

        log.info("📊 服务器状态显示器已启动 - 每30秒更新一次");
    }

    /**
     * 停止状态显示
     */
    public void stopStatusDisplay() {
        if (!running) {
            return;
        }

        running = false;
        scheduler.shutdown();

        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }

        log.info("📊 服务器状态显示器已停止");
    }

    /**
     * 显示服务器状态
     */
    private void displayStatus() {
        if (!running) {
            return;
        }

        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        Runtime runtime = Runtime.getRuntime();

        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        double memoryUsagePercent = (double) usedMemory / maxMemory * 100;

        System.out.println("\n" + repeatString("📊", 20));
        System.out.println("📈 服务器运行状态");
        System.out.println(repeatString("📊", 20));
        System.out.printf("⏰ 当前时间: %s%n", currentTime);
        System.out.printf("📦 服务器: %s v%s%n", configuration.getServerName(), configuration.getServerVersion());
        System.out.printf("🆔 服务器ID: %s%n", configuration.getServerId());
        System.out.printf("💾 内存使用: %s / %s (%.1f%%)%n", formatBytes(usedMemory), formatBytes(maxMemory),
                memoryUsagePercent);
        System.out.printf("🆓 可用内存: %s%n", formatBytes(freeMemory));
        System.out.printf("🧵 活动线程: %d%n", Thread.activeCount());
        System.out.printf("🔧 运行模式: %s%n", configuration.isEmbedded() ? "嵌入式" : "独立服务器");

        // 显示监听器状态
        displayListenersStatus();

        System.out.println(repeatString("📊", 20));

        log.debug("服务器状态 - 内存: {}/{} ({}%), 线程: {}", formatBytes(usedMemory), formatBytes(maxMemory),
                String.format("%.1f", memoryUsagePercent), Thread.activeCount());
    }

    /**
     * 显示监听器状态
     */
    private void displayListenersStatus() {
        List<ListenerConfiguration> listeners = configuration.getListeners();
        if (listeners.isEmpty()) {
            System.out.println("🔌 监听器: 暂无配置的监听器");
            return;
        }

        System.out.println("🔌 监听器状态:");
        for (ListenerConfiguration listener : listeners) {
            String status = listener.isEnabled() ? "✅ 运行中" : "❌ 已停止";
            System.out.printf("  📡 %s:%d (%s) %s%n", listener.getBindAddress(), listener.getPort(), listener.getType(),
                    status);
        }
    }

    /**
     * 显示启动横幅和信息
     */
    public void displayStartupBanner() {
        System.out.println("\n" + repeatString("🚀", 25));
        System.out.println("🌟 Network Service Template");
        System.out.println("🔌 基于扩展的网络服务框架");
        System.out.println(repeatString("🚀", 25));
    }

    /**
     * 显示关闭信息
     */
    public void displayShutdownInfo() {
        String shutdownTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        System.out.println("\n" + repeatString("🛑", 20));
        System.out.println("⏹️  服务器正在关闭...");
        System.out.printf("⏰ 关闭时间: %s%n", shutdownTime);
        System.out.printf("📦 服务器: %s v%s%n", configuration.getServerName(), configuration.getServerVersion());
        System.out.println(repeatString("🛑", 20));

        log.info("服务器关闭 - 时间: {}, 服务器: {} v{}", shutdownTime, configuration.getServerName(),
                configuration.getServerVersion());
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

    /**
     * 是否正在运行
     */
    public boolean isRunning() {
        return running;
    }
}
