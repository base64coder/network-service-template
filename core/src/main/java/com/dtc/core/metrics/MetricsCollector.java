package com.dtc.core.metrics;

import com.dtc.api.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 指标收集器
 * 负责定期收集和汇总系统中的指标数据
 * 
 * @author Network Service Template
 */
@Singleton
public class MetricsCollector {

    private static final Logger log = LoggerFactory.getLogger(MetricsCollector.class);

    private final @NotNull MetricsRegistry metricsRegistry;
    private final @NotNull ScheduledExecutorService scheduler;
    private volatile boolean started = false;

    @Inject
    public MetricsCollector(@NotNull MetricsRegistry metricsRegistry) {
        this.metricsRegistry = metricsRegistry;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "metrics-collector");
            thread.setDaemon(true);
            return thread;
        });
    }

    /**
     * 启动指标收集器
     */
    public void start() {
        if (started) {
            return;
        }

        log.info("Starting metrics collection...");

        // 每30秒收集一次指标数据
        scheduler.scheduleAtFixedRate(this::collectMetrics, 30, 30, TimeUnit.SECONDS);

        started = true;
        log.info("Metrics collection started");
    }

    /**
     * 停止指标收集器
     */
    public void stop() {
        if (!started) {
            return;
        }

        log.info("Stopping metrics collection...");

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
        log.info("Metrics collection stopped");
    }

    /**
     * 收集指标数据
     */
    private void collectMetrics() {
        try {
            // 收集计数器指标
            Map<String, Long> counters = metricsRegistry.getAllCounters();
            if (!counters.isEmpty()) {
                log.debug("Collected {} counter metrics", counters.size());
            }

            // 收集仪表盘指标
            Map<String, Long> gauges = metricsRegistry.getAllGauges();
            if (!gauges.isEmpty()) {
                log.debug("Collected {} gauge metrics", gauges.size());
            }

        } catch (Exception e) {
            log.error("Failed to collect metrics", e);
        }
    }

    /**
     * 检查是否已启动
     * 
     * @return 是否已启动
     */
    public boolean isStarted() {
        return started;
    }
}
