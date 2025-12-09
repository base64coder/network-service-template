package com.dtc.core.persistence.impl;

import com.dtc.api.annotations.NotNull;
import com.dtc.core.persistence.DataSourceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 数据源健康检查器
 * 用于验证数据源是否正常工作
 * 
 * @author Network Service Template
 */
@Singleton
public class DataSourceHealthChecker {
    
    private static final Logger log = LoggerFactory.getLogger(DataSourceHealthChecker.class);
    
    private final DataSourceProvider dataSourceProvider;
    
    @Inject
    public DataSourceHealthChecker(@NotNull DataSourceProvider dataSourceProvider) {
        this.dataSourceProvider = dataSourceProvider;
    }
    
    /**
     * 检查数据源健康状态
     * 
     * @return 是否健康
     */
    public boolean isHealthy() {
        try (Connection connection = dataSourceProvider.getConnection()) {
            // 尝试执行一个简单的查询
            boolean valid = connection.isValid(5); // 5秒超时
            if (valid) {
                log.debug("DataSource health check passed");
            } else {
                log.warn("DataSource health check failed: connection is not valid");
            }
            return valid;
        } catch (SQLException e) {
            log.error("DataSource health check failed", e);
            return false;
        }
    }
    
    /**
     * 获取数据源信息
     * 
     * @return 数据源信息字符串
     */
    @NotNull
    public String getDataSourceInfo() {
        try {
            javax.sql.DataSource dataSource = dataSourceProvider.getDataSource();
            if (dataSource instanceof com.zaxxer.hikari.HikariDataSource) {
                com.zaxxer.hikari.HikariDataSource hikariDataSource = 
                    (com.zaxxer.hikari.HikariDataSource) dataSource;
                return String.format(
                    "HikariCP DataSource - URL: %s, Active: %d, Idle: %d, Total: %d, Waiting: %d",
                    hikariDataSource.getJdbcUrl(),
                    hikariDataSource.getHikariPoolMXBean().getActiveConnections(),
                    hikariDataSource.getHikariPoolMXBean().getIdleConnections(),
                    hikariDataSource.getHikariPoolMXBean().getTotalConnections(),
                    hikariDataSource.getHikariPoolMXBean().getThreadsAwaitingConnection()
                );
            } else {
                return "DataSource: " + dataSource.getClass().getSimpleName();
            }
        } catch (Exception e) {
            log.error("Failed to get DataSource info", e);
            return "DataSource info unavailable";
        }
    }
}

