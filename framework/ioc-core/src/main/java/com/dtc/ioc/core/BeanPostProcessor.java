package com.dtc.ioc.core;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;

/**
 * Bean后处理器接口
 * 在Bean初始化前后进行处理
 * 借鉴Spring BeanPostProcessor的设计
 * 
 * @author Network Service Template
 */
public interface BeanPostProcessor {
    
    /**
     * Bean初始化前处理
     * 
     * @param bean Bean实例
     * @param beanName Bean名称
     * @return 处理后的Bean实例
     */
    @Nullable
    Object postProcessBeforeInitialization(@NotNull Object bean, @NotNull String beanName);
    
    /**
     * Bean初始化后处理
     * 
     * @param bean Bean实例
     * @param beanName Bean名称
     * @return 处理后的Bean实例
     */
    @Nullable
    Object postProcessAfterInitialization(@NotNull Object bean, @NotNull String beanName);
}
