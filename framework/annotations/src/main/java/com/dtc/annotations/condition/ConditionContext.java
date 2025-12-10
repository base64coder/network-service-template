package com.dtc.annotations.condition;

import java.util.Map;

/**
 * 条件上下文接口
 * 提供条件判断所需的上下文信息
 * 
 * @author Network Service Template
 */
public interface ConditionContext {
    /**
     * 获取Bean定义注册表
     * @return Bean定义注册表
     */
    Object getRegistry();
    
    /**
     * 获取环境配置
     * @return 环境配置
     */
    Object getEnvironment();
    
    /**
     * 获取属性值
     * @param key 属性键
     * @return 属性值
     */
    String getProperty(String key);
    
    /**
     * 获取属性值（带默认值）
     * @param key 属性键
     * @param defaultValue 默认值
     * @return 属性值
     */
    String getProperty(String key, String defaultValue);
    
    /**
     * 检查Bean是否存在
     * @param beanName Bean名称
     * @return 是否存在
     */
    boolean containsBean(String beanName);
    
    /**
     * 获取所有属性
     * @return 属性映射
     */
    Map<String, String> getAllProperties();
}
