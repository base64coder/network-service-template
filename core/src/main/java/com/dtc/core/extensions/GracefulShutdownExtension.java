package com.dtc.core.extensions;

/**
 * 支持优雅关闭的扩展接口
 * 提供扩展停止前的准备和清理功能
 * 
 * @author Network Service Template
 */
public interface GracefulShutdownExtension {

    /**
     * 准备关闭扩展
     * 在扩展停止前调用，用于准备关闭工作
     * 
     * @throws Exception 准备关闭时发生异常
     */
    void prepareForShutdown() throws Exception;

    /**
     * 检查是否可以安全关闭
     * 
     * @return 是否可以安全关闭
     */
    boolean canShutdownSafely();

    /**
     * 获取正在处理的请求数量
     * 
     * @return 正在处理的请求数量
     */
    long getActiveRequestCount();

    /**
     * 等待所有请求处理完成
     * 
     * @param timeoutMs 超时时间（毫秒）
     * @return 是否所有请求都已完成
     */
    boolean waitForRequestsToComplete(long timeoutMs);
}
