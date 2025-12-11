package com.dtc.core.persistence.datasource;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 路由数据源
 * 支持读写分离和多从库负载均衡
 */
public class RoutingDataSource implements DataSource {

    private static final Logger log = LoggerFactory.getLogger(RoutingDataSource.class);

    private DataSource masterDataSource;
    private final List<DataSource> slaveDataSources = new CopyOnWriteArrayList<>();
    private final AtomicInteger counter = new AtomicInteger(0);

    /**
     * 设置主数据源
     */
    public void setMasterDataSource(@NotNull DataSource masterDataSource) {
        this.masterDataSource = masterDataSource;
    }

    /**
     * 添加从数据源
     */
    public void addSlaveDataSource(@NotNull DataSource slaveDataSource) {
        this.slaveDataSources.add(slaveDataSource);
    }

    /**
     * 获取当前数据源
     */
    @NotNull
    protected DataSource determineTargetDataSource() {
        if (DataSourceContext.get() == DataSourceContext.Type.MASTER || slaveDataSources.isEmpty()) {
            if (masterDataSource == null) {
                throw new IllegalStateException("Master DataSource not set");
            }
            return masterDataSource;
        }

        // 简单的轮询负载均衡
        int index = Math.abs(counter.getAndIncrement()) % slaveDataSources.size();
        return slaveDataSources.get(index);
    }

    @Override
    public Connection getConnection() throws SQLException {
        return determineTargetDataSource().getConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return determineTargetDataSource().getConnection(username, password);
    }

    // 委托方法实现
    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (iface.isInstance(this)) {
            return iface.cast(this);
        }
        return determineTargetDataSource().unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return iface.isInstance(this) || determineTargetDataSource().isWrapperFor(iface);
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return determineTargetDataSource().getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        determineTargetDataSource().setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        determineTargetDataSource().setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return determineTargetDataSource().getLoginTimeout();
    }

    @Override
    public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return determineTargetDataSource().getParentLogger();
    }
}

