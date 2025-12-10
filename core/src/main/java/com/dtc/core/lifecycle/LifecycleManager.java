package com.dtc.core.lifecycle;

import com.dtc.api.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Singleton;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 生命周期管理器
 * 管理所有生命周期组件
 * 
 * @author Network Service Template
 */
@Singleton
public class LifecycleManager {

    private static final Logger log = LoggerFactory.getLogger(LifecycleManager.class);

    private final @NotNull List<LifecycleComponent> components = new CopyOnWriteArrayList<>();
    private volatile boolean started = false;

    /**
     * 注册生命周期组件
     * 
     * @param component 生命周期组件
     */
    public void registerComponent(@NotNull LifecycleComponent component) {
        components.add(component);
        log.debug("Registered lifecycle component: {}", component.getClass().getSimpleName());
    }

    /**
     * 注销生命周期组件
     * 
     * @param component 生命周期组件
     */
    public void unregisterComponent(@NotNull LifecycleComponent component) {
        components.remove(component);
        log.debug("Unregistered lifecycle component: {}", component.getClass().getSimpleName());
    }

    /**
     * 启动所有组件
     * 
     * @return 启动完成的Future
     */
    @NotNull
    public CompletableFuture<Void> startAll() {
        if (started) {
            return CompletableFuture.completedFuture(null);
        }

        log.info("Starting all lifecycle components...");

        return CompletableFuture.runAsync(() -> {
            for (LifecycleComponent component : components) {
                try {
                    component.start();
                    log.debug("Started component: {}", component.getClass().getSimpleName());
                } catch (Exception e) {
                    log.error("Failed to start component: {}", component.getClass().getSimpleName(), e);
                    throw new RuntimeException("Failed to start component: " + component.getClass().getSimpleName(), e);
                }
            }
            started = true;
            log.info("All lifecycle components started successfully");
        });
    }

    /**
     * 停止所有组件
     * 
     * @return 停止完成的Future
     */
    @NotNull
    public CompletableFuture<Void> stopAll() {
        if (!started) {
            return CompletableFuture.completedFuture(null);
        }

        log.info("Stopping all lifecycle components...");

        return CompletableFuture.runAsync(() -> {
            // 逆序停止组件
            for (int i = components.size() - 1; i >= 0; i--) {
                LifecycleComponent component = components.get(i);
                try {
                    component.stop();
                    log.debug("Stopped component: {}", component.getClass().getSimpleName());
                } catch (Exception e) {
                    log.error("Failed to stop component: {}", component.getClass().getSimpleName(), e);
                }
            }
            started = false;
            log.info("All lifecycle components stopped successfully");
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
