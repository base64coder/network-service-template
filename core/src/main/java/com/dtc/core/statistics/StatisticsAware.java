package com.dtc.core.statistics;

import com.dtc.api.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;

/**
 * 统计信息感知接口
 * 为需要统计信息的组件提供统计信息收集功能
 * 组件统计信息通过StatisticsCollector收集，并可以查询统计信息
 * 
 * @author Network Service Template
 */
public abstract class StatisticsAware {

    private static final Logger log = LoggerFactory.getLogger(StatisticsAware.class);

    protected final StatisticsCollector statisticsCollector;

    @Inject
    public StatisticsAware(@NotNull StatisticsCollector statisticsCollector) {
        this.statisticsCollector = statisticsCollector;
    }

    /**
     * 记录请求开始
     */
    protected void recordRequestStart() {
        statisticsCollector.onRequestStart();
    }

    /**
     * 记录请求完成
     */
    protected void recordRequestComplete(long processingTimeMs) {
        statisticsCollector.onRequestComplete(processingTimeMs);
    }

    /**
     * 记录请求错误
     */
    protected void recordRequestError() {
        statisticsCollector.onRequestError();
    }

    /**
     * 记录连接建立
     */
    protected void recordConnectionEstablished() {
        statisticsCollector.onConnectionEstablished();
    }

    /**
     * 记录连接关闭
     */
    protected void recordConnectionClosed() {
        statisticsCollector.onConnectionClosed();
    }

    // ========== 统计信息获取方法 ==========

    /**
     * 获取活动连接数
     */
    public int getActiveConnections() {
        return statisticsCollector.getActiveConnections();
    }

    /**
     * 获取总客户端数
     */
    public int getTotalClients() {
        return statisticsCollector.getTotalClients();
    }

    /**
     * 获取总请求数
     */
    public long getTotalRequests() {
        return statisticsCollector.getTotalRequests();
    }

    /**
     * 获取已处理请求数
     */
    public long getTotalProcessedRequests() {
        return statisticsCollector.getTotalProcessedRequests();
    }

    /**
     * 获取错误请求数
     */
    public long getErrorRequestCount() {
        return statisticsCollector.getErrorRequestCount();
    }

    /**
     * 获取活动请求数
     */
    public long getActiveRequestCount() {
        return statisticsCollector.getActiveRequestCount();
    }

    /**
     * 获取待处理请求数
     */
    public long getPendingRequestCount() {
        return statisticsCollector.getPendingRequestCount();
    }

    /**
     * 获取平均处理时间
     */
    public double getAverageProcessingTime() {
        return statisticsCollector.getAverageProcessingTime();
    }

    /**
     * 重置统计信息
     */
    public void resetStatistics() {
        statisticsCollector.resetStatistics();
        log.debug("📊 Statistics reset for {}", getClass().getSimpleName());
    }

    /**
     * 获取完整统计信息
     */
    @NotNull
    public StatisticsCollector.StatisticsInfo getStatistics() {
        return statisticsCollector.getStatistics();
    }
}
