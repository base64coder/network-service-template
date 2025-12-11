package com.dtc.framework.beans.factory;

import com.dtc.framework.beans.exception.BeansException;

public interface BeanFactory {
    Object getBean(String name) throws BeansException;
    
    <T> T getBean(String name, Class<T> requiredType) throws BeansException;
    
    <T> T getBean(Class<T> requiredType) throws BeansException;
    
    boolean containsBean(String name);
    
    boolean isSingleton(String name) throws BeansException;
    
    boolean isPrototype(String name) throws BeansException;
}

