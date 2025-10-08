package com.dtc.ioc.core.impl;

import com.dtc.api.annotations.NotNull;
import com.dtc.ioc.core.IoCModule;
import com.dtc.ioc.core.NetworkApplicationContext;

/**
 * IoC模块抽象基类
 * 提供模块化配置的基础实现
 * 
 * @author Network Service Template
 */
public abstract class AbstractIoCModule implements IoCModule {
    
    @Override
    @NotNull
    public String getModuleName() {
        return this.getClass().getSimpleName();
    }
    
    @Override
    @NotNull
    public String getModuleVersion() {
        return "1.0.0";
    }
    
    @Override
    @NotNull
    public String getModuleDescription() {
        return "IoC Module: " + getModuleName();
    }
    
    @Override
    @NotNull
    public String[] getDependencies() {
        return new String[0];
    }
    
    /**
     * 绑定接口到实现类
     * 
     * @param context 应用上下文
     * @param interfaceClass 接口类
     * @param implementationClass 实现类
     */
    protected <T> void bind(NetworkApplicationContext context, 
                           Class<T> interfaceClass, 
                           Class<? extends T> implementationClass) {
        context.registerBean(interfaceClass.getSimpleName(), implementationClass);
    }
    
    /**
     * 绑定接口到实现类（指定名称）
     * 
     * @param context 应用上下文
     * @param name Bean名称
     * @param implementationClass 实现类
     */
    protected void bind(NetworkApplicationContext context, 
                       String name, 
                       Class<?> implementationClass) {
        context.registerBean(name, implementationClass);
    }
    
    /**
     * 绑定单例实例
     * 
     * @param context 应用上下文
     * @param name Bean名称
     * @param instance 实例
     */
    protected void bindInstance(NetworkApplicationContext context, 
                               String name, 
                               Object instance) {
        context.registerBean(name, instance);
    }
    
    /**
     * 绑定接口到单例实例
     * 
     * @param context 应用上下文
     * @param interfaceClass 接口类
     * @param instance 实例
     */
    protected <T> void bindInstance(NetworkApplicationContext context, 
                                   Class<T> interfaceClass, 
                                   T instance) {
        context.registerBean(interfaceClass.getSimpleName(), instance);
    }
}
