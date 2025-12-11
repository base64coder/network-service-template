package com.dtc.core.persistence.dialect.impl;

import com.dtc.api.annotations.NotNull;
import com.dtc.core.persistence.dialect.Dialect;

/**
 * MySQL 数据库方言实现
 */
public class MySQLDialect implements Dialect {

    @Override
    public @NotNull String getName() {
        return "MySQL";
    }

    @Override
    public @NotNull String quote(@NotNull String identifier) {
        return "`" + identifier + "`";
    }

    @Override
    public @NotNull String forPage(@NotNull String sql, long offset, long limit) {
        return sql + " LIMIT " + offset + ", " + limit;
    }

    @Override
    public String getIdentitySelectString(String tableName) {
        return "SELECT LAST_INSERT_ID()";
    }
}

