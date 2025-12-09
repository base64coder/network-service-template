package com.dtc.ioc.core.impl;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import com.dtc.ioc.core.BeanDefinition;
import com.dtc.ioc.core.BeanScope;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;

/**
 * 默认 Bean 定义实现
 * 借鉴 Spring BeanDefinition 的设计
 * 
 * @author Network Service Template
 */
public class DefaultBeanDefinition implements BeanDefinition {
    
    private String beanName;
    private Class<?> beanClass;
    private BeanScope scope = BeanScope.SINGLETON;
    private boolean lazyInit = false;
    private List<String> dependsOn = new ArrayList<>();
    private String initMethodName;
    private String destroyMethodName;
    private Constructor<?> constructor;
    private Method factoryMethod;
    private Map<String, Object> propertyValues = new HashMap<>();
    private Map<String, Object> annotationMetadata = new HashMap<>();
    
    public DefaultBeanDefinition() {
    }
    
    public DefaultBeanDefinition(@NotNull String beanName, @NotNull Class<?> beanClass) {
        this.beanName = beanName;
        this.beanClass = beanClass;
    }
    
    @Override
    @NotNull
    public String getBeanName() {
        return beanName;
    }
    
    public void setBeanName(@NotNull String beanName) {
        this.beanName = beanName;
    }
    
    @Override
    @NotNull
    public Class<?> getBeanClass() {
        return beanClass;
    }
    
    public void setBeanClass(@NotNull Class<?> beanClass) {
        this.beanClass = beanClass;
    }
    
    @Override
    @NotNull
    public BeanScope getScope() {
        return scope;
    }
    
    public void setScope(@NotNull BeanScope scope) {
        this.scope = scope;
    }
    
    @Override
    public boolean isSingleton() {
        return scope == BeanScope.SINGLETON;
    }
    
    @Override
    public boolean isPrototype() {
        return scope == BeanScope.PROTOTYPE;
    }
    
    @Override
    public boolean isLazyInit() {
        return lazyInit;
    }
    
    public void setLazyInit(boolean lazyInit) {
        this.lazyInit = lazyInit;
    }
    
    @Override
    @NotNull
    public List<String> getDependsOn() {
        return new ArrayList<>(dependsOn);
    }
    
    public void setDependsOn(@NotNull List<String> dependsOn) {
        this.dependsOn = dependsOn != null ? new ArrayList<>(dependsOn) : new ArrayList<>();
    }
    
    @Override
    @Nullable
    public String getInitMethodName() {
        return initMethodName;
    }
    
    public void setInitMethodName(@Nullable String initMethodName) {
        this.initMethodName = initMethodName;
    }
    
    @Override
    @Nullable
    public String getDestroyMethodName() {
        return destroyMethodName;
    }
    
    public void setDestroyMethodName(@Nullable String destroyMethodName) {
        this.destroyMethodName = destroyMethodName;
    }
    
    @Override
    @Nullable
    public Constructor<?> getConstructor() {
        return constructor;
    }
    
    public void setConstructor(@Nullable Constructor<?> constructor) {
        this.constructor = constructor;
    }
    
    @Override
    @Nullable
    public Method getFactoryMethod() {
        return factoryMethod;
    }
    
    public void setFactoryMethod(@Nullable Method factoryMethod) {
        this.factoryMethod = factoryMethod;
    }
    
    @Override
    @NotNull
    public Map<String, Object> getPropertyValues() {
        return new HashMap<>(propertyValues);
    }
    
    public void setPropertyValue(@NotNull String name, @NotNull Object value) {
        this.propertyValues.put(name, value);
    }
    
    public void setPropertyValues(@NotNull Map<String, Object> propertyValues) {
        this.propertyValues = new HashMap<>(propertyValues);
    }
    
    @Override
    @NotNull
    public Map<String, Object> getAnnotationMetadata() {
        return new HashMap<>(annotationMetadata);
    }
    
    public void setAnnotationMetadata(@NotNull Map<String, Object> annotationMetadata) {
        this.annotationMetadata = new HashMap<>(annotationMetadata);
    }
    
    @Override
    public String toString() {
        return String.format("DefaultBeanDefinition{beanName='%s', beanClass=%s, scope=%s, lazyInit=%s}",
                beanName, beanClass != null ? beanClass.getName() : "null", scope, lazyInit);
    }
}
