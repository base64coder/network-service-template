package com.dtc.core.bootstrap.ioc;

import com.dtc.core.extensions.ExtensionBootstrap;
import com.dtc.core.extensions.ExtensionLifecycleHandler;
import com.dtc.core.extensions.ExtensionLoader;
import com.dtc.core.extensions.ExtensionManager;
import com.google.inject.AbstractModule;

/**
 * 扩展系统模块
 * 绑定扩展系统相关的服务
 * 
 * @author Network Service Template
 */
public class ExtensionModule extends AbstractModule {

    @Override
    protected void configure() {
        // 绑定扩展启动器
        bind(ExtensionBootstrap.class).asEagerSingleton();

        // 绑定扩展加载器
        bind(ExtensionLoader.class).asEagerSingleton();

        // 绑定扩展生命周期处理器
        bind(ExtensionLifecycleHandler.class).asEagerSingleton();

        // 绑定扩展管理器
        bind(ExtensionManager.class).asEagerSingleton();
    }
}
