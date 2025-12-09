package com.dtc.framework.beans;

import com.dtc.api.annotations.NotNull;
import com.dtc.annotations.ioc.Component;
import com.dtc.annotations.ioc.PostConstruct;
import com.dtc.annotations.ioc.PreDestroy;
import com.dtc.annotations.ioc.Service;
import com.dtc.annotations.ioc.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Bean 定义读取器
 * 从类注解中读取 Bean 定义信息
 * 借鉴 Spring BeanDefinitionReader 的设计
 * 
 * @author Network Service Template
 */
public class BeanDefinitionReader {
    
    private static final Logger log = LoggerFactory.getLogger(BeanDefinitionReader.class);
    
    /**
     * 从类中读取 Bean 定义
     * 
     * @param beanClass Bean 类
     * @return Bean 定义，如果不是组件则返回 null
     */
    @NotNull
    public BeanDefinition readBeanDefinition(@NotNull Class<?> beanClass) {
        BeanDefinition definition = new com.dtc.ioc.core.impl.DefaultBeanDefinition();
        
        // 确定 Bean 名称
        String beanName = determineBeanName(beanClass);
        definition.setBeanName(beanName);
        definition.setBeanClass(beanClass);
        
        // 设置作用域（默认为单例）
        definition.setScope(BeanScope.SINGLETON);
        
        // 查找初始化方法
        String initMethodName = findInitMethod(beanClass);
        if (initMethodName != null) {
            definition.setInitMethodName(initMethodName);
        }
        
        // 查找销毁方法
        String destroyMethodName = findDestroyMethod(beanClass);
        if (destroyMethodName != null) {
            definition.setDestroyMethodName(destroyMethodName);
        }
        
        log.debug("Read bean definition: {} -> {}", beanName, beanClass.getName());
        return definition;
    }
    
    /**
     * 确定 Bean 名称
     */
    @NotNull
    private String determineBeanName(@NotNull Class<?> beanClass) {
        // 检查 @Component 注解
        Component component = beanClass.getAnnotation(Component.class);
        if (component != null && !component.value().isEmpty()) {
            return component.value();
        }
        
        // 检查 @Service 注解
        Service service = beanClass.getAnnotation(Service.class);
        if (service != null && !service.value().isEmpty()) {
            return service.value();
        }
        
        // 检查 @Repository 注解
        Repository repository = beanClass.getAnnotation(Repository.class);
        if (repository != null && !repository.value().isEmpty()) {
            return repository.value();
        }
        
        // 默认使用类名（首字母小写）
        String className = beanClass.getSimpleName();
        if (className.isEmpty()) {
            return className;
        }
        return Character.toLowerCase(className.charAt(0)) + className.substring(1);
    }
    
    /**
     * 查找初始化方法（@PostConstruct）
     */
    @NotNull
    private String findInitMethod(@NotNull Class<?> beanClass) {
        for (Method method : beanClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(PostConstruct.class)) {
                return method.getName();
            }
        }
        return null;
    }
    
    /**
     * 查找销毁方法（@PreDestroy）
     */
    @NotNull
    private String findDestroyMethod(@NotNull Class<?> beanClass) {
        for (Method method : beanClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(PreDestroy.class)) {
                return method.getName();
            }
        }
        return null;
    }
    
    /**
     * 检查类是否为组件
     */
    public boolean isComponent(@NotNull Class<?> beanClass) {
        return beanClass.isAnnotationPresent(Component.class) ||
               beanClass.isAnnotationPresent(Service.class) ||
               beanClass.isAnnotationPresent(Repository.class);
    }
}

