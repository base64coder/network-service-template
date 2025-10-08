package com.dtc.ioc.core;

import com.dtc.api.annotations.NotNull;

/**
 * Bean工厂后处理器接口
 * 在Bean工厂初始化后进行处理
 * 借鉴Spring BeanFactoryPostProcessor的设计
 * 
 * @author Network Service Template
 */
public interface BeanFactoryPostProcessor {
    
    /**
     * 处理Bean工厂
     * 
     * @param beanFactory Bean工厂
     */
    void postProcessBeanFactory(@NotNull ConfigurableBeanFactory beanFactory);
}
