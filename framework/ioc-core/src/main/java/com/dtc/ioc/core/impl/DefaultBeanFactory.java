package com.dtc.ioc.core.impl;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import com.dtc.ioc.core.BeanContainer;
import com.dtc.ioc.core.BeanDefinition;
import com.dtc.ioc.core.BeanFactory;
import com.dtc.ioc.core.DependencyInjector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 默认 Bean 工厂实现
 * 借鉴 Spring BeanFactory 的设计
 * 
 * @author Network Service Template
 */
public class DefaultBeanFactory implements BeanFactory {
    
    private static final Logger log = LoggerFactory.getLogger(DefaultBeanFactory.class);
    
    private final BeanContainer beanContainer;
    private final DependencyInjector dependencyInjector;
    private final Map<String, BeanDefinition> beanDefinitions = new HashMap<>();
    private final Map<String, Object> singletonBeans = new HashMap<>();
    private final Set<String> singletonBeanNames = new HashSet<>();
    
    public DefaultBeanFactory(@NotNull BeanContainer beanContainer, 
                             @NotNull DependencyInjector dependencyInjector) {
        this.beanContainer = beanContainer;
        this.dependencyInjector = dependencyInjector;
    }
    
    @Override
    @Nullable
    public Object getBean(@NotNull String name) {
        // 先从单例缓存中获取
        if (singletonBeans.containsKey(name)) {
            return singletonBeans.get(name);
        }
        
        // 从容器中获取
        Object bean = beanContainer.getBean(name);
        if (bean != null) {
            return bean;
        }
        
        // 根据 BeanDefinition 创建
        BeanDefinition definition = beanDefinitions.get(name);
        if (definition != null) {
            return createBean(definition);
        }
        
        return null;
    }
    
    @Override
    @Nullable
    @SuppressWarnings("unchecked")
    public <T> T getBean(@NotNull String name, @NotNull Class<T> requiredType) {
        Object bean = getBean(name);
        if (bean != null && requiredType.isInstance(bean)) {
            return (T) bean;
        }
        return null;
    }
    
    @Override
    @Nullable
    @SuppressWarnings("unchecked")
    public <T> T getBean(@NotNull Class<T> requiredType) {
        // 先从容器中按类型获取
        T bean = beanContainer.getBean(requiredType);
        if (bean != null) {
            return bean;
        }
        
        // 遍历所有 BeanDefinition，查找匹配的类型
        for (BeanDefinition definition : beanDefinitions.values()) {
            if (requiredType.isAssignableFrom(definition.getBeanClass())) {
                Object instance = getBean(definition.getBeanName());
                if (instance != null && requiredType.isInstance(instance)) {
                    return (T) instance;
                }
            }
        }
        
        return null;
    }
    
    @Override
    public boolean containsBean(@NotNull String name) {
        return singletonBeans.containsKey(name) || 
               beanContainer.containsBean(name) || 
               beanDefinitions.containsKey(name);
    }
    
    @Override
    public boolean isSingleton(@NotNull String name) {
        BeanDefinition definition = beanDefinitions.get(name);
        if (definition != null) {
            return definition.isSingleton();
        }
        return singletonBeanNames.contains(name);
    }
    
    @Override
    @Nullable
    public Class<?> getType(@NotNull String name) {
        BeanDefinition definition = beanDefinitions.get(name);
        if (definition != null) {
            return definition.getBeanClass();
        }
        
        Object bean = getBean(name);
        if (bean != null) {
            return bean.getClass();
        }
        
        return null;
    }
    
    @Override
    @NotNull
    public String[] getAliases(@NotNull String name) {
        // 简单实现，返回空数组
        return new String[0];
    }
    
    @Override
    public void preInstantiateSingletons() {
        log.info("Pre-instantiating singleton beans...");
        
        for (BeanDefinition definition : beanDefinitions.values()) {
            if (definition.isSingleton() && !definition.isLazyInit()) {
                String beanName = definition.getBeanName();
                if (!singletonBeans.containsKey(beanName)) {
                    Object bean = createBean(definition);
                    if (bean != null) {
                        singletonBeans.put(beanName, bean);
                        singletonBeanNames.add(beanName);
                        beanContainer.registerBean(beanName, bean);
                        log.debug("Pre-instantiated singleton bean: {}", beanName);
                    }
                }
            }
        }
        
        log.info("Pre-instantiation complete. Total singletons: {}", singletonBeans.size());
    }
    
    @Override
    public void destroySingletons() {
        log.info("Destroying singleton beans...");
        
        // 调用销毁方法
        for (Map.Entry<String, Object> entry : singletonBeans.entrySet()) {
            String beanName = entry.getKey();
            Object bean = entry.getValue();
            BeanDefinition definition = beanDefinitions.get(beanName);
            
            if (definition != null && definition.getDestroyMethodName() != null) {
                try {
                    java.lang.reflect.Method destroyMethod = definition.getBeanClass()
                            .getMethod(definition.getDestroyMethodName());
                    destroyMethod.invoke(bean);
                    log.debug("Destroyed bean: {}", beanName);
                } catch (Exception e) {
                    log.error("Error destroying bean: {}", beanName, e);
                }
            }
        }
        
        singletonBeans.clear();
        singletonBeanNames.clear();
        log.info("All singleton beans destroyed");
    }
    
    /**
     * 注册 BeanDefinition
     */
    public void registerBeanDefinition(@NotNull String beanName, @NotNull BeanDefinition definition) {
        beanDefinitions.put(beanName, definition);
        log.debug("Registered bean definition: {} -> {}", beanName, definition.getBeanClass().getName());
    }
    
    /**
     * 获取 BeanDefinition
     */
    @Nullable
    public BeanDefinition getBeanDefinition(@NotNull String beanName) {
        return beanDefinitions.get(beanName);
    }
    
    /**
     * 创建 Bean 实例
     */
    @Nullable
    private Object createBean(@NotNull BeanDefinition definition) {
        try {
            Class<?> beanClass = definition.getBeanClass();
            
            // 使用构造函数创建实例
            Object bean;
            if (definition.getConstructor() != null) {
                // 解析构造函数参数
                Object[] args = resolveConstructorArguments(definition.getConstructor());
                bean = dependencyInjector.createBeanWithConstructor(definition.getConstructor(), args);
            } else {
                // 使用默认构造函数
                bean = beanClass.getDeclaredConstructor().newInstance();
            }
            
            // 注入依赖
            dependencyInjector.injectDependencies(bean, definition);
            
            // 调用初始化方法
            if (definition.getInitMethodName() != null) {
                java.lang.reflect.Method initMethod = beanClass.getMethod(definition.getInitMethodName());
                initMethod.invoke(bean);
            }
            
            // 如果是单例，缓存起来
            if (definition.isSingleton()) {
                singletonBeans.put(definition.getBeanName(), bean);
                singletonBeanNames.add(definition.getBeanName());
            }
            
            // 注册到容器
            beanContainer.registerBean(definition.getBeanName(), bean);
            
            log.debug("Created bean: {} -> {}", definition.getBeanName(), beanClass.getName());
            return bean;
            
        } catch (Exception e) {
            log.error("Failed to create bean: {}", definition.getBeanName(), e);
            return null;
        }
    }
    
    /**
     * 解析构造函数参数
     */
    @NotNull
    private Object[] resolveConstructorArguments(@NotNull java.lang.reflect.Constructor<?> constructor) {
        Class<?>[] paramTypes = constructor.getParameterTypes();
        Object[] args = new Object[paramTypes.length];
        
        for (int i = 0; i < paramTypes.length; i++) {
            Class<?> paramType = paramTypes[i];
            // 尝试从容器中获取依赖
            Object dependency = beanContainer.getBean(paramType);
            if (dependency != null) {
                args[i] = dependency;
            } else {
                // 如果找不到，尝试创建
                args[i] = null; // 或者抛出异常
            }
        }
        
        return args;
    }
}

