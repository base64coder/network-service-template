package com.dtc.core.bootstrap.ioc;

import com.dtc.core.persistence.DataSourceProvider;
import com.dtc.core.persistence.DataStore;
import com.dtc.core.persistence.PersistenceManager;
import com.dtc.core.persistence.impl.DataSourceInitializer;
import com.dtc.core.persistence.impl.DynamicDataSourceProvider;
import com.google.inject.AbstractModule;

/**
 * 持久化模块
 * 配置持久化相关的依赖注入
 * 
 * @author Network Service Template
 */
public class PersistenceModule extends AbstractModule {

    @Override
    protected void configure() {
        // 绑定数据源提供者
        bind(DataSourceProvider.class).to(DynamicDataSourceProvider.class).asEagerSingleton();
        bind(DynamicDataSourceProvider.class).asEagerSingleton();

        // 绑定数据源初始化器（必须在 DynamicDataSourceProvider 之后绑定）
        bind(DataSourceInitializer.class).asEagerSingleton();

        // 绑定持久化管理器
        bind(PersistenceManager.class).asEagerSingleton();

        // 绑定数据存储
        bind(DataStore.class).asEagerSingleton();
    }
}
