package com.dtc.ioc.core;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import java.util.List;
import java.util.Map;

/**
 * 可配置Bean工厂接口
 * 提供Bean工厂的配置功能
 * 借鉴Spring ConfigurableBeanFactory的设计
 * 
 * @author Network Service Template
 */
public interface ConfigurableBeanFactory extends BeanFactory {
    
    /**
     * 设置Bean类加载器
     * 
     * @param beanClassLoader Bean类加载器
     */
    void setBeanClassLoader(ClassLoader beanClassLoader);
    
    /**
     * 设置Bean表达式解析器
     * 
     * @param resolver 表达式解析器
     */
    void setBeanExpressionResolver(BeanExpressionResolver resolver);
    
    /**
     * 添加属性编辑器注册器
     * 
     * @param registrar 属性编辑器注册器
     */
    void addPropertyEditorRegistrar(PropertyEditorRegistrar registrar);
    
    /**
     * 添加Bean后处理器
     * 
     * @param beanPostProcessor Bean后处理器
     */
    void addBeanPostProcessor(BeanPostProcessor beanPostProcessor);
    
    /**
     * 获取Bean后处理器数量
     * 
     * @return 后处理器数量
     */
    int getBeanPostProcessorCount();
    
    /**
     * 注册Bean定义
     * 
     * @param beanName Bean名称
     * @param beanDefinition Bean定义
     */
    void registerBeanDefinition(String beanName, BeanDefinition beanDefinition);
    
    /**
     * 获取Bean定义
     * 
     * @param beanName Bean名称
     * @return Bean定义
     */
    @Nullable
    BeanDefinition getBeanDefinition(String beanName);
    
    /**
     * 获取所有Bean定义
     * 
     * @return Bean定义映射
     */
    @NotNull
    Map<String, BeanDefinition> getBeanDefinitions();
    
    /**
     * 获取Bean定义名称
     * 
     * @return Bean定义名称数组
     */
    @NotNull
    String[] getBeanDefinitionNames();
    
    /**
     * 注册单例Bean
     * 
     * @param beanName Bean名称
     * @param singletonObject 单例对象
     */
    void registerSingleton(String beanName, Object singletonObject);
    
    /**
     * 获取单例Bean
     * 
     * @param beanName Bean名称
     * @return 单例Bean
     */
    @Nullable
    Object getSingleton(String beanName);
    
    /**
     * 添加单例Bean
     * 
     * @param beanName Bean名称
     * @param singletonObject 单例对象
     */
    void addSingleton(String beanName, Object singletonObject);
    
    /**
     * 获取单例互斥锁
     * 
     * @return 互斥锁对象
     */
    @NotNull
    Object getSingletonMutex();
    
    /**
     * 获取Bean后处理器列表
     * 
     * @return Bean后处理器列表
     */
    @NotNull
    List<BeanPostProcessor> getBeanPostProcessors();
    
    /**
     * 获取Bean类加载器
     * 
     * @return Bean类加载器
     */
    @Nullable
    ClassLoader getBeanClassLoader();
    
    /**
     * 获取Bean表达式解析器
     * 
     * @return Bean表达式解析器
     */
    @Nullable
    BeanExpressionResolver getBeanExpressionResolver();
    
    /**
     * 获取属性编辑器注册表
     * 
     * @return 属性编辑器注册表
     */
    @Nullable
    PropertyEditorRegistry getPropertyEditorRegistry();
    
    /**
     * 设置属性编辑器注册表
     * 
     * @param propertyEditorRegistry 属性编辑器注册表
     */
    void setPropertyEditorRegistry(PropertyEditorRegistry propertyEditorRegistry);
    
    /**
     * 销毁Bean
     * 
     * @param beanName Bean名称
     * @param beanInstance Bean实例
     * @param definition Bean定义
     */
    void destroyBean(String beanName, Object beanInstance, BeanDefinition definition);
    
    /**
     * 清理Bean定义
     */
    void clearBeanDefinitions();
}
