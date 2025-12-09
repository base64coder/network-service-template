package com.dtc.ioc.core;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import java.util.Map;

/**
     * ç¯å¢éç½®æ¥å£
ç®¡çåºç¨éç½®ä¿¡æ¯
åé´Spring Environmentçè®¾è®¡
@author Network Service Template
/
public interface Environment {
    
    /**
     * è·åéç½®å±æ§
@param key å±æ§é®
@return å±æ§å¼
/
    @Nullable
    String getProperty(String key);
    
    /**
     * è·åéç½®å±æ§ï¼å¸¦é»è®¤å¼ï¼
@param key å±æ§é®
@param defaultValue é»è®¤å¼
@return å±æ§å¼
/
    @NotNull
    String getProperty(String key, String defaultValue);
    
    /**
     * è·åéç½®å±æ§ï¼æå®ç±»åï¼
@param key å±æ§é®
@param targetType ç®æ ç±»å
@return å±æ§å¼
/
    @Nullable
    <T> T getProperty(String key, Class<T> targetType);
    
    /**
     * è·åéç½®å±æ§ï¼æå®ç±»åï¼å¸¦é»è®¤å¼ï¼
@param key å±æ§é®
@param targetType ç®æ ç±»å
@param defaultValue é»è®¤å¼
@return å±æ§å¼
/
    @NotNull
    <T> T getProperty(String key, Class<T> targetType, T defaultValue);
    
    /**
     * æ£æ¥éç½®å±æ§æ¯å¦å­å¨
@param key å±æ§é®
@return æ¯å¦å­å¨
/
    boolean containsProperty(String key);
    
    /**
     * è·åææéç½®å±æ§
@return éç½®å±æ§æ å°
/
    @NotNull
    Map<String, Object> getAllProperties();
}
