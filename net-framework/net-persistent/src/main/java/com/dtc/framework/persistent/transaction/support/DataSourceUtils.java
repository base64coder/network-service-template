package com.dtc.framework.persistent.transaction.support;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public abstract class DataSourceUtils {
    public static Connection getConnection(DataSource dataSource) throws SQLException {
        ConnectionHolder conHolder = (ConnectionHolder) TransactionSynchronizationManager.getResource(dataSource);
        if (conHolder != null) {
            return conHolder.getConnection();
        }
        return dataSource.getConnection();
    }

    public static void releaseConnection(Connection con, DataSource dataSource) {
        ConnectionHolder conHolder = (ConnectionHolder) TransactionSynchronizationManager.getResource(dataSource);
        if (conHolder != null && conHolder.getConnection() == con) {
            // It's transactional, don't close
            return;
        }
        try {
            con.close();
        } catch (SQLException ex) {
            // ignore
        }
    }
}

