package com.dtc.core;

import com.google.inject.Injector;
import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import com.dtc.core.bootstrap.NetworkServiceLauncher;
import com.dtc.core.config.ServerConfiguration;
import com.dtc.core.extensions.ExtensionBootstrap;
import com.dtc.core.extensions.ExtensionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 网络服务主类
 * 负责启动和管理网络服务
 * 
 * @author Network Service Template
 */
public class NetworkService {

    private static final Logger log = LoggerFactory.getLogger(NetworkService.class);

    private final @NotNull ServerConfiguration configuration;
    private final @NotNull ExtensionBootstrap extensionBootstrap;
    private final @NotNull NetworkServiceLauncher networkLauncher;
    private final @NotNull ExtensionManager extensionManager;
    private final @NotNull Injector injector;

    private volatile boolean started = false;
    private volatile boolean stopped = false;

    public NetworkService(@NotNull ServerConfiguration config) {
        this.configuration = config;

        // 使用分层设计初始化依赖注入容器
        this.injector = com.dtc.core.bootstrap.ioc.GuiceContainerFactory.bootstrapInjector(configuration);
        if (injector == null) {
            throw new RuntimeException("Failed to initialize dependency injection container");
        }

        // 获取核心组件
        this.extensionBootstrap = injector.getInstance(ExtensionBootstrap.class);
        this.networkLauncher = injector.getInstance(NetworkServiceLauncher.class);
        this.extensionManager = injector.getInstance(ExtensionManager.class);
    }

    /**
     * 启动网络服务
     * 
     * @return 启动完成的Future
     */
    @NotNull
    public CompletableFuture<Void> start() {
        if (started) {
            return CompletableFuture.completedFuture(null);
        }

        log.info("Starting Network Service...");

        return extensionBootstrap.startExtensionSystem()
                .thenCompose(v -> networkLauncher.startServer())
                .thenRun(() -> {
                    started = true;
                    log.info("Network Service started successfully");
                })
                .exceptionally(throwable -> {
                    log.error("Failed to start Network Service", throwable);
                    throw new RuntimeException("Failed to start Network Service", throwable);
                });
    }

    /**
     * 停止网络服务
     * 
     * @return 停止完成的Future
     */
    @NotNull
    public CompletableFuture<Void> stop() {
        if (stopped) {
            return CompletableFuture.completedFuture(null);
        }

        log.info("Stopping Network Service...");

        return networkLauncher.stopServer()
                .thenCompose(v -> extensionBootstrap.stopExtensionSystem())
                .thenRun(() -> {
                    stopped = true;
                    started = false;
                    log.info("Network Service stopped successfully");
                })
                .exceptionally(throwable -> {
                    log.error("Failed to stop Network Service", throwable);
                    throw new RuntimeException("Failed to stop Network Service", throwable);
                });
    }

    /**
     * 获取扩展管理器
     * 
     * @return 扩展管理器
     */
    @NotNull
    public ExtensionManager getExtensionManager() {
        return extensionManager;
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

    /**
     * 是否已启动
     * 
     * @return 是否已启动
     */
    public boolean isStarted() {
        return started;
    }

    /**
     * 是否已停止
     * 
     * @return 是否已停止
     */
    public boolean isStopped() {
        return stopped;
    }

    /**
     * 主方法
     * 
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        try {
            // 创建服务器配置
            ServerConfiguration config = ServerConfiguration.builder()
                    .serverName("Network Service")
                    .serverVersion("1.0.0")
                    .dataFolder("data")
                    .configFolder("conf")
                    .extensionsFolder("extensions")
                    .build();

            // 创建并启动服务
            NetworkService service = new NetworkService(config);

            // 添加关闭钩子
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    service.stop().get(30, TimeUnit.SECONDS);
                } catch (Exception e) {
                    log.error("Error during shutdown", e);
                }
            }));

            // 启动服务
            service.start().get();

            // 保持运行
            Thread.currentThread().join();

        } catch (Exception e) {
            log.error("Failed to start Network Service", e);
            System.exit(1);
        }
    }
}
