package com.dtc.framework.beans.impl;

import com.dtc.core.network.http.HttpRequestHandler;
import com.dtc.core.network.http.HttpResponseHandler;
import com.dtc.core.network.http.HttpServer;
import com.dtc.core.statistics.StatisticsCollector;
import com.dtc.core.messaging.NetworkMessageQueue;
import com.google.inject.AbstractModule;

/**
 * 网络服务核心模块（自定义IOC实现版本 - 暂时不使用）
 * 配置网络服务的核心组件
 * 
 * 注意：此模块已改为使用 Google Guice 的依赖注入方式。
 * 当前框架暂时使用 Google Guice，此模块作为参考实现。
 * 
 * 当自定义IOC容器完全实现后，可以基于此模块创建自定义IOC版本。
 * 
 * @author Network Service Template
 */
public class NetworkServiceModule extends AbstractModule {
    
    @Override
    protected void configure() {
        // 绑定核心服务组件（使用 Guice 方式）
        bind(HttpRequestHandler.class).asEagerSingleton();
        bind(HttpResponseHandler.class).asEagerSingleton();
        bind(HttpServer.class).asEagerSingleton();
        bind(StatisticsCollector.class).asEagerSingleton();
        bind(NetworkMessageQueue.class).asEagerSingleton();
    }
    
}
