package com.dtc.core.bootstrap.ioc;

import com.dtc.core.bootstrap.NetworkServiceLauncher;
import com.dtc.core.bootstrap.StartupBanner;
import com.dtc.core.extensions.ExtensionBootstrap;
import com.dtc.core.extensions.ExtensionManager;
import com.google.inject.AbstractModule;

/**
 * 网络服务主模块 绑定网络服务核心组件
 * 
 * @author Network Service Template
 */
public class NetworkServiceMainModule extends AbstractModule {

    @Override
    protected void configure() {
        // 绑定启动横幅显示器
        bind(StartupBanner.class).asEagerSingleton();

        // 绑定网络服务启动器
        bind(NetworkServiceLauncher.class).asEagerSingleton();

        // 绑定扩展启动器
        bind(ExtensionBootstrap.class).asEagerSingleton();

        // 绑定扩展管理器
        bind(ExtensionManager.class).asEagerSingleton();
    }
}
