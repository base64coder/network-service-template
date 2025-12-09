package com.dtc.framework.beans;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;

/**
     * Beanè¡¨è¾¾å¼è§£æå¨æ¥å£
è§£æBeanè¡¨è¾¾å¼
åé´Spring BeanExpressionResolverçè®¾è®¡
@author Network Service Template
/
public interface BeanExpressionResolver {
    
    /**
     * è§£æè¡¨è¾¾å¼
@param value è¡¨è¾¾å¼å¼
@param evalContext è¯ä¼°ä¸ä¸æ
@return è§£æç»æ
/
    @Nullable
    Object evaluate(@NotNull String value, @NotNull BeanExpressionContext evalContext);
}
