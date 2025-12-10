package com.dtc.api.parameter;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;

import java.nio.file.Path;
import java.util.Map;

/**
 * 扩展启动输入参数
 * 
 * @author Network Service Template
 */
public interface ExtensionStartInput {

    /**
     * 获取扩展ID
     * 
     * @return 扩展ID
     */
    @NotNull
    String getExtensionId();

    /**
     * 获取扩展名称
     * 
     * @return 扩展名称
     */
    @NotNull
    String getExtensionName();

    /**
     * 获取扩展版本
     * 
     * @return 扩展版本
     */
    @NotNull
    String getExtensionVersion();

    /**
     * 获取扩展作者
     * 
     * @return 扩展作者
     */
    @Nullable
    String getExtensionAuthor();

    /**
     * 获取扩展优先级
     * 
     * @return 扩展优先级
     */
    int getExtensionPriority();

    /**
     * 获取扩展文件夹路径
     * 
     * @return 扩展文件夹路径
     */
    @NotNull
    Path getExtensionFolderPath();

    /**
     * 获取服务器信息
     * 
     * @return 服务器信息
     */
    @NotNull
    ServerInformation getServerInformation();

    /**
     * 获取配置参数
     * 
     * @return 配置参数映射
     */
    @NotNull
    Map<String, String> getConfiguration();
}
