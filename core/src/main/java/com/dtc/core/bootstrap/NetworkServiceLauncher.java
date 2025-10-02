package com.dtc.core.bootstrap;

import com.google.inject.Injector;
import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import com.dtc.core.config.ServerConfiguration;
import com.dtc.core.netty.NettyBootstrap;
import com.dtc.core.extensions.ExtensionBootstrap;
import com.dtc.core.extensions.ExtensionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

/**
 * 网络服务启动器
 * 负责启动网络服务的各个组件
 * 
 * @author Network Service Template
 */
public class NetworkServiceLauncher {

    private static final Logger log = LoggerFactory.getLogger(NetworkServiceLauncher.class);

    private final @NotNull ServerConfiguration configuration;
    private final @NotNull Injector injector;
    private final @NotNull NettyBootstrap nettyBootstrap;
    private final @NotNull ExtensionBootstrap extensionBootstrap;
    private final @NotNull ExtensionManager extensionManager;

    private volatile boolean started = false;

    public NetworkServiceLauncher(
            @NotNull ServerConfiguration configuration,
            @NotNull Injector injector,
            @NotNull NettyBootstrap nettyBootstrap,
            @NotNull ExtensionBootstrap extensionBootstrap,
            @NotNull ExtensionManager extensionManager) {
        this.configuration = configuration;
        this.injector = injector;
        this.nettyBootstrap = nettyBootstrap;
        this.extensionBootstrap = extensionBootstrap;
        this.extensionManager = extensionManager;
    }

    /**
     * 启动网络服务
     * 
     * @return 启动完成的Future
     */
    @NotNull
    public CompletableFuture<Void> startServer() {
        if (started) {
            return CompletableFuture.completedFuture(null);
        }

        log.info("Starting Network Service Server...");

        return CompletableFuture.runAsync(() -> {
            try {
                // 1. 启动Netty服务器
                nettyBootstrap.startServer().join();

                // 2. 启动扩展系统
                extensionBootstrap.startExtensionSystem().join();

                started = true;
                log.info("Network Service Server started successfully");
            } catch (Exception e) {
                log.error("Failed to start Network Service Server", e);
                throw new RuntimeException("Failed to start Network Service Server", e);
            }
        });
    }

    /**
     * 停止网络服务
     * 
     * @return 停止完成的Future
     */
    @NotNull
    public CompletableFuture<Void> stopServer() {
        if (!started) {
            return CompletableFuture.completedFuture(null);
        }

        log.info("Stopping Network Service Server...");

        return CompletableFuture.runAsync(() -> {
            try {
                // 1. 停止扩展系统
                extensionBootstrap.stopExtensionSystem().join();

                // 2. 停止Netty服务器
                nettyBootstrap.stopServer().join();

                started = false;
                log.info("Network Service Server stopped successfully");
            } catch (Exception e) {
                log.error("Failed to stop Network Service Server", e);
                throw new RuntimeException("Failed to stop Network Service Server", e);
            }
        });
    }

    /**
     * 是否已启动
     * 
     * @return 是否已启动
     */
    public boolean isStarted() {
        return started;
    }

    /**
     * 获取服务器配置
     * 
     * @return 服务器配置
     */
    @NotNull
    public ServerConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * 获取依赖注入容器
     * 
     * @return 注入容器
     */
    @NotNull
    public Injector getInjector() {
        return injector;
    }
}
