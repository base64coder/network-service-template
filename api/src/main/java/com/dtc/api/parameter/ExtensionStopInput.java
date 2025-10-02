package com.dtc.api.parameter;

import com.dtc.api.annotations.NotNull;

import java.nio.file.Path;
import java.util.Map;

/**
 * 扩展停止输入参数
 * 
 * @author Network Service Template
 */
public interface ExtensionStopInput {

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
