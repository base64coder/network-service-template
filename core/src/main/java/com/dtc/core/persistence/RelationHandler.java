package com.dtc.core.persistence;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import com.dtc.annotations.persistence.*;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;

/**
 * 关联关系处理器接口
 * 负责处理实体之间的关联关系，包括OneToOne, OneToMany, ManyToOne, ManyToMany等
 * 
 * @author Network Service Template
 */
public interface RelationHandler {
    
    /**
     * 处理一对一关联
     * 
     * @param entity 源实体
     * @param field 关联字段
     * @param annotation 关联注解
     * @return 关联的实体对象
     */
    @Nullable
    Object handleOneToOne(@NotNull Object entity, @NotNull Field field, @NotNull OneToOne annotation);
    
    /**
     * 处理一对多关联
     * 
     * @param entity 源实体
     * @param field 关联字段
     * @param annotation 关联注解
     * @return 关联的实体集合
     */
    @NotNull
    Collection<?> handleOneToMany(@NotNull Object entity, @NotNull Field field, @NotNull OneToMany annotation);
    
    /**
     * 处理多对一关联
     * 
     * @param entity 源实体
     * @param field 关联字段
     * @param annotation 关联注解
     * @return 关联的实体对象
     */
    @Nullable
    Object handleManyToOne(@NotNull Object entity, @NotNull Field field, @NotNull ManyToOne annotation);
    
    /**
     * 处理多对多关联
     * 
     * @param entity 源实体
     * @param field 关联字段
     * @param annotation 关联注解
     * @return 关联的实体集合
     */
    @NotNull
    Collection<?> handleManyToMany(@NotNull Object entity, @NotNull Field field, @NotNull ManyToMany annotation);
    
    /**
     * 批量加载关联关系
     * 
     * @param entities 实体列表
     * @param relationFields 要加载的关联字段名，如果为空则加载所有关联
     */
    void loadRelations(@NotNull List<?> entities, @Nullable String... relationFields);
    
    /**
     * 为单个实体加载关联关系
     * 
     * @param entity 实体对象
     * @param relationFields 要加载的关联字段名，如果为空则加载所有关联
     */
    void loadRelations(@NotNull Object entity, @Nullable String... relationFields);
}
