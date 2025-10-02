package com.dtc.core.bootstrap.ioc;

import com.google.inject.AbstractModule;
import com.dtc.core.bootstrap.ioc.lazysingleton.LazySingletonScope;

/**
 * 懒加载单例模块
 * 提供懒加载单例作用域支持
 * 
 * @author Network Service Template
 */
public class LazySingletonModule extends AbstractModule {

    @Override
    protected void configure() {
        // 安装懒加载单例作用域
        install(new LazySingletonScope());
    }
}
