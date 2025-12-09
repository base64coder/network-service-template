package com.dtc.core.bootstrap.ioc;

import com.dtc.core.metrics.MetricsCollector;
import com.dtc.core.metrics.MetricsRegistry;
import com.google.inject.AbstractModule;

/**
 * 指标模块
 * 配置指标相关的依赖注入
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
