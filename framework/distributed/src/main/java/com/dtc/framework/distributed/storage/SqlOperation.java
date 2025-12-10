package com.dtc.framework.distributed.storage;

import java.io.Serializable;
import java.util.Arrays;

/**
 * SQL 操作命令，用于在 Raft 集群中复制
 */
public class SqlOperation implements Serializable {
    private static final long serialVersionUID = 1L;

    private String sql;
    private Object[] params;
    private long timestamp;

    public SqlOperation() {
    }

    public SqlOperation(String sql, Object[] params) {
        this.sql = sql;
        this.params = params;
        this.timestamp = System.currentTimeMillis();
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public Object[] getParams() {
        return params;
    }

    public void setParams(Object[] params) {
        this.params = params;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "SqlOperation{" +
                "sql='" + sql + '\'' +
                ", params=" + Arrays.toString(params) +
                ", timestamp=" + timestamp +
                '}';
    }
}

