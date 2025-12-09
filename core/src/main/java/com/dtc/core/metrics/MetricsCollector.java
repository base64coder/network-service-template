package com.dtc.core.metrics;

import com.dtc.api.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * é¸å¨ç£éå æ³¦é£? * ç¹æ°­æ¹¡éå æ³¦éå±¾å§¤éå©å¯é? * 
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
     * éîå§©é¸å¨ç£éå æ³¦
     */
    public void start() {
        if (started) {
            return;
        }

        log.info("Starting metrics collection...");

        // å§£?0ç»ææ¹éåç«´å¨âå¯é?        scheduler.scheduleAtFixedRate(this::collectMetrics, 30, 30, TimeUnit.SECONDS);

        started = true;
        log.info("Metrics collection started");
    }

    /**
     * éæ»îé¸å¨ç£éå æ³¦
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
     * éå æ³¦é¸å¨ç£
     */
    private void collectMetrics() {
        try {
            // éå æ³¦çâæé£ã¦å¯é?            Map<String, Long> counters = metricsRegistry.getAllCounters();
            if (!counters.isEmpty()) {
                log.debug("Collected {} counter metrics", counters.size());
            }

            // éå æ³¦æµ îãé¸å¨ç£
            Map<String, Long> gauges = metricsRegistry.getAllGauges();
            if (!gauges.isEmpty()) {
                log.debug("Collected {} gauge metrics", gauges.size());
            }

        } catch (Exception e) {
            log.error("Failed to collect metrics", e);
        }
    }

    /**
     * éîæå®¸ææé?     * 
     * @return éîæå®¸ææé
     */
    public boolean isStarted() {
        return started;
    }
}
