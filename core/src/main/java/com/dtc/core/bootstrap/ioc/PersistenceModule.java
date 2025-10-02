package com.dtc.core.bootstrap.ioc;

import com.dtc.core.persistence.DataStore;
import com.dtc.core.persistence.PersistenceManager;
import com.google.inject.AbstractModule;

/**
 * 持久化模块
 * 绑定持久化相关的服务
 * 
 * @author Network Service Template
 */
public class PersistenceModule extends AbstractModule {

    @Override
    protected void configure() {
        // 绑定持久化管理器
        bind(PersistenceManager.class).asEagerSingleton();

        // 绑定数据存储
        bind(DataStore.class).asEagerSingleton();
    }
}
