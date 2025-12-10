package com.dtc.core.extensions;

import com.dtc.api.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 扩展关闭管理器
 * 提供管理扩展关闭的策略和优雅关闭流程
 * 
 * @author Network Service Template
 */
@Singleton
public class ExtensionShutdownManager {

    private static final Logger log = LoggerFactory.getLogger(ExtensionShutdownManager.class);
    private static final long DEFAULT_SHUTDOWN_TIMEOUT_MS = 30000; // 30秒默认超时
    private final ExtensionManager extensionManager;

    @Inject
    public ExtensionShutdownManager(@NotNull ExtensionManager extensionManager) {
        this.extensionManager = extensionManager;
    }

    /**
     * 优雅关闭所有扩展
     * 
     * @return 关闭完成的Future
     */
    @NotNull
    public CompletableFuture<Void> gracefulShutdownAll() {
        return gracefulShutdownAll(DEFAULT_SHUTDOWN_TIMEOUT_MS);
    }

    /**
     * 优雅关闭所有扩展
     * 
     * @param timeoutMs 超时时间（毫秒）
     * @return 关闭完成的Future
     */
    @NotNull
    public CompletableFuture<Void> gracefulShutdownAll(long timeoutMs) {
        log.info("Starting graceful shutdown of all extensions with timeout: {}ms", timeoutMs);

        return CompletableFuture.runAsync(() -> {
            try {
                // 获取所有扩展
                List<NetworkExtension> extensions = extensionManager.getAllExtensions().values().stream()
                        .collect(java.util.stream.Collectors.toList());
                log.info("Found {} extensions to shutdown", extensions.size());

                if (extensions.isEmpty()) {
                    log.info("No extensions to shutdown");
                    return;
                }

                // 按优先级排序，优先级高的先关闭
                extensions.sort((e1, e2) -> Integer.compare(e2.getPriority(), e1.getPriority()));

                // 逐个关闭所有扩展
                AtomicInteger completedCount = new AtomicInteger(0);
                @SuppressWarnings("unchecked")
                CompletableFuture<Void>[] shutdownFutures = extensions.stream()
                        .map(extension -> gracefulShutdownExtension(extension, timeoutMs)
                                .thenRun(() -> {
                                    int completed = completedCount.incrementAndGet();
                                    log.info("Extension shutdown completed: {}/{}", completed, extensions.size());
                                }))
                        .toArray(CompletableFuture[]::new);

                // 等待所有扩展关闭完成
                CompletableFuture.allOf(shutdownFutures).get(timeoutMs, TimeUnit.MILLISECONDS);

                log.info("All extensions shutdown gracefully completed");

            } catch (Exception e) {
                log.error("Error during graceful shutdown of extensions", e);
                throw new RuntimeException("Failed to shutdown extensions gracefully", e);
            }
        });
    }

    /**
     * 优雅关闭单个扩展
     * 
     * @param extension 要关闭的扩展
     * @param timeoutMs 超时时间（毫秒）
     * @return 关闭完成的Future
     */
    @NotNull
    public CompletableFuture<Void> gracefulShutdownExtension(@NotNull NetworkExtension extension, long timeoutMs) {
        String extensionId = extension.getId();
        log.info("Starting graceful shutdown of extension: {}", extensionId);

        return CompletableFuture.runAsync(() -> {
            try {
                // 步骤1: 准备关闭
                if (extension instanceof GracefulShutdownExtension) {
                    GracefulShutdownExtension gracefulExtension = (GracefulShutdownExtension) extension;
                    gracefulExtension.prepareForShutdown();
                    log.debug("Extension {} prepared for shutdown", extensionId);
                }

                // 步骤2: 停止接受新请求
                stopAcceptingNewRequests(extension);

                // 步骤3: 等待待处理的请求完成
                waitForPendingRequests(extension, timeoutMs);

                // 步骤4: 关闭活动连接
                closeActiveConnections(extension);

                // 步骤5: 停止扩展
                extensionManager.stopExtension(extensionId).join();

                log.info("Extension {} shutdown gracefully completed", extensionId);

            } catch (Exception e) {
                log.error("Failed to shutdown extension {} gracefully", extensionId, e);
                // 强制停止扩展
                try {
                    extensionManager.stopExtension(extensionId).join();
                } catch (Exception forceStopException) {
                    log.error("Failed to force stop extension {}", extensionId, forceStopException);
                }
                throw new RuntimeException("Failed to shutdown extension: " + extensionId, e);
            }
        });
    }

    /**
     * 停止接受新请求
     * 
     * @param extension 扩展
     */
    private void stopAcceptingNewRequests(@NotNull NetworkExtension extension) {
        try {
            String extensionId = extension.getId();
            log.debug("Stopping new request acceptance for extension: {}", extensionId);

            // 设置扩展为禁用状态
            extension.setEnabled(false);

            // 根据扩展类型执行相应的停止操作
            if (extensionId.contains("http")) {
                // HTTP 扩展需要移除路由
                log.debug("Removing HTTP routes for extension: {}", extensionId);
            } else if (extensionId.contains("tcp")) {
                // TCP 扩展需要关闭端口
                log.debug("Closing TCP port for extension: {}", extensionId);
            } else if (extensionId.contains("websocket")) {
                // WebSocket 扩展需要关闭端口
                log.debug("Closing WebSocket port for extension: {}", extensionId);
            } else if (extensionId.contains("mqtt")) {
                // MQTT 扩展需要关闭端口
                log.debug("Closing MQTT port for extension: {}", extensionId);
            }

            log.debug("New request acceptance stopped for extension: {}", extensionId);
        } catch (Exception e) {
            log.warn("Failed to stop new request acceptance for extension: {}", extension.getId(), e);
        }
    }

    /**
     * 等待待处理的请求完成
     * 
     * @param extension 扩展
     * @param timeoutMs 超时时间
     */
    private void waitForPendingRequests(@NotNull NetworkExtension extension, long timeoutMs) {
        try {
            String extensionId = extension.getId();
            log.debug("Waiting for pending requests to complete for extension: {}", extensionId);

            // 检查扩展是否支持统计功能，如果支持则使用StatisticsAware接口
            if (extension instanceof com.dtc.core.statistics.StatisticsAware) {
                com.dtc.core.statistics.StatisticsAware statsExtension = (com.dtc.core.statistics.StatisticsAware) extension;
                long pendingRequests = statsExtension.getPendingRequestCount();

                if (pendingRequests == 0) {
                    log.debug("No pending requests for extension: {}", extensionId);
                    return;
                }

                log.info("Found {} pending requests for extension: {}", pendingRequests, extensionId);

                // 等待请求完成
                long startTime = System.currentTimeMillis();
                while (pendingRequests > 0 && (System.currentTimeMillis() - startTime) < timeoutMs) {
                    Thread.sleep(100);
                    pendingRequests = statsExtension.getPendingRequestCount();

                    if (pendingRequests > 0) {
                        log.debug("Still {} pending requests for extension: {}", pendingRequests, extensionId);
                    }
                }

                if (pendingRequests > 0) {
                    log.warn("Timeout waiting for pending requests for extension: {}, {} requests still pending",
                            extensionId, pendingRequests);
                } else {
                    log.info("All pending requests completed for extension: {}", extensionId);
                }
            }
        } catch (Exception e) {
            log.warn("Error waiting for pending requests for extension: {}", extension.getId(), e);
        }
    }

    /**
     * 关闭活动连接
     * 
     * @param extension 扩展
     */
    private void closeActiveConnections(@NotNull NetworkExtension extension) {
        try {
            String extensionId = extension.getId();
            log.debug("Closing active connections for extension: {}", extensionId);

            // 根据扩展类型执行相应的连接关闭操作
            // 使用反射调用优雅关闭连接的方法
            try {
                if (extensionId.contains("tcp")) {
                    // 尝试调用 TCP 扩展的连接关闭方法
                    java.lang.reflect.Method method = extension.getClass().getMethod("gracefulCloseAllConnections");
                    method.invoke(extension);
                } else if (extensionId.contains("websocket")) {
                    // 尝试调用 WebSocket 扩展的连接关闭方法
                    java.lang.reflect.Method method = extension.getClass().getMethod("gracefulCloseAllConnections");
                    method.invoke(extension);
                } else if (extensionId.contains("custom")) {
                    // 尝试调用 Custom 扩展的连接关闭方法
                    java.lang.reflect.Method method = extension.getClass().getMethod("gracefulCloseAllConnections");
                    method.invoke(extension);
                }
            } catch (Exception e) {
                log.debug("Extension {} does not support graceful connection closing: {}", extensionId, e.getMessage());
            }

            log.debug("Active connections closed for extension: {}", extensionId);
        } catch (Exception e) {
            log.warn("Failed to close active connections for extension: {}", extension.getId(), e);
        }
    }

    /**
     * 获取扩展关闭状态
     * 
     * @param extensionId 扩展ID
     * @return 关闭状态信息
     */
    @NotNull
    public ShutdownStatus getShutdownStatus(@NotNull String extensionId) {
        NetworkExtension extension = extensionManager.getExtension(extensionId);
        if (extension == null) {
            return new ShutdownStatus(extensionId, false, "Extension not found", 0, 0);
        }

        boolean canShutdownSafely = true;
        long activeRequests = 0;
        int activeConnections = 0;

        if (extension instanceof GracefulShutdownExtension) {
            GracefulShutdownExtension gracefulExtension = (GracefulShutdownExtension) extension;
            canShutdownSafely = gracefulExtension.canShutdownSafely();
            activeRequests = gracefulExtension.getActiveRequestCount();
        }

        // 检查扩展是否支持统计功能，如果支持则使用StatisticsAware接口
        if (extension instanceof com.dtc.core.statistics.StatisticsAware) {
            com.dtc.core.statistics.StatisticsAware statsExtension = (com.dtc.core.statistics.StatisticsAware) extension;
            activeRequests = statsExtension.getActiveRequestCount();
        }

        // 尝试获取连接数量，使用反射调用连接管理方法
        try {
            if (extensionId.contains("tcp") || extensionId.contains("websocket") || extensionId.contains("custom")) {
                java.lang.reflect.Method method = extension.getClass().getMethod("getActiveConnectionCount");
                activeConnections = (Integer) method.invoke(extension);
            }
        } catch (Exception e) {
            log.debug("Extension {} does not support connection count: {}", extensionId, e.getMessage());
        }

        return new ShutdownStatus(extensionId, canShutdownSafely,
                canShutdownSafely ? "Ready for shutdown" : "Has active requests/connections",
                activeRequests, activeConnections);
    }

    /**
     * 关闭状态信息
     */
    public static class ShutdownStatus {
        private final String extensionId;
        private final boolean canShutdownSafely;
        private final String status;
        private final long activeRequests;
        private final int activeConnections;

        public ShutdownStatus(String extensionId, boolean canShutdownSafely, String status,
                long activeRequests, int activeConnections) {
            this.extensionId = extensionId;
            this.canShutdownSafely = canShutdownSafely;
            this.status = status;
            this.activeRequests = activeRequests;
            this.activeConnections = activeConnections;
        }

        public String getExtensionId() {
            return extensionId;
        }

        public boolean canShutdownSafely() {
            return canShutdownSafely;
        }

        public String getStatus() {
            return status;
        }

        public long getActiveRequests() {
            return activeRequests;
        }

        public int getActiveConnections() {
            return activeConnections;
        }

        @Override
        public String toString() {
            return String.format("ShutdownStatus{id='%s', canShutdown=%s, status='%s', requests=%d, connections=%d}",
                    extensionId, canShutdownSafely, status, activeRequests, activeConnections);
        }
    }
}
