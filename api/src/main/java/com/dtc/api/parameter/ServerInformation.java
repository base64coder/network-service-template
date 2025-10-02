package com.dtc.api.parameter;

import com.dtc.api.annotations.NotNull;

import java.nio.file.Path;
import java.util.Map;

/**
 * 服务器信息接口
 * 
 * @author Network Service Template
 */
public interface ServerInformation {

    /**
     * 获取服务器名称
     * 
     * @return 服务器名称
     */
    @NotNull
    String getServerName();

    /**
     * 获取服务器版本
     * 
     * @return 服务器版本
     */
    @NotNull
    String getServerVersion();

    /**
     * 获取服务器ID
     * 
     * @return 服务器ID
     */
    @NotNull
    String getServerId();

    /**
     * 获取数据文件夹路径
     * 
     * @return 数据文件夹路径
     */
    @NotNull
    Path getDataFolder();

    /**
     * 获取配置文件夹路径
     * 
     * @return 配置文件夹路径
     */
    @NotNull
    Path getConfigFolder();

    /**
     * 获取扩展文件夹路径
     * 
     * @return 扩展文件夹路径
     */
    @NotNull
    Path getExtensionsFolder();

    /**
     * 获取系统属性
     * 
     * @return 系统属性映射
     */
    @NotNull
    Map<String, String> getSystemProperties();

    /**
     * 获取环境变量
     * 
     * @return 环境变量映射
     */
    @NotNull
    Map<String, String> getEnvironmentVariables();

    /**
     * 是否运行在嵌入式模式
     * 
     * @return 是否嵌入式模式
     */
    boolean isEmbedded();
}
