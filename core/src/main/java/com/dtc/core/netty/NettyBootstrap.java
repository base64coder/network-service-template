package com.dtc.core.netty;

import com.dtc.api.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.CompletableFuture;

/**
 * Netty启动器
 * 负责启动和管理Netty服务器
 * 
 * @author Network Service Template
 */
@Singleton
public class NettyBootstrap {

    private static final Logger log = LoggerFactory.getLogger(NettyBootstrap.class);

    private final @NotNull NettyServer nettyServer;
    private volatile boolean started = false;

    @Inject
    public NettyBootstrap(@NotNull NettyServer nettyServer) {
        this.nettyServer = nettyServer;
    }

    /**
     * 启动Netty服务器
     * 
     * @return 启动完成的Future
     */
    @NotNull
    public CompletableFuture<Void> startServer() {
        if (started) {
            return CompletableFuture.completedFuture(null);
        }

        log.info("Starting Netty server...");

        return CompletableFuture.runAsync(() -> {
            try {
                nettyServer.start();
                started = true;
                log.info("Netty server started successfully");
            } catch (Exception e) {
                log.error("Failed to start Netty server", e);
                throw new RuntimeException("Failed to start Netty server", e);
            }
        });
    }

    /**
     * 停止Netty服务器
     * 
     * @return 停止完成的Future
     */
    @NotNull
    public CompletableFuture<Void> stopServer() {
        if (!started) {
            return CompletableFuture.completedFuture(null);
        }

        log.info("Stopping Netty server...");

        return CompletableFuture.runAsync(() -> {
            try {
                nettyServer.stop();
                started = false;
                log.info("Netty server stopped successfully");
            } catch (Exception e) {
                log.error("Failed to stop Netty server", e);
                throw new RuntimeException("Failed to stop Netty server", e);
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
}
