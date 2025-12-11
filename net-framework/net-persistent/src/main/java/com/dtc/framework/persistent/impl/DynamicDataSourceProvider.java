package com.dtc.framework.persistent.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dtc.api.annotations.NotNull;
import com.dtc.framework.persistent.DataSourceConfig;
import com.dtc.framework.persistent.DataSourceProvider;

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
        // 延迟初始化：先创建一个占位符，等待 DataSourceInitializer 初始化
        // 这样可以避免循环依赖，因为 DataSourceInitializer 需要 ServerConfiguration
        // 而 ServerConfiguration 可能也需要 DataSourceProvider
        log.info("DynamicDataSourceProvider created, waiting for initialization...");
    }
    
    /**
     * 初始化数据源（由 DataSourceInitializer 调用）
     * 
     * @param config 数据源配置
     */
    public void initialize(@NotNull DataSourceConfig config) {
        log.info("Initializing DynamicDataSourceProvider with config: {}", config.getUrl());
        DataSourceProvider provider = new HikariDataSourceProvider(config);
        this.currentProvider.set(provider);
        log.info("DynamicDataSourceProvider initialized successfully");
    }
    
    /**
     * 切换数据源
     * 
     * @param newConfig 新的数据源配置
     */
    public synchronized void switchDataSource(@NotNull DataSourceConfig newConfig) {
        log.info("Switching DataSource to: {}", newConfig.getUrl());
        
        DataSourceProvider oldProvider = currentProvider.get();
        DataSourceProvider newProvider = new HikariDataSourceProvider(newConfig);
        
        currentProvider.set(newProvider);
        
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
            throw new IllegalStateException("DataSourceProvider has not been initialized yet. " +
                    "Please ensure DataSourceInitializer is properly configured.");
        }
        return provider.getDataSource();
    }
    
    @Override
    @NotNull
    public Connection getConnection() throws SQLException {
        DataSourceProvider provider = currentProvider.get();
        if (provider == null) {
            throw new IllegalStateException("DataSourceProvider has not been initialized yet. " +
                    "Please ensure DataSourceInitializer is properly configured.");
        }
        return provider.getConnection();
    }
    
    @Override
    public void releaseConnection(Connection connection) {
        DataSourceProvider provider = currentProvider.get();
        if (provider != null) {
            provider.releaseConnection(connection);
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
}

