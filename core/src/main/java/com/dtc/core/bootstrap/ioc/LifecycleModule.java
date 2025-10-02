package com.dtc.core.bootstrap.ioc;

import com.dtc.core.lifecycle.LifecycleManager;
import com.google.inject.AbstractModule;

/**
 * 生命周期模块
 * 提供生命周期管理功能
 * 
 * @author Network Service Template
 */
public class LifecycleModule extends AbstractModule {

    @Override
    protected void configure() {
        // 绑定生命周期管理器
        bind(LifecycleManager.class).asEagerSingleton();
    }
}
