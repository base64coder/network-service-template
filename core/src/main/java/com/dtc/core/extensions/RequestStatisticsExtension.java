package com.dtc.core.extensions;

import com.dtc.api.annotations.NotNull;

/**
 * 请求统计扩展接口
 * 提供请求处理统计信息
 * 
 * @author Network Service Template
 */
public interface RequestStatisticsExtension {

    /**
     * 获取待处理请求数量
     * 
     * @return 待处理请求数量
     */
    int getPendingRequestCount();

    /**
     * 获取正在处理的请求数量
     * 
     * @return 正在处理的请求数量
     */
    int getActiveRequestCount();

    /**
     * 获取总处理请求数量
     * 
     * @return 总处理请求数量
     */
    long getTotalProcessedRequests();

    /**
     * 获取错误请求数量
     * 
     * @return 错误请求数量
     */
    long getErrorRequestCount();

    /**
     * 获取平均处理时间（毫秒）
     * 
     * @return 平均处理时间
     */
    double getAverageProcessingTime();

    /**
     * 重置统计信息
     */
    void resetStatistics();
}
