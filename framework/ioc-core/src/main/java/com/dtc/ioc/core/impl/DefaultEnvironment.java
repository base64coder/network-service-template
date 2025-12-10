package com.dtc.ioc.core.impl;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import com.dtc.ioc.core.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认环境配置实现
 * 借鉴 Environment 的设计
 * 
 * @author Network Service Template
 */
public class DefaultEnvironment implements Environment {
    
    private static final Logger log = LoggerFactory.getLogger(DefaultEnvironment.class);
    
    // 配置属性存储
    private final Map<String, Object> properties = new ConcurrentHashMap<>();
    
    public DefaultEnvironment() {
        // 初始化默认配置
        initializeDefaultProperties();
    }
    
    @Override
    @Nullable
    public String getProperty(String key) {
        Object value = properties.get(key);
        return value != null ? value.toString() : null;
    }
    
    @Override
    @NotNull
    public String getProperty(String key, String defaultValue) {
        String value = getProperty(key);
        return value != null ? value : defaultValue;
    }
    
    @Override
    @Nullable
    @SuppressWarnings("unchecked")
    public <T> T getProperty(String key, Class<T> targetType) {
        Object value = properties.get(key);
        if (value != null && targetType.isAssignableFrom(value.getClass())) {
            return (T) value;
        }
        return null;
    }
    
    @Override
    @NotNull
    @SuppressWarnings("unchecked")
    public <T> T getProperty(String key, Class<T> targetType, T defaultValue) {
        T value = getProperty(key, targetType);
        return value != null ? value : defaultValue;
    }
    
    @Override
    public boolean containsProperty(String key) {
        return properties.containsKey(key);
    }
    
    @Override
    @NotNull
    public Map<String, Object> getAllProperties() {
        return new ConcurrentHashMap<>(properties);
    }
    
    /**
     * 设置配置属性
     * @param key 属性键
     * @param value 属性值
     */
    public void setProperty(String key, Object value) {
        properties.put(key, value);
        log.debug("⚙️ Property set: {} = {}", key, value);
    }
    
    /**
     * 初始化默认配置
     */
    private void initializeDefaultProperties() {
        // 设置默认配置
        setProperty("application.name", "Network Service Template");
        setProperty("application.version", "1.0.0");
        setProperty("server.port", 8080);
        setProperty("server.host", "localhost");
        setProperty("logging.level", "INFO");
        
        log.debug("⚙️ Default properties initialized");
    }
}
