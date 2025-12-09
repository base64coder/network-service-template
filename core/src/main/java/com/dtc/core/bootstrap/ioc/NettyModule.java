package com.dtc.core.bootstrap.ioc;

import com.dtc.core.network.netty.NettyBootstrap;
import com.dtc.core.network.netty.NettyServer;
import com.google.inject.AbstractModule;

/**
 * Netty 服务器模块
 * 配置 Netty 相关的依赖注入
 * 
 * @author Network Service Template
 */
public class NettyModule extends AbstractModule {

    @Override
    protected void configure() {
        // 绑定 Netty 服务器实例
        bind(NettyServer.class).asEagerSingleton();

        // 绑定 Netty 启动器
        bind(NettyBootstrap.class).asEagerSingleton();
    }
}
