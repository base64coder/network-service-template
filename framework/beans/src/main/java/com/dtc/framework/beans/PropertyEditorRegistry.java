package com.dtc.framework.beans;

import com.dtc.api.annotations.NotNull;
import java.beans.PropertyEditor;

/**
     * å±æ§ç¼è¾å¨æ³¨åè¡¨æ¥å£
ç®¡çå±æ§ç¼è¾å¨
åé´Spring PropertyEditorRegistryçè®¾è®¡
@author Network Service Template
/
public interface PropertyEditorRegistry {
    
    /**
     * æ³¨åå±æ§ç¼è¾å¨
@param requiredType å¿éç±»å
@param propertyEditor å±æ§ç¼è¾å¨
/
    void registerCustomEditor(@NotNull Class<?> requiredType, @NotNull PropertyEditor propertyEditor);
    
    /**
     * æ³¨åå±æ§ç¼è¾å¨ï¼æå®å±æ§è·¯å¾ï¼
@param requiredType å¿éç±»å
@param propertyPath å±æ§è·¯å¾
@param propertyEditor å±æ§ç¼è¾å¨
/
    void registerCustomEditor(@NotNull Class<?> requiredType, @NotNull String propertyPath, @NotNull PropertyEditor propertyEditor);
    
    /**
     * æ¥æ¾å±æ§ç¼è¾å¨
@param requiredType å¿éç±»å
@return å±æ§ç¼è¾å¨
/
    PropertyEditor findCustomEditor(@NotNull Class<?> requiredType);
    
    /**
     * æ¥æ¾å±æ§ç¼è¾å¨ï¼æå®å±æ§è·¯å¾ï¼
@param requiredType å¿éç±»å
@param propertyPath å±æ§è·¯å¾
@return å±æ§ç¼è¾å¨
/
    PropertyEditor findCustomEditor(@NotNull Class<?> requiredType, @NotNull String propertyPath);
}
