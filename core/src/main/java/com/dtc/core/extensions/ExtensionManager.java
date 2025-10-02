package com.dtc.core.extensions;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import com.dtc.core.extensions.model.ExtensionMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;

/**
 * 扩展管理器
 * 负责管理扩展的生命周期
 * 
 * @author Network Service Template
 */
public class ExtensionManager {

    private static final Logger log = LoggerFactory.getLogger(ExtensionManager.class);

    private final Map<String, NetworkExtension> extensions = new ConcurrentHashMap<>();
    private final Map<String, ClassLoader> extensionClassLoaders = new ConcurrentHashMap<>();

    /**
     * 注册扩展
     * 
     * @param extension 扩展实例
     */
    public void registerExtension(@NotNull NetworkExtension extension) {
        String extensionId = extension.getId();
        extensions.put(extensionId, extension);
        extensionClassLoaders.put(extensionId, extension.getExtensionClassloader());

        log.info("Registered extension: {} v{}", extensionId, extension.getVersion());
    }

    /**
     * 注销扩展
     * 
     * @param extensionId 扩展ID
     */
    public void unregisterExtension(@NotNull String extensionId) {
        NetworkExtension extension = extensions.remove(extensionId);
        extensionClassLoaders.remove(extensionId);

        if (extension != null) {
            log.info("Unregistered extension: {}", extensionId);
        }
    }

    /**
     * 获取扩展
     * 
     * @param extensionId 扩展ID
     * @return 扩展实例
     */
    @Nullable
    public NetworkExtension getExtension(@NotNull String extensionId) {
        return extensions.get(extensionId);
    }

    /**
     * 获取所有扩展
     * 
     * @return 扩展映射
     */
    @NotNull
    public Map<String, NetworkExtension> getAllExtensions() {
        return Map.copyOf(extensions);
    }

    /**
     * 启动扩展
     * 
     * @param extensionId 扩展ID
     * @return 启动完成的Future
     */
    @NotNull
    public CompletableFuture<Void> startExtension(@NotNull String extensionId) {
        NetworkExtension extension = extensions.get(extensionId);
        if (extension == null) {
            return CompletableFuture.failedFuture(
                    new IllegalArgumentException("Extension not found: " + extensionId));
        }

        return CompletableFuture.runAsync(() -> {
            try {
                extension.start();
                log.info("Started extension: {}", extensionId);
            } catch (Exception e) {
                log.error("Failed to start extension: {}", extensionId, e);
                throw new RuntimeException("Failed to start extension: " + extensionId, e);
            }
        });
    }

    /**
     * 停止扩展
     * 
     * @param extensionId 扩展ID
     * @return 停止完成的Future
     */
    @NotNull
    public CompletableFuture<Void> stopExtension(@NotNull String extensionId) {
        NetworkExtension extension = extensions.get(extensionId);
        if (extension == null) {
            return CompletableFuture.failedFuture(
                    new IllegalArgumentException("Extension not found: " + extensionId));
        }

        return CompletableFuture.runAsync(() -> {
            try {
                extension.stop();
                log.info("Stopped extension: {}", extensionId);
            } catch (Exception e) {
                log.error("Failed to stop extension: {}", extensionId, e);
                throw new RuntimeException("Failed to stop extension: " + extensionId, e);
            }
        });
    }

    /**
     * 停止所有扩展
     * 
     * @return 停止完成的Future
     */
    @NotNull
    public CompletableFuture<Void> stopAllExtensions() {
        return CompletableFuture.allOf(
                extensions.keySet().stream()
                        .map(this::stopExtension)
                        .toArray(CompletableFuture[]::new));
    }

    /**
     * 检查扩展是否存在
     * 
     * @param extensionId 扩展ID
     * @return 是否存在
     */
    public boolean hasExtension(@NotNull String extensionId) {
        return extensions.containsKey(extensionId);
    }

    /**
     * 获取扩展数量
     * 
     * @return 扩展数量
     */
    public int getExtensionCount() {
        return extensions.size();
    }
}
