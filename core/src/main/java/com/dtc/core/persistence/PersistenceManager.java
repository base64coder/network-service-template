package com.dtc.core.persistence;

import com.dtc.api.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.CompletableFuture;

/**
 * 持久化管理器
 * 管理数据持久化
 * 
 * @author Network Service Template
 */
@Singleton
public class PersistenceManager {

    private static final Logger log = LoggerFactory.getLogger(PersistenceManager.class);

    private final @NotNull DataStore dataStore;
    private volatile boolean started = false;

    @Inject
    public PersistenceManager(@NotNull DataStore dataStore) {
        this.dataStore = dataStore;
    }

    /**
     * 启动持久化系统
     * 
     * @return 启动完成的Future
     */
    @NotNull
    public CompletableFuture<Void> start() {
        if (started) {
            return CompletableFuture.completedFuture(null);
        }

        log.info("Starting persistence manager...");

        return CompletableFuture.runAsync(() -> {
            try {
                dataStore.initialize();
                started = true;
                log.info("Persistence manager started successfully");
            } catch (Exception e) {
                log.error("Failed to start persistence manager", e);
                throw new RuntimeException("Failed to start persistence manager", e);
            }
        });
    }

    /**
     * 停止持久化系统
     * 
     * @return 停止完成的Future
     */
    @NotNull
    public CompletableFuture<Void> stop() {
        if (!started) {
            return CompletableFuture.completedFuture(null);
        }

        log.info("Stopping persistence manager...");

        return CompletableFuture.runAsync(() -> {
            try {
                dataStore.shutdown();
                started = false;
                log.info("Persistence manager stopped successfully");
            } catch (Exception e) {
                log.error("Failed to stop persistence manager", e);
                throw new RuntimeException("Failed to stop persistence manager", e);
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
