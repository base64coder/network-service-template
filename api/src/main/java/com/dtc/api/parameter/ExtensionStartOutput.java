package com.dtc.api.parameter;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import java.util.Optional;

/**
 * 扩展启动输出参数
 * 
 * @author Network Service Template
 */
public interface ExtensionStartOutput {

    /**
     * 设置启动失败原因
     * 
     * @param reason 失败原因
     */
    void preventStartup(@NotNull String reason);

    /**
     * 获取启动失败原因
     * 
     * @return 失败原因，如果启动成功则返回空
     */
    @NotNull
    Optional<String> getReason();

    /**
     * 设置扩展配置
     * 
     * @param key   配置键
     * @param value 配置值
     */
    void setConfiguration(@NotNull String key, @NotNull String value);

    /**
     * 获取扩展配置
     * 
     * @param key 配置键
     * @return 配置值
     */
    @Nullable
    String getConfiguration(@NotNull String key);
}
