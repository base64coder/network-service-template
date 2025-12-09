package com.dtc.ioc.core;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;

/**
     * Beanè¡¨è¾¾å¼ä¸ä¸ææ¥å£
æä¾è¡¨è¾¾å¼è¯ä¼°çä¸ä¸æä¿¡æ¯
åé´Spring BeanExpressionContextçè®¾è®¡
@author Network Service Template
/
public interface BeanExpressionContext {
    
    /**
     * è·åBeanå®ä¾
@param name Beanåç§°
@return Beanå®ä¾
/
    @Nullable
    Object getBean(@NotNull String name);
    
    /**
     * è·åBeanå®ä¾ï¼æå®ç±»åï¼
@param name Beanåç§°
@param requiredType å¿éç±»å
@return Beanå®ä¾
/
    @Nullable
    <T> T getBean(@NotNull String name, @NotNull Class<T> requiredType);
    
    /**
     * æ£æ¥Beanæ¯å¦å­å¨
@param name Beanåç§°
@return æ¯å¦å­å¨
/
    boolean containsBean(@NotNull String name);
}
