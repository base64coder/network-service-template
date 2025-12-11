package com.dtc.core.persistence.dialect;

import com.dtc.core.persistence.dialect.impl.H2Dialect;
import com.dtc.core.persistence.dialect.impl.MySQLDialect;
import com.dtc.core.persistence.dialect.impl.PostgreSQLDialect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 方言工厂
 * 根据数据库连接信息自动探测并创建对应的 Dialect
 */
public class DialectFactory {

    private static final Logger log = LoggerFactory.getLogger(DialectFactory.class);
    private static final Map<String, Dialect> dialectCache = new ConcurrentHashMap<>();

    /**
     * 根据 JDBC 连接获取方言
     */
    public static Dialect getDialect(Connection connection) {
        try {
            String url = connection.getMetaData().getURL();
            return getDialect(url);
        } catch (SQLException e) {
            log.error("Failed to get database metadata", e);
            return new MySQLDialect(); // 默认回退到 MySQL
        }
    }

    /**
     * 根据 JDBC URL 获取方言
     */
    public static Dialect getDialect(String jdbcUrl) {
        if (jdbcUrl == null) {
            return new MySQLDialect();
        }

        if (jdbcUrl.startsWith("jdbc:mysql:") || jdbcUrl.startsWith("jdbc:mariadb:")) {
            return new MySQLDialect();
        } else if (jdbcUrl.startsWith("jdbc:h2:")) {
            return new H2Dialect();
        } else if (jdbcUrl.startsWith("jdbc:postgresql:")) {
            return new PostgreSQLDialect();
        } else if (jdbcUrl.startsWith("jdbc:sqlite:")) {
            // 暂时使用 MySQL 方言作为 SQLite 的近似替代，分页语法类似
            // 后续可添加专门的 SQLiteDialect
            return new MySQLDialect(); 
        }

        log.warn("Unknown database type for URL: {}, using MySQL dialect as default", jdbcUrl);
        return new MySQLDialect();
    }
}

