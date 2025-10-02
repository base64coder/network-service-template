package com.dtc.core.extensions;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import com.dtc.core.config.ServerConfiguration;
import com.dtc.core.extensions.model.ExtensionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

/**
 * 扩展系统启动器
 * 负责启动和管理扩展系统
 * 
 * @author Network Service Template
 */
public class ExtensionBootstrap {

    private static final Logger log = LoggerFactory.getLogger(ExtensionBootstrap.class);

    private final @NotNull ServerConfiguration configuration;
    private final @NotNull ExtensionLoader extensionLoader;
    private final @NotNull ExtensionManager extensionManager;
    private final @NotNull ExtensionLifecycleHandler lifecycleHandler;

    public ExtensionBootstrap(
            @NotNull ServerConfiguration configuration,
            @NotNull ExtensionLoader extensionLoader,
            @NotNull ExtensionManager extensionManager,
            @NotNull ExtensionLifecycleHandler lifecycleHandler) {
        this.configuration = configuration;
        this.extensionLoader = extensionLoader;
        this.extensionManager = extensionManager;
        this.lifecycleHandler = lifecycleHandler;
    }

    /**
     * 启动扩展系统
     * 
     * @return 启动完成的Future
     */
    @NotNull
    public CompletableFuture<Void> startExtensionSystem() {
        log.info("Starting extension system...");

        return CompletableFuture.runAsync(() -> {
            try {
                // 扫描扩展目录
                Collection<ExtensionEvent> extensionEvents = extensionLoader.loadExtensions(configuration.getExtensionsFolder());

                // 处理扩展事件
                lifecycleHandler.handleExtensionEvents(extensionEvents).join();

                log.info("Extension system started successfully");
            } catch (Exception e) {
                log.error("Failed to start extension system", e);
                throw new RuntimeException("Failed to start extension system", e);
            }
        });
    }

    /**
     * 停止扩展系统
     * 
     * @return 停止完成的Future
     */
    @NotNull
    public CompletableFuture<Void> stopExtensionSystem() {
        log.info("Stopping extension system...");

        return CompletableFuture.runAsync(() -> {
            try {
                // 停止所有扩展
                extensionManager.stopAllExtensions();

                log.info("Extension system stopped successfully");
            } catch (Exception e) {
                log.error("Failed to stop extension system", e);
                throw new RuntimeException("Failed to stop extension system", e);
            }
        });
    }

    /**
     * 加载嵌入式扩展
     * 
     * @param embeddedExtension 嵌入式扩展
     * @return 加载完成的Future
     */
    @NotNull
    public CompletableFuture<Void> loadEmbeddedExtension(@NotNull Object embeddedExtension) {
        log.info("Loading embedded extension...");

        return CompletableFuture.runAsync(() -> {
            try {
                // 处理嵌入式扩展
                lifecycleHandler.handleEmbeddedExtension(embeddedExtension).join();

                log.info("Embedded extension loaded successfully");
            } catch (Exception e) {
                log.error("Failed to load embedded extension", e);
                throw new RuntimeException("Failed to load embedded extension", e);
            }
        });
    }
}
