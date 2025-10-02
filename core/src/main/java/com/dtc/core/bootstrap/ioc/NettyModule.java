package com.dtc.core.bootstrap.ioc;

import com.google.inject.AbstractModule;
import com.dtc.core.netty.NettyBootstrap;
import com.dtc.core.netty.NettyServer;

/**
 * Netty网络模块
 * 绑定Netty相关的服务
 * 
 * @author Network Service Template
 */
public class NettyModule extends AbstractModule {

    @Override
    protected void configure() {
        // 绑定Netty服务器
        bind(NettyServer.class).asEagerSingleton();

        // 绑定Netty启动器
        bind(NettyBootstrap.class).asEagerSingleton();
    }
}
