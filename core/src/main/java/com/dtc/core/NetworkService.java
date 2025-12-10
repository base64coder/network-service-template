package com.dtc.core;

import com.dtc.api.ServiceConfig;
import com.dtc.api.annotations.NotNull;
import com.dtc.core.bootstrap.launcher.NetworkServiceLauncher;
import com.dtc.core.bootstrap.launcher.ServerStatusDisplay;
import com.dtc.core.bootstrap.ioc.IoCContainerFactory;
import com.dtc.core.bootstrap.config.ServerConfiguration;
import com.dtc.core.extensions.ExtensionBootstrap;
import com.dtc.core.extensions.ExtensionManager;
import com.dtc.core.messaging.NetworkMessageHandler;
import com.dtc.core.messaging.NetworkMessageQueue;
import com.google.inject.Injector;
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
    private final @NotNull ServerStatusDisplay statusDisplay;
    private final @NotNull NetworkMessageQueue messageQueue;
    private final @NotNull NetworkMessageHandler messageHandler;

    private volatile boolean started = false;
    private volatile boolean stopped = false;

    public NetworkService(@NotNull ServerConfiguration config) {
        this.configuration = config;

        // 使用分层设计初始化依赖注入容器
        this.injector = IoCContainerFactory.bootstrapInjector(configuration);
        if (injector == null) {
            throw new RuntimeException("Failed to initialize dependency injection container");
        }

        // 获取核心组件
        this.extensionBootstrap = injector.getInstance(ExtensionBootstrap.class);
        this.networkLauncher = injector.getInstance(NetworkServiceLauncher.class);
        this.extensionManager = injector.getInstance(ExtensionManager.class);
        this.messageQueue = injector.getInstance(NetworkMessageQueue.class);
        this.messageHandler = injector.getInstance(NetworkMessageHandler.class);
        this.statusDisplay = new ServerStatusDisplay(configuration);
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

        return extensionBootstrap.startExtensionSystem().thenCompose(v -> networkLauncher.startServer()).thenRun(() -> {
            started = true;

            // 启动消息队列
            messageQueue.start();

            // 启动状态显示器
            statusDisplay.startStatusDisplay();

            log.info("🎉 Network Service 启动成功！消息处理系统已就绪");
        }).exceptionally(throwable -> {
            log.error("❌ Network Service 启动失败", throwable);
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

        return networkLauncher.stopServer().thenCompose(v -> extensionBootstrap.stopExtensionSystem()).thenRun(() -> {
            // 停止消息队列
            messageQueue.stop();

            // 停止状态显示器
            statusDisplay.stopStatusDisplay();
            statusDisplay.displayShutdownInfo();

            stopped = true;
            started = false;
            log.info("🛑 Network Service 已停止");
        }).exceptionally(throwable -> {
            log.error("❌ Network Service 停止失败", throwable);
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
     * 获取消息处理器
     * 
     * @return 消息处理器
     */
    @NotNull
    public NetworkMessageHandler getMessageHandler() {
        return messageHandler;
    }

    /**
     * 获取消息处理器统计信息
     * 
     * @return 统计信息
     */
    @NotNull
    public NetworkMessageHandler.HandlerStats getMessageStats() {
        return messageHandler.getStats();
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
            // 使用 ServiceConfig 枚举创建服务器配置
            ServerConfiguration.Builder configBuilder = ServerConfiguration.builder()
                    .serverName("Network Service")
                    .serverVersion("1.0.0")
                    .dataFolder("data")
                    .configFolder("conf")
                    .extensionsFolder("extensions");

            // 根据 ServiceConfig 枚举动态添加监听器
            for (ServiceConfig serviceConfig : ServiceConfig.values()) {
                configBuilder.addListener(
                        serviceConfig.getServiceId(),
                        serviceConfig.getDefaultPort(),
                        "0.0.0.0",
                        true,
                        serviceConfig.getServiceName(),
                        serviceConfig.getDescription());
            }

            ServerConfiguration config = configBuilder.build();

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
