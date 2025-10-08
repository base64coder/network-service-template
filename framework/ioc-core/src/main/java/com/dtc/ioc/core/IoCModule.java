package com.dtc.ioc.core;

import com.dtc.api.annotations.NotNull;

/**
 * IoC模块接口
 * 提供模块化配置功能，类似Guice的Module
 * 
 * @author Network Service Template
 */
public interface IoCModule {
    
    /**
     * 配置模块
     * 在此方法中注册Bean定义和配置
     * 
     * @param context 应用上下文
     */
    void configure(@NotNull NetworkApplicationContext context);
    
    /**
     * 获取模块名称
     * 
     * @return 模块名称
     */
    @NotNull
    String getModuleName();
    
    /**
     * 获取模块版本
     * 
     * @return 模块版本
     */
    @NotNull
    String getModuleVersion();
    
    /**
     * 获取模块描述
     * 
     * @return 模块描述
     */
    @NotNull
    String getModuleDescription();
    
    /**
     * 获取依赖的模块
     * 
     * @return 依赖模块名称列表
     */
    @NotNull
    String[] getDependencies();
}
