package com.dtc.core.framework.ioc.factory;

import com.dtc.core.framework.ioc.exception.BeansException;

public interface BeanFactory {
    Object getBean(String name) throws BeansException;
    
    <T> T getBean(String name, Class<T> requiredType) throws BeansException;
    
    <T> T getBean(Class<T> requiredType) throws BeansException;
    
    boolean containsBean(String name);
    
    boolean isSingleton(String name) throws BeansException;
    
    boolean isPrototype(String name) throws BeansException;
}

