package com.dtc.ioc.core;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import java.util.Map;

/**
 * 环境配置接口
 * 管理应用配置信息
 * 借鉴Spring Environment的设计
 * 
 * @author Network Service Template
 */
public interface Environment {
    
    /**
     * 获取配置属性
     * @param key 属性键
     * @return 属性值
     */
    @Nullable
    String getProperty(String key);
    
    /**
     * 获取配置属性（带默认值）
     * @param key 属性键
     * @param defaultValue 默认值
     * @return 属性值
     */
    @NotNull
    String getProperty(String key, String defaultValue);
    
    /**
     * 获取配置属性（指定类型）
     * @param key 属性键
     * @param targetType 目标类型
     * @return 属性值
     */
    @Nullable
    <T> T getProperty(String key, Class<T> targetType);
    
    /**
     * 获取配置属性（指定类型，带默认值）
     * @param key 属性键
     * @param targetType 目标类型
     * @param defaultValue 默认值
     * @return 属性值
     */
    @NotNull
    <T> T getProperty(String key, Class<T> targetType, T defaultValue);
    
    /**
     * 检查配置属性是否存在
     * @param key 属性键
     * @return 是否存在
     */
    boolean containsProperty(String key);
    
    /**
     * 获取所有配置属性
     * @return 配置属性映射
     */
    @NotNull
    Map<String, Object> getAllProperties();
}
