package com.dtc.framework.persistent.query;

import java.util.List;

import com.dtc.api.annotations.NotNull;

/**
 * 查询包装器
 * 参考 MyBatis-Flex 的 QueryWrapper 设计
 * 提供链式查询构建功能
 * 
 * @param <T> 实体类型
 * 
 * @author Network Service Template
 */
public interface QueryWrapper<T> {
    
    /**
     * 等于条件
     * 
     * @param column 列名
     * @param value 值
     * @return 查询包装器
     */
    @NotNull
    QueryWrapper<T> eq(@NotNull String column, @NotNull Object value);
    
    /**
     * 不等于条件
     * 
     * @param column 列名
     * @param value 值
     * @return 查询包装器
     */
    @NotNull
    QueryWrapper<T> ne(@NotNull String column, @NotNull Object value);
    
    /**
     * 大于条件
     * 
     * @param column 列名
     * @param value 值
     * @return 查询包装器
     */
    @NotNull
    QueryWrapper<T> gt(@NotNull String column, @NotNull Object value);
    
    /**
     * 大于等于条件
     * 
     * @param column 列名
     * @param value 值
     * @return 查询包装器
     */
    @NotNull
    QueryWrapper<T> ge(@NotNull String column, @NotNull Object value);
    
    /**
     * 小于条件
     * 
     * @param column 列名
     * @param value 值
     * @return 查询包装器
     */
    @NotNull
    QueryWrapper<T> lt(@NotNull String column, @NotNull Object value);
    
    /**
     * 小于等于条件
     * 
     * @param column 列名
     * @param value 值
     * @return 查询包装器
     */
    @NotNull
    QueryWrapper<T> le(@NotNull String column, @NotNull Object value);
    
    /**
     * LIKE 条件
     * 
     * @param column 列名
     * @param value 值
     * @return 查询包装器
     */
    @NotNull
    QueryWrapper<T> like(@NotNull String column, @NotNull String value);
    
    /**
     * IN 条件
     * 
     * @param column 列名
     * @param values 值列表
     * @return 查询包装器
     */
    @NotNull
    QueryWrapper<T> in(@NotNull String column, @NotNull List<Object> values);
    
    /**
     * IS NULL 条件
     * 
     * @param column 列名
     * @return 查询包装器
     */
    @NotNull
    QueryWrapper<T> isNull(@NotNull String column);
    
    /**
     * IS NOT NULL 条件
     * 
     * @param column 列名
     * @return 查询包装器
     */
    @NotNull
    QueryWrapper<T> isNotNull(@NotNull String column);
    
    /**
     * AND 条件组合
     * 
     * @param wrapper 查询包装器
     * @return 查询包装器
     */
    @NotNull
    QueryWrapper<T> and(@NotNull QueryWrapper<T> wrapper);
    
    /**
     * OR 条件组合
     * 
     * @param wrapper 查询包装器
     * @return 查询包装器
     */
    @NotNull
    QueryWrapper<T> or(@NotNull QueryWrapper<T> wrapper);
    
    /**
     * 排序（升序）
     * 
     * @param column 列名
     * @return 查询包装器
     */
    @NotNull
    QueryWrapper<T> orderByAsc(@NotNull String column);
    
    /**
     * 排序（降序）
     * 
     * @param column 列名
     * @return 查询包装器
     */
    @NotNull
    QueryWrapper<T> orderByDesc(@NotNull String column);
    
    /**
     * 限制结果数量
     * 
     * @param limit 限制数量
     * @return 查询包装器
     */
    @NotNull
    QueryWrapper<T> limit(int limit);
    
    /**
     * 偏移量
     * 
     * @param offset 偏移量
     * @return 查询包装器
     */
    @NotNull
    QueryWrapper<T> offset(int offset);
}

