package com.dtc.ioc.core;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import java.util.Map;

/**
 * 网络应用上下文接口
 * 提供应用上下文功能，支持模块化配置
 * 
 * @author Network Service Template
 */
public interface NetApplicationContext {
    
    /**
     * 获取Bean实例
     * @param beanType Bean类型
     * @return Bean实例
     */
    @Nullable
    <T> T getBean(Class<T> beanType);
    
    /**
     * 根据名称获取Bean实例
     * @param beanName Bean名称
     * @return Bean实例
     */
    @Nullable
    Object getBean(String beanName);
    
    /**
     * 根据名称和类型获取Bean实例
     * @param beanName Bean名称
     * @param beanType Bean类型
     * @return Bean实例
     */
    @Nullable
    <T> T getBean(String beanName, Class<T> beanType);
    
    /**
     * 获取指定类型的所有Bean实例
     * @param beanType Bean类型
     * @return Bean实例映射
     */
    @NotNull
    <T> Map<String, T> getBeansOfType(Class<T> beanType);
    
    /**
     * 检查Bean是否存在
     * @param beanName Bean名称
     * @return 是否存在
     */
    boolean containsBean(String beanName);
    
    /**
     * 检查Bean是否为单例
     * @param beanName Bean名称
     * @return 是否为单例
     */
    boolean isSingleton(String beanName);
    
    /**
     * 获取Bean的类型
     * @param beanName Bean名称
     * @return Bean类型
     */
    @Nullable
    Class<?> getType(String beanName);
    
    /**
     * 获取所有Bean名称
     * @return Bean名称数组
     */
    @NotNull
    String[] getBeanDefinitionNames();
    
    /**
     * 刷新容器
     */
    void refresh();
    
    /**
     * 关闭容器
     */
    void close();
    
    /**
     * 检查容器是否活跃
     * @return 是否活跃
     */
    boolean isActive();
    
    /**
     * 注册Bean定义
     * @param beanName Bean名称
     * @param beanClass Bean类型
     */
    void registerBean(String beanName, Class<?> beanClass);
    
    /**
     * 注册Bean实例
     * @param beanName Bean名称
     * @param beanInstance Bean实例
     */
    void registerBean(String beanName, Object beanInstance);
    
    /**
     * 发布应用事件
     * @param event 应用事件
     */
    void publishEvent(ApplicationEvent event);
    
    /**
     * 添加应用监听器
     * @param listener 应用监听器
     */
    void addApplicationListener(ApplicationListener<?> listener);
    
    /**
     * 添加Bean后处理器
     * @param beanPostProcessor Bean后处理器
     */
    void addBeanPostProcessor(BeanPostProcessor beanPostProcessor);
    
    /**
     * 添加Bean工厂后处理器
     * @param beanFactoryPostProcessor Bean工厂后处理器
     */
    void addBeanFactoryPostProcessor(BeanFactoryPostProcessor beanFactoryPostProcessor);
}
