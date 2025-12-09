package com.dtc.ioc.core;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * Bean 定义接口
 * 描述 Bean 的元数据信息
 * 借鉴 Spring BeanDefinition 的设计
 * 
 * @author Network Service Template
 */
public interface BeanDefinition {
    
    /**
     * 获取 Bean 名称
     * 
     * @return Bean 名称
     */
    @NotNull
    String getBeanName();
    
    /**
     * 获取 Bean 类型
     * 
     * @return Bean 类型
     */
    @NotNull
    Class<?> getBeanClass();
    
    /**
     * 获取作用域
     * 
     * @return 作用域
     */
    @NotNull
    BeanScope getScope();
    
    /**
     * 是否为单例
     * 
     * @return 是否为单例
     */
    boolean isSingleton();
    
    /**
     * 是否为原型
     * 
     * @return 是否为原型
     */
    boolean isPrototype();
    
    /**
     * 是否为懒加载
     * 
     * @return 是否为懒加载
     */
    boolean isLazyInit();
    
    /**
     * 获取依赖的 Bean 名称列表
     * 
     * @return 依赖列表
     */
    @NotNull
    List<String> getDependsOn();
    
    /**
     * 获取初始化方法名称
     * 
     * @return 初始化方法名称
     */
    @Nullable
    String getInitMethodName();
    
    /**
     * 获取销毁方法名称
     * 
     * @return 销毁方法名称
     */
    @Nullable
    String getDestroyMethodName();
    
    /**
     * 获取构造函数
     * 
     * @return 构造函数
     */
    @Nullable
    Constructor<?> getConstructor();
    
    /**
     * 获取工厂方法
     * 
     * @return 工厂方法
     */
    @Nullable
    Method getFactoryMethod();
    
    /**
     * 获取属性值
     * 
     * @return 属性值映射
     */
    @NotNull
    Map<String, Object> getPropertyValues();
    
    /**
     * 获取注解元数据
     * 
     * @return 注解元数据映射
     */
    @NotNull
    Map<String, Object> getAnnotationMetadata();
}
