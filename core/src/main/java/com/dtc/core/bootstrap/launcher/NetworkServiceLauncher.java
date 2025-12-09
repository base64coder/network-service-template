package com.dtc.core.bootstrap.launcher;

import com.dtc.api.annotations.NotNull;
import com.dtc.core.bootstrap.config.ServerConfiguration;
import com.dtc.core.extensions.ExtensionBootstrap;
import com.dtc.core.extensions.ExtensionManager;
import com.dtc.core.network.netty.NettyBootstrap;
import com.google.inject.Inject;
import com.google.inject.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

/**
 * 网络服务启动器
 * 负责启动和管理网络服务服务器
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
    private final @NotNull StartupBanner startupBanner;
    private final @NotNull java.util.Set<StartupHook> startupHooks;

    private volatile boolean started = false;

    @Inject
    public NetworkServiceLauncher(@NotNull ServerConfiguration configuration, @NotNull Injector injector,
            @NotNull NettyBootstrap nettyBootstrap, @NotNull ExtensionBootstrap extensionBootstrap,
            @NotNull ExtensionManager extensionManager, @NotNull StartupBanner startupBanner,
            @NotNull java.util.Set<StartupHook> startupHooks) {
        this.configuration = configuration;
        this.injector = injector;
        this.nettyBootstrap = nettyBootstrap;
        this.extensionBootstrap = extensionBootstrap;
        this.extensionManager = extensionManager;
        this.startupBanner = startupBanner;
        this.startupHooks = startupHooks;
    }

    /**
     * 启动网络服务服务器
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
                // 显示启动横幅
                startupBanner.displayBanner();
                startupBanner.displayServerInfo();
                startupBanner.displaySystemInfo();
                startupBanner.displayEnvironmentInfo();

                log.info("🚀 开始启动网络服务服务器...");

                // 1. 启动Netty服务器
                log.info("📡 启动 Netty 服务器...");
                nettyBootstrap.startServer().join();
                log.info("✅ Netty 服务器启动完成");

                // 2. 启动扩展系统
                log.info("🔌 启动扩展系统...");
                extensionBootstrap.startExtensionSystem().join();
                log.info("✅ 扩展系统启动完成");

                // 3. 执行启动钩子
                if (!startupHooks.isEmpty()) {
                    log.info("🪝 执行启动钩子...");
                    for (StartupHook hook : startupHooks) {
                        try {
                            hook.onServerStartup();
                        } catch (Exception e) {
                            log.error("Failed to execute startup hook: " + hook.getClass().getName(), e);
                        }
                    }
                }

                started = true;

                // 显示启动完成信息
                startupBanner.displayStartupComplete();
                log.info("🎉 网络服务服务器启动成功！所有服务已就绪");
            } catch (Exception e) {
                log.error("❌ 网络服务服务器启动失败", e);
                throw new RuntimeException("Failed to start Network Service Server", e);
            }
        });
    }

    /**
     * 停止网络服务服务器
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
