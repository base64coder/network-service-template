package com.dtc.framework.persistent.dialect.impl;

import com.dtc.api.annotations.NotNull;
import com.dtc.framework.persistent.dialect.Dialect;

/**
 * H2 数据库方言实现
 */
public class H2Dialect implements Dialect {

    @Override
    public @NotNull String getName() {
        return "H2";
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
        return "CALL IDENTITY()";
    }
}

