package com.dtc.core.extensions;

/**
 * 支持优雅关闭的扩展接口
 * 提供扩展关闭时的优雅处理和清理功能
 * 
 * @author Network Service Template
 */
public interface GracefulShutdownExtension {

    /**
     * 准备关闭扩展
     * 当扩展关闭时调用，用于优雅关闭和清理资源
     * 
     * @throws Exception 准备关闭过程中的异常
     */
    void prepareForShutdown() throws Exception;

    /**
     * 检查是否可以安全关闭
     * 
     * @return 是否可以安全关闭
     */
    boolean canShutdownSafely();

    /**
     * 获取活动请求的数量
     * 
     * @return 活动请求的数量
     */
    long getActiveRequestCount();

    /**
     * 等待所有请求完成
     * 
     * @param timeoutMs 超时时间（毫秒）
     * @return 是否所有请求都已完成
     */
    boolean waitForRequestsToComplete(long timeoutMs);
}
