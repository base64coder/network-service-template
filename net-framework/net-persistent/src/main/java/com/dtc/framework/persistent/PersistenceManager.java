package com.dtc.framework.persistent;

import com.dtc.api.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.concurrent.CompletableFuture;

/**
 * é¸ä½·ç®éæ «î¸éåæ«
 * ç» ï¼æéçåµé¸ä½·ç®é? * 
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
     * éîå§©é¸ä½·ç®éæ «é´ç¼?     * 
     * @return éîå§©ç¹å±¾åé¨å¢uture
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
     * éæ»îé¸ä½·ç®éæ «é´ç¼?     * 
     * @return éæ»îç¹å±¾åé¨å¢uture
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
     * éîæå®¸ææé?     * 
     * @return éîæå®¸ææé
     */
    public boolean isStarted() {
        return started;
    }
}
