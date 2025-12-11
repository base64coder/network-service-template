package com.dtc.core.persistence.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dtc.api.annotations.NotNull;
import com.dtc.core.persistence.DataSourceConfig;
import com.dtc.core.persistence.DataSourceProvider;
import com.dtc.core.persistence.datasource.RoutingDataSource;
import com.dtc.core.persistence.transaction.ConnectionHolder;
import com.dtc.core.persistence.transaction.ConnectionProxy;

/**
 * 动态数据源提供者
 * 支持运行时切换数据源
 * 
 * @author Network Service Template
 */
@Singleton
public class DynamicDataSourceProvider implements DataSourceProvider, AutoCloseable {
    
    private static final Logger log = LoggerFactory.getLogger(DynamicDataSourceProvider.class);
    
    private final AtomicReference<DataSourceProvider> currentProvider = new AtomicReference<>();
    
    @Inject
    public DynamicDataSourceProvider() {
        log.info("DynamicDataSourceProvider created, waiting for initialization...");
    }
    
    /**
     * 初始化数据源（由 DataSourceInitializer 调用）
     */
    public void initialize(@NotNull DataSourceConfig config) {
        log.info("Initializing DynamicDataSourceProvider with config: {}", config.getUrl());
        // 创建主数据源
        HikariDataSourceProvider masterProvider = new HikariDataSourceProvider(config);
        DataSource masterDS = masterProvider.getDataSource();
        
        // 创建路由数据源
        RoutingDataSource routingDataSource = new RoutingDataSource();
        routingDataSource.setMasterDataSource(masterDS);
        // TODO: 从配置中加载从数据源并添加到 routingDataSource
        
        // 创建包装了 RoutingDataSource 的 Provider
        DataSourceProvider provider = new RoutingDataSourceProvider(routingDataSource, masterProvider);
        
        this.currentProvider.set(provider);
        log.info("DynamicDataSourceProvider initialized successfully with RoutingDataSource");
    }
    
    /**
     * 切换数据源
     */
    public synchronized void switchDataSource(@NotNull DataSourceConfig newConfig) {
        log.info("Switching DataSource to: {}", newConfig.getUrl());
        
        DataSourceProvider oldProvider = currentProvider.get();
        // 重新初始化（简化版，实际可能需要平滑切换）
        initialize(newConfig);
        
        // 关闭旧数据源
        if (oldProvider instanceof AutoCloseable closeable) {
            try {
                closeable.close();
            } catch (Exception e) {
                log.error("Failed to close old DataSourceProvider", e);
            }
        }
        
        log.info("DataSource switched successfully");
    }
    
    @Override
    @NotNull
    public DataSource getDataSource() {
        DataSourceProvider provider = currentProvider.get();
        if (provider == null) {
            throw new IllegalStateException("DataSourceProvider has not been initialized yet.");
        }
        return provider.getDataSource();
    }
    
    @Override
    @NotNull
    public Connection getConnection() throws SQLException {
        DataSource ds = getDataSource();
        // 检查当前线程是否有事务连接
        Connection txnConn = ConnectionHolder.getConnection(ds);
        if (txnConn != null) {
            return ConnectionProxy.wrap(txnConn);
        }
        
        // 否则返回新连接
        return ds.getConnection();
    }
    
    @Override
    public void releaseConnection(Connection connection) {
        // 对于通过 getConnection() 获取的连接，如果是 Proxy，close() 会被忽略
        // 如果是原生连接，需要关闭
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                log.error("Failed to close connection", e);
            }
        }
    }
    
    @Override
    public void close() {
        DataSourceProvider provider = currentProvider.get();
        if (provider instanceof AutoCloseable closeable) {
            try {
                closeable.close();
            } catch (Exception e) {
                log.error("Failed to close DataSourceProvider", e);
            }
        }
    }
    
    // 内部类：RoutingDataSourceProvider
    private static class RoutingDataSourceProvider implements DataSourceProvider, AutoCloseable {
        private final RoutingDataSource dataSource;
        private final AutoCloseable underlyingCloseable;
        
        public RoutingDataSourceProvider(RoutingDataSource dataSource, AutoCloseable underlyingCloseable) {
            this.dataSource = dataSource;
            this.underlyingCloseable = underlyingCloseable;
        }
        
        @Override
        public DataSource getDataSource() {
            return dataSource;
        }
        
        @Override
        public Connection getConnection() throws SQLException {
            return dataSource.getConnection();
        }
        
        @Override
        public void releaseConnection(Connection connection) {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    // log
                }
            }
        }
        
        @Override
        public void close() throws Exception {
            underlyingCloseable.close();
        }
    }
}
