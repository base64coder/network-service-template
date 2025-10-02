package com.dtc.core.bootstrap.ioc;

import com.google.inject.AbstractModule;
import com.dtc.core.metrics.MetricsCollector;
import com.dtc.core.metrics.MetricsRegistry;

/**
 * 指标监控模块
 * 绑定指标监控相关的服务
 * 
 * @author Network Service Template
 */
public class MetricsModule extends AbstractModule {

    @Override
    protected void configure() {
        // 绑定指标注册表
        bind(MetricsRegistry.class).asEagerSingleton();

        // 绑定指标收集器
        bind(MetricsCollector.class).asEagerSingleton();
    }
}
