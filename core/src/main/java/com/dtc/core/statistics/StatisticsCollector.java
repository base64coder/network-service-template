package com.dtc.core.statistics;

import com.dtc.api.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 统一统计收集器
 * 负责收集和管理所有组件的统计信息
 * 
 * @author Network Service Template
 */
@Singleton
public class StatisticsCollector {

    private static final Logger log = LoggerFactory.getLogger(StatisticsCollector.class);

    // 连接统计
    private final AtomicInteger activeConnections = new AtomicInteger(0);
    private final AtomicInteger totalClients = new AtomicInteger(0);

    // 请求统计
    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong totalProcessedRequests = new AtomicLong(0);
    private final AtomicLong errorRequestCount = new AtomicLong(0);
    private final AtomicLong activeRequestCount = new AtomicLong(0);
    private final AtomicLong pendingRequestCount = new AtomicLong(0);
    private final AtomicLong totalProcessingTime = new AtomicLong(0);

    @Inject
    public StatisticsCollector() {
        log.info("Creating StatisticsCollector instance");
    }

    // ========== 连接统计方法 ==========

    /**
     * 连接建立
     */
    public void onConnectionEstablished() {
        activeConnections.incrementAndGet();
        totalClients.incrementAndGet();
        log.debug("🔌 New connection established. Active connections: {}", activeConnections.get());
    }

    /**
     * 连接断开
     */
    public void onConnectionClosed() {
        activeConnections.decrementAndGet();
        log.debug("🔌 Connection closed. Active connections: {}", activeConnections.get());
    }

    /**
     * 获取活跃连接数
     */
    public int getActiveConnections() {
        return activeConnections.get();
    }

    /**
     * 获取总客户端数
     */
    public int getTotalClients() {
        return totalClients.get();
    }

    // ========== 请求统计方法 ==========

    /**
     * 记录请求开始
     */
    public void onRequestStart() {
        totalRequests.incrementAndGet();
        activeRequestCount.incrementAndGet();
        pendingRequestCount.incrementAndGet();
    }

    /**
     * 记录请求处理完成
     */
    public void onRequestComplete(long processingTimeMs) {
        activeRequestCount.decrementAndGet();
        pendingRequestCount.decrementAndGet();
        totalProcessedRequests.incrementAndGet();
        totalProcessingTime.addAndGet(processingTimeMs);
    }

    /**
     * 记录请求处理错误
     */
    public void onRequestError() {
        activeRequestCount.decrementAndGet();
        pendingRequestCount.decrementAndGet();
        errorRequestCount.incrementAndGet();
    }

    /**
     * 获取总请求数
     */
    public long getTotalRequests() {
        return totalRequests.get();
    }

    /**
     * 获取已处理请求数
     */
    public long getTotalProcessedRequests() {
        return totalProcessedRequests.get();
    }

    /**
     * 获取错误请求数
     */
    public long getErrorRequestCount() {
        return errorRequestCount.get();
    }

    /**
     * 获取活跃请求数
     */
    public long getActiveRequestCount() {
        return activeRequestCount.get();
    }

    /**
     * 获取待处理请求数
     */
    public long getPendingRequestCount() {
        return pendingRequestCount.get();
    }

    /**
     * 获取平均处理时间
     */
    public double getAverageProcessingTime() {
        long total = totalProcessedRequests.get();
        if (total == 0) {
            return 0.0;
        }
        return (double) totalProcessingTime.get() / total;
    }

    // ========== 统计信息获取 ==========

    /**
     * 获取完整统计信息
     */
    @NotNull
    public StatisticsInfo getStatistics() {
        return new StatisticsInfo(
                getActiveConnections(),
                getTotalClients(),
                getTotalRequests(),
                getTotalProcessedRequests(),
                getErrorRequestCount(),
                getActiveRequestCount(),
                getPendingRequestCount(),
                getAverageProcessingTime(),
                System.currentTimeMillis());
    }

    /**
     * 重置所有统计信息
     */
    public void resetStatistics() {
        activeConnections.set(0);
        totalClients.set(0);
        totalRequests.set(0);
        totalProcessedRequests.set(0);
        errorRequestCount.set(0);
        activeRequestCount.set(0);
        pendingRequestCount.set(0);
        totalProcessingTime.set(0);
        log.info("📊 Statistics reset completed");
    }

    /**
     * 统计信息数据类
     */
    public static class StatisticsInfo {
        private final int activeConnections;
        private final int totalClients;
        private final long totalRequests;
        private final long totalProcessedRequests;
        private final long errorRequestCount;
        private final long activeRequestCount;
        private final long pendingRequestCount;
        private final double averageProcessingTime;
        private final long timestamp;

        public StatisticsInfo(int activeConnections, int totalClients, long totalRequests,
                long totalProcessedRequests, long errorRequestCount, long activeRequestCount,
                long pendingRequestCount, double averageProcessingTime, long timestamp) {
            this.activeConnections = activeConnections;
            this.totalClients = totalClients;
            this.totalRequests = totalRequests;
            this.totalProcessedRequests = totalProcessedRequests;
            this.errorRequestCount = errorRequestCount;
            this.activeRequestCount = activeRequestCount;
            this.pendingRequestCount = pendingRequestCount;
            this.averageProcessingTime = averageProcessingTime;
            this.timestamp = timestamp;
        }

        // Getters
        public int getActiveConnections() {
            return activeConnections;
        }

        public int getTotalClients() {
            return totalClients;
        }

        public long getTotalRequests() {
            return totalRequests;
        }

        public long getTotalProcessedRequests() {
            return totalProcessedRequests;
        }

        public long getErrorRequestCount() {
            return errorRequestCount;
        }

        public long getActiveRequestCount() {
            return activeRequestCount;
        }

        public long getPendingRequestCount() {
            return pendingRequestCount;
        }

        public double getAverageProcessingTime() {
            return averageProcessingTime;
        }

        public long getTimestamp() {
            return timestamp;
        }

        @Override
        public String toString() {
            return String.format(
                    "StatisticsInfo{activeConnections=%d, totalClients=%d, totalRequests=%d, " +
                            "totalProcessedRequests=%d, errorRequestCount=%d, activeRequestCount=%d, " +
                            "pendingRequestCount=%d, averageProcessingTime=%.2f, timestamp=%d}",
                    activeConnections, totalClients, totalRequests, totalProcessedRequests,
                    errorRequestCount, activeRequestCount, pendingRequestCount, averageProcessingTime, timestamp);
        }
    }
}
