package com.dtc.framework.persistent.transaction.support;

import java.sql.Connection;

public class ConnectionHolder {
    private final Connection connection;
    
    public ConnectionHolder(Connection connection) {
        this.connection = connection;
    }
    
    public Connection getConnection() {
        return connection;
    }
}

