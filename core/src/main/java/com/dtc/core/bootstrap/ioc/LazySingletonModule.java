package com.dtc.core.bootstrap.ioc;

import com.dtc.core.bootstrap.ioc.lazysingleton.LazySingletonScope;
import com.google.inject.AbstractModule;

/**
 * 延迟单例模块
 * 注册延迟单例作用域
 * 
 * @author Network Service Template
 */
public class LazySingletonModule extends AbstractModule {

    @Override
    protected void configure() {
        // 安装延迟单例作用域
        install(new LazySingletonScope());
    }
}
