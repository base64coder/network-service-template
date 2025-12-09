package com.dtc.core.network.netty;

import com.dtc.api.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.CompletableFuture;

/**
 * Nettyéîå§©é£?çç»çéîå§©éå²î¸éåettyéå¶å§é£? * 
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
     * éîå§©Nettyéå¶å§é£?     * 
     * @return éîå§©ç¹å±¾åé¨å¢uture
     */
    @NotNull
    public CompletableFuture<Void> startServer() {
        if (started) {
            return CompletableFuture.completedFuture(null);
        }

        log.info("Starting Netty server...");

        return CompletableFuture.runAsync(() -> {
            try {
                log.info("é¦æ¡ éæ¿îé?Netty éå¶å§é£ã§ç²æµ ?..");
                nettyServer.start();
                started = true;
                log.info("é?Netty éå¶å§é£ã¥æéã¦åé?- ç¼æ ç²¶çåå¡çè¾©å");
            } catch (Exception e) {
                log.error("é?Netty éå¶å§é£ã¥æéã¥ãç?, e);
                throw new RuntimeException("Failed to start Netty server", e);
            }
        });
    }

    /**
     * éæ»îNettyéå¶å§é£?     * 
     * @return éæ»îç¹å±¾åé¨å¢uture
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
     * éîæå®¸ææé?     * 
     * @return éîæå®¸ææé
     */
    public boolean isStarted() {
        return started;
    }
}
