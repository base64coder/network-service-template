package com.dtc.core.persistence.transaction;

import java.sql.Connection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.sql.DataSource;
import com.dtc.api.annotations.Nullable;

/**
 * Connection Holder
 * Holds JDBC connection for current thread
 */
public class ConnectionHolder {
    private static final ThreadLocal<Map<DataSource, Connection>> resources = ThreadLocal.withInitial(ConcurrentHashMap::new);

    @Nullable
    public static Connection getConnection(DataSource dataSource) {
        return resources.get().get(dataSource);
    }

    public static void setConnection(DataSource dataSource, Connection connection) {
        resources.get().put(dataSource, connection);
    }

    public static void removeConnection(DataSource dataSource) {
        resources.get().remove(dataSource);
    }
    
    public static void clear() {
        resources.remove();
    }
}

