package com.dtc.api.parameter;

import com.dtc.api.annotations.NotNull;

import java.util.Optional;

/**
 * 扩展停止输出参数
 * 
 * @author Network Service Template
 */
public interface ExtensionStopOutput {

    /**
     * 设置停止失败原因
     * 
     * @param reason 失败原因
     */
    void preventStop(@NotNull String reason);

    /**
     * 获取停止失败原因
     * 
     * @return 失败原因，如果停止成功则返回空
     */
    @NotNull
    Optional<String> getReason();

    /**
     * 设置清理延迟时间（毫秒）
     * 
     * @param delayMs 延迟时间
     */
    void setCleanupDelay(long delayMs);

    /**
     * 获取清理延迟时间
     * 
     * @return 延迟时间
     */
    long getCleanupDelay();
}
