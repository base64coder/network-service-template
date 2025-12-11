package com.dtc.framework.persistent;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * 实体元数据接口
 * 封装实体的元数据信息，包括表名、字段映射、关联关系等
 * 
 * @author Network Service Template
 */
public interface EntityMetadata {
    
    /**
     * 获取实体类型
     * 
     * @return 实体类型
     */
    @NotNull
    Class<?> getEntityClass();
    
    /**
     * 获取表名
     * 
     * @return 表名
     */
    @NotNull
    String getTableName();
    
    /**
     * 获取Schema
     * 
     * @return Schema名称
     */
    @Nullable
    String getSchema();
    
    /**
     * 获取主键字段
     * 
     * @return 主键字段
     */
    @NotNull
    Field getPrimaryKeyField();
    
    /**
     * 获取所有字段映射，字段名 -> 列名
     * 
     * @return 字段映射
     */
    @NotNull
    Map<String, String> getFieldColumnMapping();
    
    /**
     * 获取所有列名
     * 
     * @return 列名列表
     */
    @NotNull
    List<String> getColumnNames();
    
    /**
     * 获取一对一关联字段
     * 
     * @return 关联字段列表
     */
    @NotNull
    List<Field> getOneToOneFields();
    
    /**
     * 获取一对多关联字段
     * 
     * @return 关联字段列表
     */
    @NotNull
    List<Field> getOneToManyFields();
    
    /**
     * 获取多对一关联字段
     * 
     * @return 关联字段列表
     */
    @NotNull
    List<Field> getManyToOneFields();
    
    /**
     * 获取多对多关联字段
     * 
     * @return 关联字段列表
     */
    @NotNull
    List<Field> getManyToManyFields();
    
    /**
     * 根据字段名获取列名
     * 
     * @param fieldName 字段名
     * @return 列名
     */
    @Nullable
    String getColumnName(@NotNull String fieldName);
    
    /**
     * 根据列名获取字段名
     * 
     * @param columnName 列名
     * @return 字段名
     */
    @Nullable
    String getFieldName(@NotNull String columnName);
}
