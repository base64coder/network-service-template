package com.dtc.framework.persistent.dialect;

import com.dtc.api.annotations.NotNull;

/**
 * 数据库方言接口
 * 用于屏蔽不同数据库之间的 SQL 差异
 * 
 * @author Network Service Template
 */
public interface Dialect {

    /**
     * 获取方言名称
     */
    @NotNull
    String getName();

    /**
     * 为标识符添加引用引号
     * 例如 MySQL 使用反引号 `，PostgreSQL 使用双引号 "
     * 
     * @param identifier 表名或列名
     * @return 添加引用后的标识符
     */
    @NotNull
    String quote(@NotNull String identifier);

    /**
     * 生成分页 SQL
     * 
     * @param sql    原始 SQL
     * @param offset 偏移量
     * @param limit  限制条数
     * @return 分页后的 SQL
     */
    @NotNull
    String forPage(@NotNull String sql, long offset, long limit);

    /**
     * 获取获取生成主键的 SQL
     * 
     * @param tableName 表名
     * @return 获取主键的 SQL
     */
    String getIdentitySelectString(String tableName);

    /**
     * 是否支持批量插入
     */
    default boolean supportBatchInsert() {
        return true;
    }
}

