package com.dtc.ioc.core;

import com.dtc.api.annotations.NotNull;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * 依赖注入器接口
 * 借鉴 Guice 的依赖注入机制
 * 
 * @author Network Service Template
 */
public interface DependencyInjector {
    
    /**
     * 注入依赖
     * 
     * @param bean Bean 实例
     * @param definition Bean 定义
     */
    void injectDependencies(Object bean, BeanDefinition definition);
    
    /**
     * 注入字段依赖
     * 
     * @param bean Bean 实例
     * @param beanClass Bean 类型
     */
    void injectFieldDependencies(Object bean, Class<?> beanClass);
    
    /**
     * 注入构造函数依赖
     * 
     * @param constructor 构造函数
     * @param args 参数
     * @return Bean 实例
     */
    Object createBeanWithConstructor(Constructor<?> constructor, Object[] args);
    
    /**
     * 注入方法依赖
     * 
     * @param bean Bean 实例
     * @param beanClass Bean 类型
     */
    void injectMethodDependencies(Object bean, Class<?> beanClass);
}
