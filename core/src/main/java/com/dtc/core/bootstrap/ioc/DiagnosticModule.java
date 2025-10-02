package com.dtc.core.bootstrap.ioc;

import com.dtc.core.diagnostic.DiagnosticService;
import com.dtc.core.diagnostic.HealthChecker;
import com.google.inject.AbstractModule;

/**
 * 诊断模块
 * 绑定诊断相关的服务
 * 
 * @author Network Service Template
 */
public class DiagnosticModule extends AbstractModule {

    @Override
    protected void configure() {
        // 绑定诊断服务
        bind(DiagnosticService.class).asEagerSingleton();

        // 绑定健康检查器
        bind(HealthChecker.class).asEagerSingleton();
    }
}
