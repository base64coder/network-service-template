package com.dtc.core.extensions;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import com.dtc.core.extensions.model.ExtensionMetadata;

import java.nio.file.Path;

/**
 * 网络扩展接口
 * 定义扩展的基本行为
 * 
 * @author Network Service Template
 */
public interface NetworkExtension {
    /**
     * 获取扩展ID
     * 
     * @return 扩展ID
     */
    @NotNull
    String getId();

    /**
     * 获取扩展名称
     * 
     * @return 扩展名称
     */
    @NotNull
    String getName();

    /**
     * 获取扩展版本
     * 
     * @return 扩展版本
     */
    @NotNull
    String getVersion();

    /**
     * 获取扩展作者
     * 
     * @return 扩展作者
     */
    @Nullable
    String getAuthor();

    /**
     * 获取扩展优先级
     * 
     * @return 扩展优先级
     */
    int getPriority();

    /**
     * 获取启动优先级
     * 
     * @return 启动优先级
     */
    int getStartPriority();

    /**
     * 获取扩展元数据
     * 
     * @return 扩展元数据
     */
    @NotNull
    ExtensionMetadata getMetadata();

    /**
     * 获取扩展文件夹路径
     * 
     * @return 扩展文件夹路径
     */
    @NotNull
    Path getExtensionFolderPath();

    /**
     * 获取扩展类加载器
     * 
     * @return 扩展类加载器
     */
    @Nullable
    ClassLoader getExtensionClassloader();

    /**
     * 启动扩展
     */
    void start() throws Exception;

    /**
     * 停止扩展
     */
    void stop() throws Exception;

    /**
     * 是否已启用
     * 
     * @return 是否已启用
     */
    boolean isEnabled();

    /**
     * 设置启用状态
     * 
     * @param enabled 是否启用
     */
    void setEnabled(boolean enabled);

    /**
     * 是否已启动
     * 
     * @return 是否已启动
     */
    boolean isStarted();

    /**
     * 是否已停止
     * 
     * @return 是否已停止
     */
    boolean isStopped();

    /**
     * 清理扩展资源
     * 
     * @param disable 是否禁用扩展
     */
    void cleanup(boolean disable);
}
