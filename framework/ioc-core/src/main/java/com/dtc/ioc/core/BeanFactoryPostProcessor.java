package com.dtc.ioc.core;

import com.dtc.api.annotations.NotNull;

/**
     * Beanå·¥ååå¤çå¨æ¥å£
å¨Beanå·¥ååå§ååè¿è¡å¤ç
åé´Spring BeanFactoryPostProcessorçè®¾è®¡
@author Network Service Template
/
public interface BeanFactoryPostProcessor {
    
    /**
     * å¤çBeanå·¥å
@param beanFactory Beanå·¥å
/
    void postProcessBeanFactory(@NotNull ConfigurableBeanFactory beanFactory);
}
