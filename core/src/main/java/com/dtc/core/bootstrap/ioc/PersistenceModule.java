package com.dtc.core.bootstrap.ioc;

import com.dtc.core.persistence.DataSourceProvider;
import com.dtc.core.persistence.DataStore;
import com.dtc.core.persistence.PersistenceManager;
import com.dtc.core.persistence.impl.DataSourceInitializer;
import com.dtc.core.persistence.impl.DynamicDataSourceProvider;
import com.dtc.annotations.transaction.Transactional;
import com.dtc.core.persistence.transaction.TransactionInterceptor;
import com.dtc.annotations.transaction.DistributedTransactional;
import com.dtc.core.persistence.transaction.distributed.GlobalTransactionManager;
import com.dtc.core.persistence.transaction.distributed.DistributedTransactionInterceptor;
import com.dtc.core.persistence.transaction.distributed.impl.DbBasedGlobalTransactionManager;
import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;

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
        
        // 绑定全局事务管理器
        bind(GlobalTransactionManager.class).to(DbBasedGlobalTransactionManager.class).asEagerSingleton();

        // 配置分布式事务拦截器
        DistributedTransactionInterceptor distributedInterceptor = new DistributedTransactionInterceptor();
        requestInjection(distributedInterceptor);
        
        bindInterceptor(Matchers.annotatedWith(DistributedTransactional.class), Matchers.any(), distributedInterceptor);
        bindInterceptor(Matchers.any(), Matchers.annotatedWith(DistributedTransactional.class), distributedInterceptor);

        // 配置本地事务拦截器
        TransactionInterceptor transactionInterceptor = new TransactionInterceptor();
        requestInjection(transactionInterceptor);
        
        // 类级别 @Transactional
        bindInterceptor(Matchers.annotatedWith(Transactional.class), Matchers.any(), transactionInterceptor);
        // 方法级别 @Transactional
        bindInterceptor(Matchers.any(), Matchers.annotatedWith(Transactional.class), transactionInterceptor);
    }
}
