package com.dtc.ioc.core;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;

/**
     * Beanåå¤çå¨æ¥å£
å¨Beanåå§åååè¿è¡å¤ç
åé´Spring BeanPostProcessorçè®¾è®¡
@author Network Service Template
/
public interface BeanPostProcessor {
    
    /**
     * Beanåå§ååå¤ç
@param bean Beanå®ä¾
@param beanName Beanåç§°
@return å¤çåçBeanå®ä¾
/
    @Nullable
    Object postProcessBeforeInitialization(@NotNull Object bean, @NotNull String beanName);
    
    /**
     * Beanåå§ååå¤ç
@param bean Beanå®ä¾
@param beanName Beanåç§°
@return å¤çåçBeanå®ä¾
/
    @Nullable
    Object postProcessAfterInitialization(@NotNull Object bean, @NotNull String beanName);
}
