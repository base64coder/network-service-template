package com.dtc.core.extensions;

import com.dtc.api.annotations.NotNull;
import com.dtc.core.extensions.model.ExtensionEvent;
import com.dtc.core.extensions.model.ExtensionMetadata;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 扩展生命周期操作管理器
 * 提供统一的扩展生命周期操作接口
 * 
 * @author Network Service Template
 */
@Singleton
public class ExtensionLifecycleOperations {

    private static final Logger log = LoggerFactory.getLogger(ExtensionLifecycleOperations.class);
    private static final long DEFAULT_OPERATION_TIMEOUT_MS = 30000; // 30秒默认超时

    private final ExtensionManager extensionManager;
    private final ExtensionShutdownManager shutdownManager;
    private final ExtensionLifecycleHandler lifecycleHandler;

    @Inject
    public ExtensionLifecycleOperations(@NotNull ExtensionManager extensionManager,
            @NotNull ExtensionShutdownManager shutdownManager,
            @NotNull ExtensionLifecycleHandler lifecycleHandler) {
        this.extensionManager = extensionManager;
        this.shutdownManager = shutdownManager;
        this.lifecycleHandler = lifecycleHandler;
    }

    /**
     * 批量启用扩展
     * 
     * @param extensionIds 扩展ID列表
     * @return 操作完成的Future
     */
    @NotNull
    public CompletableFuture<OperationResult> enableExtensions(@NotNull Collection<String> extensionIds) {
        return enableExtensions(extensionIds, DEFAULT_OPERATION_TIMEOUT_MS);
    }

    /**
     * 批量启用扩展
     * 
     * @param extensionIds 扩展ID列表
     * @param timeoutMs    超时时间（毫秒）
     * @return 操作完成的Future
     */
    @NotNull
    public CompletableFuture<OperationResult> enableExtensions(@NotNull Collection<String> extensionIds,
            long timeoutMs) {
        log.info("Enabling {} extensions with timeout: {}ms", extensionIds.size(), timeoutMs);

        return CompletableFuture.supplyAsync(() -> {
            OperationResult result = new OperationResult();

            for (String extensionId : extensionIds) {
                try {
                    ExtensionEvent event = new ExtensionEvent(ExtensionEvent.Change.ENABLE,
                            ExtensionMetadata.builder().id(extensionId).build(),
                            java.nio.file.Paths.get("extensions", extensionId));
                    lifecycleHandler.handleExtensionEvents(List.of(event)).get(timeoutMs, TimeUnit.MILLISECONDS);
                    result.addSuccess(extensionId);
                    log.info("Extension {} enabled successfully", extensionId);
                } catch (Exception e) {
                    result.addFailure(extensionId, e.getMessage());
                    log.error("Failed to enable extension: {}", extensionId, e);
                }
            }

            log.info("Batch enable completed: {} successful, {} failed",
                    result.getSuccessCount(), result.getFailureCount());
            return result;
        });
    }

    /**
     * 批量禁用扩展
     * 
     * @param extensionIds 扩展ID列表
     * @return 操作完成的Future
     */
    @NotNull
    public CompletableFuture<OperationResult> disableExtensions(@NotNull Collection<String> extensionIds) {
        return disableExtensions(extensionIds, DEFAULT_OPERATION_TIMEOUT_MS);
    }

    /**
     * 批量禁用扩展
     * 
     * @param extensionIds 扩展ID列表
     * @param timeoutMs    超时时间（毫秒）
     * @return 操作完成的Future
     */
    @NotNull
    public CompletableFuture<OperationResult> disableExtensions(@NotNull Collection<String> extensionIds,
            long timeoutMs) {
        log.info("Disabling {} extensions with timeout: {}ms", extensionIds.size(), timeoutMs);

        return CompletableFuture.supplyAsync(() -> {
            OperationResult result = new OperationResult();

            for (String extensionId : extensionIds) {
                try {
                    ExtensionEvent event = new ExtensionEvent(ExtensionEvent.Change.DISABLE,
                            ExtensionMetadata.builder().id(extensionId).build(),
                            java.nio.file.Paths.get("extensions", extensionId));
                    lifecycleHandler.handleExtensionEvents(List.of(event)).get(timeoutMs, TimeUnit.MILLISECONDS);
                    result.addSuccess(extensionId);
                    log.info("Extension {} disabled successfully", extensionId);
                } catch (Exception e) {
                    result.addFailure(extensionId, e.getMessage());
                    log.error("Failed to disable extension: {}", extensionId, e);
                }
            }

            log.info("Batch disable completed: {} successful, {} failed",
                    result.getSuccessCount(), result.getFailureCount());
            return result;
        });
    }

    /**
     * 批量更新扩展
     * 
     * @param extensionIds 扩展ID列表
     * @return 操作完成的Future
     */
    @NotNull
    public CompletableFuture<OperationResult> updateExtensions(@NotNull Collection<String> extensionIds) {
        return updateExtensions(extensionIds, DEFAULT_OPERATION_TIMEOUT_MS);
    }

    /**
     * 批量更新扩展
     * 
     * @param extensionIds 扩展ID列表
     * @param timeoutMs    超时时间（毫秒）
     * @return 操作完成的Future
     */
    @NotNull
    public CompletableFuture<OperationResult> updateExtensions(@NotNull Collection<String> extensionIds,
            long timeoutMs) {
        log.info("Updating {} extensions with timeout: {}ms", extensionIds.size(), timeoutMs);

        return CompletableFuture.supplyAsync(() -> {
            OperationResult result = new OperationResult();

            for (String extensionId : extensionIds) {
                try {
                    ExtensionEvent event = new ExtensionEvent(ExtensionEvent.Change.UPDATE,
                            ExtensionMetadata.builder().id(extensionId).build(),
                            java.nio.file.Paths.get("extensions", extensionId));
                    lifecycleHandler.handleExtensionEvents(List.of(event)).get(timeoutMs, TimeUnit.MILLISECONDS);
                    result.addSuccess(extensionId);
                    log.info("Extension {} updated successfully", extensionId);
                } catch (Exception e) {
                    result.addFailure(extensionId, e.getMessage());
                    log.error("Failed to update extension: {}", extensionId, e);
                }
            }

            log.info("Batch update completed: {} successful, {} failed",
                    result.getSuccessCount(), result.getFailureCount());
            return result;
        });
    }

    /**
     * 批量移除扩展
     * 
     * @param extensionIds 扩展ID列表
     * @return 操作完成的Future
     */
    @NotNull
    public CompletableFuture<OperationResult> removeExtensions(@NotNull Collection<String> extensionIds) {
        return removeExtensions(extensionIds, DEFAULT_OPERATION_TIMEOUT_MS);
    }

    /**
     * 批量移除扩展
     * 
     * @param extensionIds 扩展ID列表
     * @param timeoutMs    超时时间（毫秒）
     * @return 操作完成的Future
     */
    @NotNull
    public CompletableFuture<OperationResult> removeExtensions(@NotNull Collection<String> extensionIds,
            long timeoutMs) {
        log.info("Removing {} extensions with timeout: {}ms", extensionIds.size(), timeoutMs);

        return CompletableFuture.supplyAsync(() -> {
            OperationResult result = new OperationResult();

            for (String extensionId : extensionIds) {
                try {
                    ExtensionEvent event = new ExtensionEvent(ExtensionEvent.Change.REMOVE,
                            ExtensionMetadata.builder().id(extensionId).build(),
                            java.nio.file.Paths.get("extensions", extensionId));
                    lifecycleHandler.handleExtensionEvents(List.of(event)).get(timeoutMs, TimeUnit.MILLISECONDS);
                    result.addSuccess(extensionId);
                    log.info("Extension {} removed successfully", extensionId);
                } catch (Exception e) {
                    result.addFailure(extensionId, e.getMessage());
                    log.error("Failed to remove extension: {}", extensionId, e);
                }
            }

            log.info("Batch remove completed: {} successful, {} failed",
                    result.getSuccessCount(), result.getFailureCount());
            return result;
        });
    }

    /**
     * 获取所有扩展的状态
     * 
     * @return 扩展状态列表
     */
    @NotNull
    public List<ExtensionStatus> getAllExtensionStatus() {
        return extensionManager.getAllExtensions().values().stream()
                .map(this::getExtensionStatus)
                .collect(Collectors.toList());
    }

    /**
     * 获取单个扩展的状态
     * 
     * @param extension 扩展
     * @return 扩展状态
     */
    @NotNull
    private ExtensionStatus getExtensionStatus(@NotNull NetworkExtension extension) {
        String extensionId = extension.getId();
        boolean isStarted = extension.isStarted();
        boolean isEnabled = extension.isEnabled();

        // 获取关闭状态
        ExtensionShutdownManager.ShutdownStatus shutdownStatus = shutdownManager.getShutdownStatus(extensionId);

        return new ExtensionStatus(extensionId, extension.getName(), extension.getVersion(),
                isStarted, isEnabled, shutdownStatus.canShutdownSafely(),
                shutdownStatus.getActiveRequests(), shutdownStatus.getActiveConnections());
    }

    /**
     * 操作结果
     */
    public static class OperationResult {
        private final java.util.List<String> successful = new java.util.ArrayList<>();
        private final java.util.List<String> failed = new java.util.ArrayList<>();
        private final java.util.Map<String, String> failureReasons = new java.util.HashMap<>();

        public void addSuccess(String extensionId) {
            successful.add(extensionId);
        }

        public void addFailure(String extensionId, String reason) {
            failed.add(extensionId);
            failureReasons.put(extensionId, reason);
        }

        public List<String> getSuccessful() {
            return new java.util.ArrayList<>(successful);
        }

        public List<String> getFailed() {
            return new java.util.ArrayList<>(failed);
        }

        public Map<String, String> getFailureReasons() {
            return new java.util.HashMap<>(failureReasons);
        }

        public int getSuccessCount() {
            return successful.size();
        }

        public int getFailureCount() {
            return failed.size();
        }

        public boolean isAllSuccessful() {
            return failed.isEmpty();
        }

        @Override
        public String toString() {
            return String.format("OperationResult{successful=%d, failed=%d, allSuccessful=%s}",
                    getSuccessCount(), getFailureCount(), isAllSuccessful());
        }
    }

    /**
     * 扩展状态
     */
    public static class ExtensionStatus {
        private final String extensionId;
        private final String name;
        private final String version;
        private final boolean isStarted;
        private final boolean isEnabled;
        private final boolean canShutdownSafely;
        private final int activeRequests;
        private final int activeConnections;

        public ExtensionStatus(String extensionId, String name, String version, boolean isStarted,
                boolean isEnabled, boolean canShutdownSafely, int activeRequests, int activeConnections) {
            this.extensionId = extensionId;
            this.name = name;
            this.version = version;
            this.isStarted = isStarted;
            this.isEnabled = isEnabled;
            this.canShutdownSafely = canShutdownSafely;
            this.activeRequests = activeRequests;
            this.activeConnections = activeConnections;
        }

        // Getters
        public String getExtensionId() {
            return extensionId;
        }

        public String getName() {
            return name;
        }

        public String getVersion() {
            return version;
        }

        public boolean isStarted() {
            return isStarted;
        }

        public boolean isEnabled() {
            return isEnabled;
        }

        public boolean canShutdownSafely() {
            return canShutdownSafely;
        }

        public int getActiveRequests() {
            return activeRequests;
        }

        public int getActiveConnections() {
            return activeConnections;
        }

        @Override
        public String toString() {
            return String.format("ExtensionStatus{id='%s', name='%s', version='%s', started=%s, enabled=%s, " +
                    "canShutdown=%s, requests=%d, connections=%d}",
                    extensionId, name, version, isStarted, isEnabled, canShutdownSafely, activeRequests,
                    activeConnections);
        }
    }
}
