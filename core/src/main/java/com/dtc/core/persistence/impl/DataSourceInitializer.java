package com.dtc.core.persistence.impl;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dtc.api.annotations.NotNull;
import com.dtc.core.bootstrap.config.ServerConfiguration;
import com.dtc.core.persistence.DataSourceConfig;

/**
 * 数据源初始化器
 * 在系统启动时从 ServerConfiguration 初始化数据源
 * 
 * @author Network Service Template
 */
@Singleton
public class DataSourceInitializer {
    
    private static final Logger log = LoggerFactory.getLogger(DataSourceInitializer.class);
    
    private final DynamicDataSourceProvider dataSourceProvider;
    private final ServerConfiguration serverConfiguration;
    
    @Inject
    public DataSourceInitializer(@NotNull DynamicDataSourceProvider dataSourceProvider,
                                 @NotNull ServerConfiguration serverConfiguration) {
        this.dataSourceProvider = dataSourceProvider;
        this.serverConfiguration = serverConfiguration;
        initialize();
    }
    
    /**
     * 初始化数据源
     */
    private void initialize() {
        DataSourceConfig config = serverConfiguration.getDataSourceConfig();
        
        // 如果配置中没有URL，使用默认值或系统属性
        if (config.getUrl() == null || config.getUrl().isEmpty()) {
            log.info("No DataSource URL in ServerConfiguration, using system properties or defaults");
            DataSourceConfig defaultConfig = createDefaultConfig();
            dataSourceProvider.initialize(defaultConfig);
        } else {
            log.info("Initializing DataSource from ServerConfiguration: {}", config.getUrl());
            dataSourceProvider.initialize(config);
        }
    }
    
    /**
     * 创建默认配置
     */
    @NotNull
    private DataSourceConfig createDefaultConfig() {
        DataSourceConfig config = new DataSourceConfig();
        
        // 从系统属性读取，如果没有则使用默认值
        String url = System.getProperty("jdbc.url", "jdbc:h2:mem:testdb");
        String username = System.getProperty("jdbc.username", "sa");
        String password = System.getProperty("jdbc.password", "");
        String driverClassName = System.getProperty("jdbc.driver", "org.h2.Driver");
        
        config.setUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName(driverClassName);
        
        // 从系统属性读取连接池配置
        String maxPoolSize = System.getProperty("jdbc.maxPoolSize", "10");
        String minIdle = System.getProperty("jdbc.minIdle", "2");
        String connectionTimeout = System.getProperty("jdbc.connectionTimeout", "30000");
        String idleTimeout = System.getProperty("jdbc.idleTimeout", "600000");
        String maxLifetime = System.getProperty("jdbc.maxLifetime", "1800000");
        
        try {
            config.setMaximumPoolSize(Integer.parseInt(maxPoolSize));
            config.setMinimumIdle(Integer.parseInt(minIdle));
            config.setConnectionTimeout(Long.parseLong(connectionTimeout));
            config.setIdleTimeout(Long.parseLong(idleTimeout));
            config.setMaxLifetime(Long.parseLong(maxLifetime));
        } catch (NumberFormatException e) {
            log.warn("Invalid number format in system properties, using defaults", e);
        }
        
        return config;
    }
}

