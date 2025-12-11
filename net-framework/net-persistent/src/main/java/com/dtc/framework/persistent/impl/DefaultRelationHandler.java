package com.dtc.framework.persistent.impl;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import com.dtc.framework.persistent.*;
import com.dtc.annotations.persistence.*;
import java.lang.reflect.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.sql.*;
import java.util.*;

/**
 * 默认关联关系处理器实现
 * 支持OneToOne、OneToMany、ManyToOne、ManyToMany关联关系的处理
 * 
 * @author Network Service Template
 */
@Singleton
public class DefaultRelationHandler implements RelationHandler {
    
    private static final Logger log = LoggerFactory.getLogger(DefaultRelationHandler.class);
    
    private final DataSourceProvider dataSourceProvider;
    
    @Inject
    public DefaultRelationHandler(DataSourceProvider dataSourceProvider) {
        this.dataSourceProvider = dataSourceProvider;
    }
    
    @Override
    @Nullable
    public Object handleOneToOne(@NotNull Object entity, @NotNull Field field, @NotNull OneToOne annotation) {
        try {
            EntityMetadata metadata = EntityMetadataRegistry.getOrCreate(entity.getClass());
            String selfField = annotation.selfField();
            if (selfField.isEmpty()) {
                selfField = field.getName();
            }
            
            Object selfValue = getFieldValue(entity, selfField);
            if (selfValue == null) {
                return null;
            }
            
            String targetTable = annotation.targetTable();
            String targetField = annotation.targetField();
            
            // 如果有连接表配置
            if (!annotation.joinTable().isEmpty()) {
                return handleOneToOneViaJoinTable(entity, field, annotation, selfValue);
            }
            
            // 否则使用外键关联
            Class<?> targetType = field.getType();
            EntityMetadata targetMetadata = EntityMetadataRegistry.getOrCreate(targetType);
            if (targetTable.isEmpty()) {
                targetTable = targetMetadata.getTableName();
            }
            
            String sql = String.format("SELECT * FROM %s WHERE %s = ?",
                targetTable, targetMetadata.getColumnName(targetField));
            
            if (!annotation.extraCondition().isEmpty()) {
                sql += " AND " + annotation.extraCondition();
            }
            
            return querySingleEntity(sql, targetType, selfValue);
            
        } catch (Exception e) {
            log.error("Failed to handle OneToOne relation", e);
            return null;
        }
    }
    
    @Override
    @NotNull
    public Collection<?> handleOneToMany(@NotNull Object entity, @NotNull Field field, @NotNull OneToMany annotation) {
        try {
            EntityMetadata metadata = EntityMetadataRegistry.getOrCreate(entity.getClass());
            String selfField = annotation.selfField();
            if (selfField.isEmpty()) {
                // 使用主键
                selfField = metadata.getPrimaryKeyField().getName();
            }
            
            Object selfValue = getFieldValue(entity, selfField);
            if (selfValue == null) {
                return Collections.emptyList();
            }
            
            String targetTable = annotation.targetTable();
            String targetField = annotation.targetField();
            
            // OneToMany通常不使用连接表，使用外键
            
            // 否则使用外键关联
            Class<?> targetType = getCollectionElementType(field);
            EntityMetadata targetMetadata = EntityMetadataRegistry.getOrCreate(targetType);
            if (targetTable.isEmpty()) {
                targetTable = targetMetadata.getTableName();
            }
            
            String sql = String.format("SELECT * FROM %s WHERE %s = ?",
                targetTable, targetMetadata.getColumnName(targetField));
            
            if (!annotation.extraCondition().isEmpty()) {
                sql += " AND " + annotation.extraCondition();
            }
            
            if (!annotation.orderBy().isEmpty()) {
                sql += " ORDER BY " + annotation.orderBy();
            }
            
            return queryEntityList(sql, targetType, selfValue);
            
        } catch (Exception e) {
            log.error("Failed to handle OneToMany relation", e);
            return Collections.emptyList();
        }
    }
    
    @Override
    @Nullable
    public Object handleManyToOne(@NotNull Object entity, @NotNull Field field, @NotNull ManyToOne annotation) {
        try {
            String selfField = annotation.selfField();
            Object selfValue = getFieldValue(entity, selfField);
            if (selfValue == null) {
                return null;
            }
            
            String targetTable = annotation.targetTable();
            String targetField = annotation.targetField();
            
            Class<?> targetType = field.getType();
            EntityMetadata targetMetadata = EntityMetadataRegistry.getOrCreate(targetType);
            if (targetTable.isEmpty()) {
                targetTable = targetMetadata.getTableName();
            }
            
            if (targetField.isEmpty()) {
                // 使用目标实体的主键
                targetField = targetMetadata.getPrimaryKeyField().getName();
            }
            
            String sql = String.format("SELECT * FROM %s WHERE %s = ?",
                targetTable, targetMetadata.getColumnName(targetField));
            
            if (!annotation.extraCondition().isEmpty()) {
                sql += " AND " + annotation.extraCondition();
            }
            
            return querySingleEntity(sql, targetType, selfValue);
            
        } catch (Exception e) {
            log.error("Failed to handle ManyToOne relation", e);
            return null;
        }
    }
    
    @Override
    @NotNull
    public Collection<?> handleManyToMany(@NotNull Object entity, @NotNull Field field, @NotNull ManyToMany annotation) {
        try {
            EntityMetadata metadata = EntityMetadataRegistry.getOrCreate(entity.getClass());
            String selfField = annotation.selfField();
            if (selfField.isEmpty()) {
                selfField = metadata.getPrimaryKeyField().getName();
            }
            
            Object selfValue = getFieldValue(entity, selfField);
            if (selfValue == null) {
                return Collections.emptyList();
            }
            
            String joinTable = annotation.joinTable();
            String joinSelfColumn = annotation.joinSelfColumn();
            String joinTargetColumn = annotation.joinTargetColumn();
            
            Class<?> targetType = getCollectionElementType(field);
            EntityMetadata targetMetadata = EntityMetadataRegistry.getOrCreate(targetType);
            
            String targetTable = annotation.targetTable();
            if (targetTable.isEmpty()) {
                targetTable = targetMetadata.getTableName();
            }
            
            String targetField = annotation.targetField();
            if (targetField.isEmpty()) {
                targetField = targetMetadata.getPrimaryKeyField().getName();
            }
            String targetColumn = targetMetadata.getColumnName(targetField);
            
            // 通过连接表查询多对多关联
            String sql = String.format(
                "SELECT t.* FROM %s t " +
                "INNER JOIN %s j ON t.%s = j.%s " +
                "WHERE j.%s = ?",
                targetTable, joinTable, targetColumn, joinTargetColumn, joinSelfColumn);
            
            if (!annotation.extraCondition().isEmpty()) {
                sql += " AND " + annotation.extraCondition();
            }
            
            if (!annotation.orderBy().isEmpty()) {
                sql += " ORDER BY " + annotation.orderBy();
            }
            
            return queryEntityList(sql, targetType, selfValue);
            
        } catch (Exception e) {
            log.error("Failed to handle ManyToMany relation", e);
            return Collections.emptyList();
        }
    }
    
    @Override
    public void loadRelations(@NotNull List<?> entities, @Nullable String... relationFields) {
        if (entities.isEmpty()) {
            return;
        }
        
        Set<String> fieldsToLoad = relationFields != null && relationFields.length > 0
            ? Set.of(relationFields)
            : null;
        
        for (Object entity : entities) {
            loadRelations(entity, fieldsToLoad);
        }
    }
    
    @Override
    public void loadRelations(@NotNull Object entity, @Nullable String... relationFields) {
        Set<String> fieldsToLoad = relationFields != null && relationFields.length > 0
            ? Set.of(relationFields)
            : null;
        
        loadRelations(entity, fieldsToLoad);
    }
    
    /**
     * 加载关联关系，使用Set参数的重载方法
     */
    private void loadRelations(@NotNull Object entity, @Nullable Set<String> fieldsToLoad) {
        try {
            EntityMetadata metadata = EntityMetadataRegistry.getOrCreate(entity.getClass());
            
            // 处理OneToOne
            for (Field field : metadata.getOneToOneFields()) {
                if (fieldsToLoad == null || fieldsToLoad.contains(field.getName())) {
                    OneToOne annotation = field.getAnnotation(OneToOne.class);
                    if (annotation.fetch() == FetchType.EAGER) {
                        Object related = handleOneToOne(entity, field, annotation);
                        setFieldValue(entity, field, related);
                    }
                }
            }
            
            // 处理OneToMany
            for (Field field : metadata.getOneToManyFields()) {
                if (fieldsToLoad == null || fieldsToLoad.contains(field.getName())) {
                    OneToMany annotation = field.getAnnotation(OneToMany.class);
                    if (annotation.fetch() == FetchType.EAGER) {
                        Collection<?> related = handleOneToMany(entity, field, annotation);
                        setFieldValue(entity, field, related);
                    }
                }
            }
            
            // 处理ManyToOne
            for (Field field : metadata.getManyToOneFields()) {
                if (fieldsToLoad == null || fieldsToLoad.contains(field.getName())) {
                    ManyToOne annotation = field.getAnnotation(ManyToOne.class);
                    if (annotation.fetch() == FetchType.EAGER) {
                        Object related = handleManyToOne(entity, field, annotation);
                        setFieldValue(entity, field, related);
                    }
                }
            }
            
            // 处理ManyToMany
            for (Field field : metadata.getManyToManyFields()) {
                if (fieldsToLoad == null || fieldsToLoad.contains(field.getName())) {
                    ManyToMany annotation = field.getAnnotation(ManyToMany.class);
                    if (annotation.fetch() == FetchType.EAGER) {
                        Collection<?> related = handleManyToMany(entity, field, annotation);
                        setFieldValue(entity, field, related);
                    }
                }
            }
            
        } catch (Exception e) {
            log.error("Failed to load relations for entity", e);
        }
    }
    
    // ========== 私有辅助方法 ==========
    
    /**
     * 通过连接表处理OneToOne
     */
    @Nullable
    private Object handleOneToOneViaJoinTable(@NotNull Object entity, @NotNull Field field,
                                               @NotNull OneToOne annotation, @NotNull Object selfValue) {
        // 可以通过路由管理器实现连接表的关联
        // 为了简化，这里暂时不实现，可以通过数据库查询连接表
        return null;
    }
    
    /**
     * 通过连接表处理OneToMany
     */
    @NotNull
    private Collection<?> handleOneToManyViaJoinTable(@NotNull Object entity, @NotNull Field field,
                                                       @NotNull OneToMany annotation, @NotNull Object selfValue) {
        // 可以通过路由管理器实现连接表的关联
        return Collections.emptyList();
    }
    
    /**
     * 查询单个实体
     */
    @Nullable
    private Object querySingleEntity(@NotNull String sql, @NotNull Class<?> entityType, @NotNull Object param) {
        log.debug("Executing relation SQL: {} with param: {}", sql, param);
        
        try (Connection conn = dataSourceProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            setParameter(stmt, 1, param);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToEntity(rs, entityType);
                }
            }
        } catch (SQLException e) {
            log.error("Failed to query single entity", e);
        }
        
        return null;
    }
    
    /**
     * 查询实体列表
     */
    @NotNull
    private List<Object> queryEntityList(@NotNull String sql, @NotNull Class<?> entityType, @NotNull Object param) {
        log.debug("Executing relation SQL: {} with param: {}", sql, param);
        
        List<Object> results = new ArrayList<>();
        
        try (Connection conn = dataSourceProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            setParameter(stmt, 1, param);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Object entity = mapRowToEntity(rs, entityType);
                    if (entity != null) {
                        results.add(entity);
                    }
                }
            }
        } catch (SQLException e) {
            log.error("Failed to query entity list", e);
        }
        
        return results;
    }
    
    /**
     * 将ResultSet行映射为实体对象
     */
    @Nullable
    private Object mapRowToEntity(@NotNull ResultSet rs, @NotNull Class<?> entityType) throws SQLException {
        try {
            Object entity = entityType.getDeclaredConstructor().newInstance();
            EntityMetadata metadata = EntityMetadataRegistry.getOrCreate(entityType);
            Map<String, String> fieldColumnMapping = metadata.getFieldColumnMapping();
            
            for (Map.Entry<String, String> entry : fieldColumnMapping.entrySet()) {
                String fieldName = entry.getKey();
                String columnName = entry.getValue();
                
                Field field = entityType.getDeclaredField(fieldName);
                field.setAccessible(true);
                
                Object value = rs.getObject(columnName);
                if (value != null) {
                    value = convertValue(value, field.getType());
                    field.set(entity, value);
                }
            }
            
            return entity;
        } catch (Exception e) {
            log.error("Failed to map row to entity", e);
            return null;
        }
    }
    
    /**
     * 获取集合元素类型
     */
    @NotNull
    private Class<?> getCollectionElementType(@NotNull Field field) {
        Type genericType = field.getGenericType();
        if (genericType instanceof ParameterizedType) {
            Type[] actualTypes = ((ParameterizedType) genericType).getActualTypeArguments();
            if (actualTypes.length > 0) {
                return (Class<?>) actualTypes[0];
            }
        }
        throw new IllegalArgumentException("Cannot determine collection element type for field: " + field.getName());
    }
    
    /**
     * 获取字段值
     */
    @Nullable
    private Object getFieldValue(@NotNull Object entity, @NotNull String fieldName) {
        try {
            Field field = entity.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(entity);
        } catch (Exception e) {
            log.error("Failed to get field value: {}", fieldName, e);
            return null;
        }
    }
    
    /**
     * 设置字段值
     */
    private void setFieldValue(@NotNull Object entity, @NotNull Field field, @Nullable Object value) {
        try {
            field.setAccessible(true);
            field.set(entity, value);
        } catch (Exception e) {
            log.error("Failed to set field value: {}", field.getName(), e);
        }
    }
    
    /**
     * 类型转换
     */
    @Nullable
    private Object convertValue(@NotNull Object value, @NotNull Class<?> targetType) {
        if (targetType.isAssignableFrom(value.getClass())) {
            return value;
        }
        
        if (targetType == Long.class || targetType == long.class) {
            if (value instanceof Number) {
                return ((Number) value).longValue();
            }
        } else if (targetType == Integer.class || targetType == int.class) {
            if (value instanceof Number) {
                return ((Number) value).intValue();
            }
        } else if (targetType == String.class) {
            return value.toString();
        }
        
        return value;
    }
    
    /**
     * 设置PreparedStatement参数
     */
    private void setParameter(@NotNull PreparedStatement stmt, int index, @Nullable Object value) throws SQLException {
        if (value == null) {
            stmt.setNull(index, Types.NULL);
        } else {
            stmt.setObject(index, value);
        }
    }
}
