package com.dtc.core.diagnostic;

import com.dtc.api.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 健康检查器
 * 负责定期健康检查
 * 
 * @author Network Service Template
 */
@Singleton
public class HealthChecker {

    private static final Logger log = LoggerFactory.getLogger(HealthChecker.class);

    private final @NotNull DiagnosticService diagnosticService;
    private final @NotNull ScheduledExecutorService scheduler;
    private volatile boolean started = false;

    @Inject
    public HealthChecker(@NotNull DiagnosticService diagnosticService) {
        this.diagnosticService = diagnosticService;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "health-checker");
            thread.setDaemon(true);
            return thread;
        });
    }

    /**
     * 启动健康检查
     */
    public void start() {
        if (started) {
            return;
        }

        log.info("Starting health checker...");

        // 每60秒执行一次健康检查
        scheduler.scheduleAtFixedRate(this::performHealthCheck, 60, 60, TimeUnit.SECONDS);

        started = true;
        log.info("Health checker started");
    }

    /**
     * 停止健康检查
     */
    public void stop() {
        if (!started) {
            return;
        }

        log.info("Stopping health checker...");

        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }

        started = false;
        log.info("Health checker stopped");
    }

    /**
     * 执行健康检查
     */
    private void performHealthCheck() {
        try {
            DiagnosticService.HealthStatus status = diagnosticService.performHealthCheck();
            log.debug("Health check result: {}", status);

            if (status != DiagnosticService.HealthStatus.HEALTHY) {
                log.warn("System health status: {}", status);
            }

        } catch (Exception e) {
            log.error("Health check failed", e);
        }
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
