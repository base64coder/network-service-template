package com.dtc.core.framework.ioc.factory;

import com.dtc.core.framework.ioc.exception.BeansException;

public interface BeanPostProcessor {
    // 初始化前（处理 @PostConstruct, Aware 等）
    default Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    // 初始化后（处理 AOP 代理生成）
    default Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
}

