package com.dtc.ioc.core.impl;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import com.dtc.ioc.annotations.Autowired;
import com.dtc.ioc.core.BeanDefinition;
import com.dtc.ioc.core.DependencyInjector;
import com.dtc.ioc.core.NetworkApplicationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * 默认依赖注入器实现
 * 借鉴Guice的高性能依赖注入机制
 * 
 * @author Network Service Template
 */
public class DefaultDependencyInjector implements DependencyInjector {
    
    private static final Logger log = LoggerFactory.getLogger(DefaultDependencyInjector.class);
    
    private final NetworkApplicationContext container;
    
    public DefaultDependencyInjector(NetworkApplicationContext container) {
        this.container = container;
    }
    
    @Override
    public void injectDependencies(Object bean, BeanDefinition definition) {
        try {
            log.debug("🔧 Injecting dependencies for bean: {}", definition.getBeanName());
            
            // 注入字段依赖
            injectFieldDependencies(bean, definition.getBeanClass());
            
            // 注入方法依赖
            injectMethodDependencies(bean, definition.getBeanClass());
            
            log.debug("✅ Dependencies injected successfully for bean: {}", definition.getBeanName());
            
        } catch (Exception e) {
            log.error("❌ Error injecting dependencies for bean: {}", definition.getBeanName(), e);
            throw new RuntimeException("Failed to inject dependencies", e);
        }
    }
    
    @Override
    public void injectFieldDependencies(Object bean, Class<?> beanClass) {
        Field[] fields = beanClass.getDeclaredFields();
        
        for (Field field : fields) {
            if (isInjectableField(field)) {
                try {
                    Object dependency = resolveDependency(field.getType());
                    if (dependency != null) {
                        field.setAccessible(true);
                        field.set(bean, dependency);
                        log.debug("🔧 Injected field dependency: {} -> {}", field.getName(), dependency.getClass().getSimpleName());
                    }
                } catch (Exception e) {
                    log.error("❌ Error injecting field dependency: {}", field.getName(), e);
                }
            }
        }
    }
    
    @Override
    @Nullable
    public Object createBeanWithConstructor(Constructor<?> constructor, Object[] args) {
        try {
            return constructor.newInstance(args);
        } catch (Exception e) {
            log.error("❌ Error creating bean with constructor", e);
            return null;
        }
    }
    
    @Override
    public void injectMethodDependencies(Object bean, Class<?> beanClass) {
        Method[] methods = beanClass.getDeclaredMethods();
        
        for (Method method : methods) {
            if (isInjectableMethod(method)) {
                try {
                    Object[] args = resolveMethodParameters(method);
                    method.setAccessible(true);
                    method.invoke(bean, args);
                    log.debug("🔧 Injected method dependency: {}", method.getName());
                } catch (Exception e) {
                    log.error("❌ Error injecting method dependency: {}", method.getName(), e);
                }
            }
        }
    }
    
    /**
     * 检查字段是否可注入
     */
    private boolean isInjectableField(Field field) {
        // 检查是否有@Autowired注解
        return field.isAnnotationPresent(Autowired.class);
    }
    
    /**
     * 检查方法是否可注入
     */
    private boolean isInjectableMethod(Method method) {
        // 检查是否有@Autowired注解
        return method.isAnnotationPresent(Autowired.class);
    }
    
    /**
     * 解析依赖
     */
    @Nullable
    private Object resolveDependency(Class<?> dependencyType) {
        try {
            return container.getBean(dependencyType);
        } catch (Exception e) {
            log.debug("Dependency not found: {}", dependencyType.getName());
            return null;
        }
    }
    
    /**
     * 解析方法参数
     */
    @NotNull
    private Object[] resolveMethodParameters(Method method) {
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];
        
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            args[i] = resolveDependency(parameter.getType());
        }
        
        return args;
    }
}
