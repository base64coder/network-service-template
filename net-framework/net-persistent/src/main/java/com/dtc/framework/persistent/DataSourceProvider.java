package com.dtc.framework.persistent;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 数据源提供者接口
 * 提供数据源获取
 * 
 * @author Network Service Template
 */
public interface DataSourceProvider {
    
    /**
     * 获取数据源
     * 
     * @return 数据源
     */
    @NotNull
    DataSource getDataSource();
    
    /**
     * 获取数据库连接
     * 
     * @return 数据库连接
     * @throws SQLException SQL异常
     */
    @NotNull
    Connection getConnection() throws SQLException;
    
    /**
     * 释放连接
     * 
     * @param connection 数据库连接
     */
    void releaseConnection(@Nullable Connection connection);
}
