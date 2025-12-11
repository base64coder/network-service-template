package com.dtc.framework.persistent.dialect.impl;

import com.dtc.api.annotations.NotNull;
import com.dtc.framework.persistent.dialect.Dialect;

/**
 * PostgreSQL 数据库方言实现
 */
public class PostgreSQLDialect implements Dialect {

    @Override
    public @NotNull String getName() {
        return "PostgreSQL";
    }

    @Override
    public @NotNull String quote(@NotNull String identifier) {
        return "\"" + identifier + "\"";
    }

    @Override
    public @NotNull String forPage(@NotNull String sql, long offset, long limit) {
        return sql + " LIMIT " + limit + " OFFSET " + offset;
    }

    @Override
    public String getIdentitySelectString(String tableName) {
        // PG通常使用 RETURNING id，这里简化处理，实际可能需要配合 SQL 构建器
        return null; 
    }
}

