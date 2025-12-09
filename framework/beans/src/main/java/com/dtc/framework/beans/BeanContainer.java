package com.dtc.framework.beans;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import java.util.Map;
import java.util.Set;

/**
 * Bean 容器接口
 * 管理所有 Bean 的注册、获取和生命周期
 * 借鉴 Spring ApplicationContext 的设计
 * 
 * @author Network Service Template
 */
public interface BeanContainer {
    
    /**
     * 注册 Bean
     * 
     * @param beanName Bean 名称
     * @param bean Bean 实例
     */
    void registerBean(@NotNull String beanName, @NotNull Object bean);
    
    /**
     * 注册 Bean（自动命名）
     * 
     * @param bean Bean 实例
     * @return Bean 名称
     */
    @NotNull
    String registerBean(@NotNull Object bean);
    
    /**
     * 获取 Bean
     * 
     * @param beanName Bean 名称
     * @return Bean 实例
     */
    @Nullable
    Object getBean(@NotNull String beanName);
    
    /**
     * 获取 Bean（指定类型）
     * 
     * @param beanName Bean 名称
     * @param beanType Bean 类型
     * @return Bean 实例
     */
    @Nullable
    <T> T getBean(@NotNull String beanName, @NotNull Class<T> beanType);
    
    /**
     * 获取 Bean（按类型）
     * 
     * @param beanType Bean 类型
     * @return Bean 实例
     */
    @Nullable
    <T> T getBean(@NotNull Class<T> beanType);
    
    /**
     * 获取所有指定类型的 Bean
     * 
     * @param beanType Bean 类型
     * @return Bean 映射
     */
    @NotNull
    <T> Map<String, T> getBeansOfType(@NotNull Class<T> beanType);
    
    /**
     * 检查 Bean 是否存在
     * 
     * @param beanName Bean 名称
     * @return 是否存在
     */
    boolean containsBean(@NotNull String beanName);
    
    /**
     * 移除 Bean
     * 
     * @param beanName Bean 名称
     * @return 是否移除成功
     */
    boolean removeBean(@NotNull String beanName);
    
    /**
     * 获取所有 Bean 名称
     * 
     * @return Bean 名称集合
     */
    @NotNull
    Set<String> getBeanNames();
    
    /**
     * 获取所有 Bean
     * 
     * @return Bean 映射
     */
    @NotNull
    Map<String, Object> getAllBeans();
    
    /**
     * 清除所有 Bean
     */
    void clear();
}

