package com.dtc.ioc.core;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;

/**
 * Bean表达式上下文接口
 * 提供表达式评估的上下文信息
 * 借鉴Spring BeanExpressionContext的设计
 * 
 * @author Network Service Template
 */
public interface BeanExpressionContext {
    
    /**
     * 获取Bean实例
     * 
     * @param name Bean名称
     * @return Bean实例
     */
    @Nullable
    Object getBean(@NotNull String name);
    
    /**
     * 获取Bean实例（指定类型）
     * 
     * @param name Bean名称
     * @param requiredType 必需类型
     * @return Bean实例
     */
    @Nullable
    <T> T getBean(@NotNull String name, @NotNull Class<T> requiredType);
    
    /**
     * 检查Bean是否存在
     * 
     * @param name Bean名称
     * @return 是否存在
     */
    boolean containsBean(@NotNull String name);
}
