package com.dtc.core.bootstrap.ioc;

import com.dtc.core.bootstrap.launcher.NetworkServiceLauncher;
import com.dtc.core.bootstrap.launcher.StartupBanner;
import com.dtc.core.extensions.ExtensionBootstrap;
import com.dtc.core.extensions.ExtensionManager;
import com.google.inject.AbstractModule;

/**
 * 网络服务主模块
 * 绑定网络服务核心组件
 * 
 * @author Network Service Template
 */
public class NetworkServiceMainModule extends AbstractModule {

    @Override
    protected void configure() {
        // 绑定启动横幅显示
        bind(StartupBanner.class).asEagerSingleton();

        // 绑定网络服务启动器
        bind(NetworkServiceLauncher.class).asEagerSingleton();

        // 绑定扩展启动器
        bind(ExtensionBootstrap.class).asEagerSingleton();

        // 绑定扩展管理器
        bind(ExtensionManager.class).asEagerSingleton();
        
        // 绑定Injector自身（用于在扩展中获取Injector实例）
        // 注意：这需要在创建Injector后通过requestInjection或Provider方式绑定
        // 由于Guice的限制，我们使用Provider模式
        bind(com.google.inject.Injector.class).toProvider(() -> {
            // 这里返回当前Injector，但需要在创建后设置
            // 实际使用时，扩展可以通过其他方式获取Injector
            throw new UnsupportedOperationException("Injector should be obtained from NetworkService");
        });
    }
}
