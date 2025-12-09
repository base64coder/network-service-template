package com.dtc.framework.beans.impl;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import com.dtc.framework.beans.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
     * é»è®¤ç¯å¢éç½®å®ç°
åé´Spring Environmentçè®¾è®¡
@author Network Service Template
/
public class DefaultEnvironment implements Environment {
    
    private static final Logger log = LoggerFactory.getLogger(DefaultEnvironment.class);
    
    // éç½®å±æ§å­å¨
    private final Map<String, Object> properties = new ConcurrentHashMap<>();
    
    public DefaultEnvironment() {
        // åå§åé»è®¤éç½®
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
     * è®¾ç½®éç½®å±æ§
@param key å±æ§é®
@param value å±æ§å¼
/
    public void setProperty(String key, Object value) {
        properties.put(key, value);
        log.debug("ð§ Property set: {} = {}", key, value);
    }
    
    /**
     * åå§åé»è®¤éç½®
/
    private void initializeDefaultProperties() {
        // è®¾ç½®é»è®¤éç½®
        setProperty("application.name", "Network Service Template");
        setProperty("application.version", "1.0.0");
        setProperty("server.port", 8080);
        setProperty("server.host", "localhost");
        setProperty("logging.level", "INFO");
        
        log.debug("ð§ Default properties initialized");
    }
}
