package com.dtc.framework.beans;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;

/**
 * Bean 工厂接口
 * 管理 Bean 的创建和配置
 * 借鉴 Spring BeanFactory 的设计
 * 
 * @author Network Service Template
 */
public interface BeanFactory {
    
    /**
     * 获取 Bean 实例
     * 
     * @param name Bean 名称
     * @return Bean 实例
     */
    @Nullable
    Object getBean(String name);
    
    /**
     * 获取 Bean 实例（指定类型）
     * 
     * @param name Bean 名称
     * @param requiredType 必需类型
     * @return Bean 实例
     */
    @Nullable
    <T> T getBean(String name, Class<T> requiredType);
    
    /**
     * 获取 Bean 实例（指定类型）
     * 
     * @param requiredType 必需类型
     * @return Bean 实例
     */
    @Nullable
    <T> T getBean(Class<T> requiredType);
    
    /**
     * 检查 Bean 是否存在
     * 
     * @param name Bean 名称
     * @return 是否存在
     */
    boolean containsBean(String name);
    
    /**
     * 检查 Bean 是否为单例
     * 
     * @param name Bean 名称
     * @return 是否为单例
     */
    boolean isSingleton(String name);
    
    /**
     * 获取 Bean 类型
     * 
     * @param name Bean 名称
     * @return Bean 类型
     */
    @Nullable
    Class<?> getType(String name);
    
    /**
     * 获取 Bean 别名
     * 
     * @param name Bean 名称
     * @return 别名数组
     */
    @NotNull
    String[] getAliases(String name);
    
    /**
     * 预实例化单例 Bean
     */
    void preInstantiateSingletons();
    
    /**
     * 销毁单例 Bean
     */
    void destroySingletons();
}
