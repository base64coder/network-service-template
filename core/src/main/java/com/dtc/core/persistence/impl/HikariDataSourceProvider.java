package com.dtc.core.persistence.impl;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dtc.api.annotations.NotNull;
import com.dtc.core.persistence.DataSourceConfig;
import com.dtc.core.persistence.DataSourceProvider;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * HikariCP 数据源提供者实现
 * 
 * @author Network Service Template
 */
public class HikariDataSourceProvider implements DataSourceProvider, AutoCloseable {
    
    private static final Logger log = LoggerFactory.getLogger(HikariDataSourceProvider.class);
    
    private final HikariDataSource dataSource;
    
    public HikariDataSourceProvider(@NotNull DataSourceConfig config) {
        log.info("Initializing HikariCP DataSource with URL: {}", config.getUrl());
        
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(config.getUrl());
        hikariConfig.setUsername(config.getUsername());
        hikariConfig.setPassword(config.getPassword());
        
        if (config.getDriverClassName() != null) {
            hikariConfig.setDriverClassName(config.getDriverClassName());
        }
        
        hikariConfig.setMaximumPoolSize(config.getMaximumPoolSize());
        hikariConfig.setMinimumIdle(config.getMinimumIdle());
        hikariConfig.setConnectionTimeout(config.getConnectionTimeout());
        hikariConfig.setIdleTimeout(config.getIdleTimeout());
        hikariConfig.setMaxLifetime(config.getMaxLifetime());
        
        this.dataSource = new HikariDataSource(hikariConfig);
        log.info("HikariCP DataSource initialized successfully");
    }
    
    @Override
    @NotNull
    public DataSource getDataSource() {
        return dataSource;
    }
    
    @Override
    @NotNull
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
    
    @Override
    public void releaseConnection(Connection connection) {
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
        if (dataSource != null && !dataSource.isClosed()) {
            log.info("Closing HikariCP DataSource...");
            dataSource.close();
            log.info("HikariCP DataSource closed");
        }
    }
}

