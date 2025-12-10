package com.dtc.core.persistence.impl;

import com.dtc.api.annotations.NotNull;
import com.dtc.core.persistence.DataSourceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Singleton;
import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;

/**
 * 简单数据源提供者实现
 * 基于DriverManager的简单实现
 * 
 * @author Network Service Template
 */
@Singleton
public class SimpleDataSourceProvider implements DataSourceProvider {
    
    private static final Logger log = LoggerFactory.getLogger(SimpleDataSourceProvider.class);
    
    private final DataSource dataSource;
    private final String jdbcUrl;
    private final String username;
    private final String password;
    
    public SimpleDataSourceProvider() {
        // 默认配置，可以通过系统属性覆盖
        this.jdbcUrl = System.getProperty("jdbc.url", "jdbc:h2:mem:testdb");
        this.username = System.getProperty("jdbc.username", "sa");
        this.password = System.getProperty("jdbc.password", "");
        
        this.dataSource = new SimpleDataSource(jdbcUrl, username, password);
        log.info("Initialized SimpleDataSourceProvider with URL: {}", jdbcUrl);
    }
    
    public SimpleDataSourceProvider(String jdbcUrl, String username, String password) {
        this.jdbcUrl = jdbcUrl;
        this.username = username;
        this.password = password;
        this.dataSource = new SimpleDataSource(jdbcUrl, username, password);
        log.info("Initialized SimpleDataSourceProvider with URL: {}", jdbcUrl);
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
                log.error("Failed to release connection", e);
            }
        }
    }
    
    /**
     * 简单的DataSource实现
     */
    private static class SimpleDataSource implements DataSource {
        private final String url;
        private final String username;
        private final String password;
        
        public SimpleDataSource(String url, String username, String password) {
            this.url = url;
            this.username = username;
            this.password = password;
        }
        
        @Override
        public Connection getConnection() throws SQLException {
            return DriverManager.getConnection(url, username, password);
        }
        
        @Override
        public Connection getConnection(String username, String password) throws SQLException {
            return DriverManager.getConnection(url, username, password);
        }
        
        @Override
        public PrintWriter getLogWriter() throws SQLException {
            return null;
        }
        
        @Override
        public void setLogWriter(PrintWriter out) throws SQLException {
        }
        
        @Override
        public void setLoginTimeout(int seconds) throws SQLException {
        }
        
        @Override
        public int getLoginTimeout() throws SQLException {
            return 0;
        }
        
        @Override
        public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
            return null;
        }
        
        @Override
        public <T> T unwrap(Class<T> iface) throws SQLException {
            return null;
        }
        
        @Override
        public boolean isWrapperFor(Class<?> iface) throws SQLException {
            return false;
        }
    }
}
