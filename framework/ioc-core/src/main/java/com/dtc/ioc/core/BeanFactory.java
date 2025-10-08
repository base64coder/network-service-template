package com.dtc.ioc.core;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;

/**
 * Bean工厂接口
 * 管理Bean的创建和配置
 * 借鉴Spring BeanFactory的设计
 * 
 * @author Network Service Template
 */
public interface BeanFactory {
    
    /**
     * 获取Bean实例
     * 
     * @param name Bean名称
     * @return Bean实例
     */
    @Nullable
    Object getBean(String name);
    
    /**
     * 获取Bean实例（指定类型）
     * 
     * @param name Bean名称
     * @param requiredType 必需类型
     * @return Bean实例
     */
    @Nullable
    <T> T getBean(String name, Class<T> requiredType);
    
    /**
     * 获取Bean实例（指定类型）
     * 
     * @param requiredType 必需类型
     * @return Bean实例
     */
    @Nullable
    <T> T getBean(Class<T> requiredType);
    
    /**
     * 检查Bean是否存在
     * 
     * @param name Bean名称
     * @return 是否存在
     */
    boolean containsBean(String name);
    
    /**
     * 检查Bean是否为单例
     * 
     * @param name Bean名称
     * @return 是否为单例
     */
    boolean isSingleton(String name);
    
    /**
     * 获取Bean类型
     * 
     * @param name Bean名称
     * @return Bean类型
     */
    @Nullable
    Class<?> getType(String name);
    
    /**
     * 获取Bean别名
     * 
     * @param name Bean名称
     * @return 别名数组
     */
    @NotNull
    String[] getAliases(String name);
    
    /**
     * 预实例化单例Bean
     */
    void preInstantiateSingletons();
    
    /**
     * 销毁单例Bean
     */
    void destroySingletons();
}
