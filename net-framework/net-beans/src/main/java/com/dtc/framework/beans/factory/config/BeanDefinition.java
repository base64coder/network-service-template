package com.dtc.framework.beans.factory.config;

import java.util.ArrayList;
import java.util.List;

/**
 * IoC 核心元数据
 * 支撑特性 1, 3, 6, 8, 11
 */
public class BeanDefinition {
    // 基本信息
    private String beanClassName;
    private Class<?> beanClass;
    private String scope = "singleton";
    private boolean lazyInit = false;
    private boolean primary = false;
    private String factoryBeanName; // 工厂Bean名称（用于@Configuration @Bean）
    private String factoryMethodName; // 工厂方法名称

    // 生命周期
    private String initMethodName;
    private String destroyMethodName;

    // 依赖信息
    private final List<String> dependsOn = new ArrayList<>();
    
    // 属性注入元数据 (支持字段注入和Setter注入)
    private final List<PropertyPropertyValue> propertyValues = new ArrayList<>();

    public BeanDefinition() {}

    public void setBeanClass(Class<?> beanClass) {
        this.beanClass = beanClass;
        if (beanClass != null) {
            this.beanClassName = beanClass.getName();
        }
    }

    public Class<?> getBeanClass() { return beanClass; }
    
    public String getBeanClassName() { return beanClassName; }
    public void setBeanClassName(String beanClassName) { this.beanClassName = beanClassName; }

    public String getScope() { return scope; }
    public void setScope(String scope) { this.scope = scope; }
    
    public boolean isSingleton() { return "singleton".equals(scope); }
    public boolean isPrototype() { return "prototype".equals(scope); }
    
    public boolean isLazyInit() { return lazyInit; }
    public void setLazyInit(boolean lazyInit) { this.lazyInit = lazyInit; }

    public boolean isPrimary() { return primary; }
    public void setPrimary(boolean primary) { this.primary = primary; }

    public String getFactoryBeanName() { return factoryBeanName; }
    public void setFactoryBeanName(String factoryBeanName) { this.factoryBeanName = factoryBeanName; }

    public String getFactoryMethodName() { return factoryMethodName; }
    public void setFactoryMethodName(String factoryMethodName) { this.factoryMethodName = factoryMethodName; }

    public String getInitMethodName() { return initMethodName; }
    public void setInitMethodName(String initMethodName) { this.initMethodName = initMethodName; }

    public String getDestroyMethodName() { return destroyMethodName; }
    public void setDestroyMethodName(String destroyMethodName) { this.destroyMethodName = destroyMethodName; }

    public List<String> getDependsOn() { return dependsOn; }
    
    public List<PropertyPropertyValue> getPropertyValues() { return propertyValues; }
}
