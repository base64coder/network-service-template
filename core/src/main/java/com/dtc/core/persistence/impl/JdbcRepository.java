package com.dtc.core.persistence.impl;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import com.dtc.core.persistence.*;
import com.dtc.annotations.persistence.Id;
import com.dtc.annotations.persistence.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.*;
import java.util.*;

/**
 * JDBC Repository实现
 * 基于JDBC的Repository实现，提供通用的CRUD操作
 * 
 * @param <T> 实体类型
 * @param <ID> 主键类型
 * 
 * @author Network Service Template
 */
public class JdbcRepository<T, ID> implements BaseRepository<T, ID> {
    
    private static final Logger log = LoggerFactory.getLogger(JdbcRepository.class);
    
    private final DataSourceProvider dataSourceProvider;
    private final Class<T> entityClass;
    private final EntityMetadata metadata;
    private final Field primaryKeyField;
    private final String primaryKeyColumn;
    
    @Inject
    public JdbcRepository(DataSourceProvider dataSourceProvider) {
        this.dataSourceProvider = dataSourceProvider;
        
        // 通过泛型获取实体类型
        Type superClass = getClass().getGenericSuperclass();
        if (superClass instanceof ParameterizedType) {
            Type[] typeArgs = ((ParameterizedType) superClass).getActualTypeArguments();
            @SuppressWarnings("unchecked")
            Class<T> entityClass = (Class<T>) typeArgs[0];
            this.entityClass = entityClass;
        } else {
            throw new IllegalStateException("Cannot determine entity class from generic type");
        }
        
        // 获取实体元数据
        this.metadata = EntityMetadataRegistry.getOrCreate(entityClass);
        this.primaryKeyField = metadata.getPrimaryKeyField();
        this.primaryKeyColumn = metadata.getColumnName(primaryKeyField.getName());
        
        log.info("Initialized JdbcRepository for entity: {}", entityClass.getSimpleName());
    }
    
    @Override
    @Nullable
    public T findById(@NotNull ID id) {
        String sql = buildSelectByIdSql();
        log.debug("Executing SQL: {} with id: {}", sql, id);
        
        try (Connection conn = dataSourceProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            setParameter(stmt, 1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToEntity(rs);
                }
            }
        } catch (SQLException e) {
            log.error("Failed to find entity by id: {}", id, e);
            throw new RuntimeException("Failed to find entity by id", e);
        }
        
        return null;
    }
    
    @Override
    @NotNull
    public Optional<T> findByIdOptional(@NotNull ID id) {
        T entity = findById(id);
        return Optional.ofNullable(entity);
    }
    
    @Override
    @NotNull
    public List<T> findAll() {
        String sql = buildSelectAllSql();
        log.debug("Executing SQL: {}", sql);
        
        List<T> results = new ArrayList<>();
        
        try (Connection conn = dataSourceProvider.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                results.add(mapRowToEntity(rs));
            }
        } catch (SQLException e) {
            log.error("Failed to find all entities", e);
            throw new RuntimeException("Failed to find all entities", e);
        }
        
        return results;
    }
    
    @Override
    @NotNull
    public T save(@NotNull T entity) {
        ID id = getPrimaryKeyValue(entity);
        
        if (id == null || isNewEntity(id)) {
            return insert(entity);
        } else {
            return updateEntity(entity);
        }
    }
    
    @Override
    @NotNull
    public List<T> saveAll(@NotNull List<T> entities) {
        List<T> results = new ArrayList<>();
        for (T entity : entities) {
            results.add(save(entity));
        }
        return results;
    }
    
    @Override
    public boolean deleteById(@NotNull ID id) {
        String sql = buildDeleteByIdSql();
        log.debug("Executing SQL: {} with id: {}", sql, id);
        
        try (Connection conn = dataSourceProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            setParameter(stmt, 1, id);
            
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            log.error("Failed to delete entity by id: {}", id, e);
            throw new RuntimeException("Failed to delete entity by id", e);
        }
    }
    
    @Override
    public boolean delete(@NotNull T entity) {
        ID id = getPrimaryKeyValue(entity);
        if (id == null) {
            return false;
        }
        return deleteById(id);
    }
    
    @Override
    public int deleteAll(@NotNull List<T> entities) {
        int count = 0;
        for (T entity : entities) {
            if (delete(entity)) {
                count++;
            }
        }
        return count;
    }
    
    @Override
    public boolean existsById(@NotNull ID id) {
        return findById(id) != null;
    }
    
    @Override
    public long count() {
        String sql = buildCountSql();
        log.debug("Executing SQL: {}", sql);
        
        try (Connection conn = dataSourceProvider.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            log.error("Failed to count entities", e);
            throw new RuntimeException("Failed to count entities", e);
        }
        
        return 0;
    }
    
    @NotNull
    public T update(@NotNull T entity) {
        return save(entity);
    }
    
    // ========== 私有辅助方法 ==========
    
    /**
     * 插入实体
     */
    @NotNull
    private T insert(@NotNull T entity) {
        String sql = buildInsertSql();
        log.debug("Executing SQL: {}", sql);
        
        try (Connection conn = dataSourceProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            setInsertParameters(stmt, entity);
            
            int rows = stmt.executeUpdate();
            if (rows == 0) {
                throw new RuntimeException("Failed to insert entity");
            }
            
            // 处理自动生成主键
            Id.KeyType keyType = primaryKeyField.getAnnotation(Id.class).keyType();
            if (keyType == Id.KeyType.AUTO) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        Object generatedId = generatedKeys.getObject(1);
                        setPrimaryKeyValue(entity, generatedId);
                    }
                }
            }
            
            return entity;
        } catch (SQLException e) {
            log.error("Failed to insert entity", e);
            throw new RuntimeException("Failed to insert entity", e);
        }
    }
    
    /**
     * 更新实体（使用save方法）
     */
    @NotNull
    private T updateEntity(@NotNull T entity) {
        String sql = buildUpdateSql();
        log.debug("Executing SQL: {}", sql);
        
        try (Connection conn = dataSourceProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            setUpdateParameters(stmt, entity);
            ID id = getPrimaryKeyValue(entity);
            setParameter(stmt, getUpdateParameterCount(), id);
            
            int rows = stmt.executeUpdate();
            if (rows == 0) {
                throw new RuntimeException("Failed to update entity");
            }
            
            return entity;
        } catch (SQLException e) {
            log.error("Failed to update entity", e);
            throw new RuntimeException("Failed to update entity", e);
        }
    }
    
    /**
     * 检查是否为新实体
     */
    private boolean isNewEntity(@NotNull ID id) {
        // 简单判断：如果ID为null或为0，则为新实体
        if (id instanceof Number) {
            return ((Number) id).longValue() == 0;
        }
        return false;
    }
    
    /**
     * 构建查询SQL
     */
    @NotNull
    private String buildSelectByIdSql() {
        String tableName = getTableName();
        return String.format("SELECT * FROM %s WHERE %s = ?", tableName, primaryKeyColumn);
    }
    
    @NotNull
    private String buildSelectAllSql() {
        String tableName = getTableName();
        return String.format("SELECT * FROM %s", tableName);
    }
    
    @NotNull
    private String buildCountSql() {
        String tableName = getTableName();
        return String.format("SELECT COUNT(*) FROM %s", tableName);
    }
    
    @NotNull
    private String buildDeleteByIdSql() {
        String tableName = getTableName();
        return String.format("DELETE FROM %s WHERE %s = ?", tableName, primaryKeyColumn);
    }
    
    @NotNull
    private String buildInsertSql() {
        String tableName = getTableName();
        List<String> columns = new ArrayList<>();
        List<String> placeholders = new ArrayList<>();
        
        Map<String, String> fieldColumnMapping = metadata.getFieldColumnMapping();
        for (Map.Entry<String, String> entry : fieldColumnMapping.entrySet()) {
            String fieldName = entry.getKey();
            String columnName = entry.getValue();
            
            // 跳过主键字段茂录聢氓娄聜忙聻聹忙聵炉猫聡陋氓垄聻茂录?
            if (fieldName.equals(primaryKeyField.getName())) {
                Id.KeyType keyType = primaryKeyField.getAnnotation(Id.class).keyType();
                if (keyType == Id.KeyType.AUTO) {
                    continue;
                }
            }
            
            columns.add(columnName);
            placeholders.add("?");
        }
        
        return String.format("INSERT INTO %s (%s) VALUES (%s)",
            tableName,
            String.join(", ", columns),
            String.join(", ", placeholders));
    }
    
    @NotNull
    private String buildUpdateSql() {
        String tableName = getTableName();
        List<String> setClauses = new ArrayList<>();
        
        Map<String, String> fieldColumnMapping = metadata.getFieldColumnMapping();
        for (Map.Entry<String, String> entry : fieldColumnMapping.entrySet()) {
            String fieldName = entry.getKey();
            String columnName = entry.getValue();
            
            // 跳过主键字段
            if (fieldName.equals(primaryKeyField.getName())) {
                continue;
            }
            
            setClauses.add(columnName + " = ?");
        }
        
        return String.format("UPDATE %s SET %s WHERE %s = ?",
            tableName,
            String.join(", ", setClauses),
            primaryKeyColumn);
    }
    
    /**
     * 设置插入参数
     */
    private void setInsertParameters(@NotNull PreparedStatement stmt, @NotNull T entity) throws SQLException {
        int index = 1;
        Map<String, String> fieldColumnMapping = metadata.getFieldColumnMapping();
        
        for (Map.Entry<String, String> entry : fieldColumnMapping.entrySet()) {
            String fieldName = entry.getKey();
            
            // 跳过主键字段茂录聢氓娄聜忙聻聹忙聵炉猫聡陋氓垄聻茂录?
            if (fieldName.equals(primaryKeyField.getName())) {
                Id.KeyType keyType = primaryKeyField.getAnnotation(Id.class).keyType();
                if (keyType == Id.KeyType.AUTO) {
                    continue;
                }
            }
            
            Object value = getFieldValue(entity, fieldName);
            setParameter(stmt, index++, value);
        }
    }
    
    /**
     * 设置更新参数
     */
    private void setUpdateParameters(@NotNull PreparedStatement stmt, @NotNull T entity) throws SQLException {
        int index = 1;
        Map<String, String> fieldColumnMapping = metadata.getFieldColumnMapping();
        
        for (Map.Entry<String, String> entry : fieldColumnMapping.entrySet()) {
            String fieldName = entry.getKey();
            
            // 跳过主键字段
            if (fieldName.equals(primaryKeyField.getName())) {
                continue;
            }
            
            Object value = getFieldValue(entity, fieldName);
            setParameter(stmt, index++, value);
        }
    }
    
    /**
     * 获取更新参数数量
     */
    private int getUpdateParameterCount() {
        return metadata.getFieldColumnMapping().size(); // 减去主键字段
    }
    
    /**
     * 将ResultSet行映射为实体对象
     */
    @NotNull
    private T mapRowToEntity(@NotNull ResultSet rs) throws SQLException {
        try {
            T entity = entityClass.getDeclaredConstructor().newInstance();
            Map<String, String> fieldColumnMapping = metadata.getFieldColumnMapping();
            
            for (Map.Entry<String, String> entry : fieldColumnMapping.entrySet()) {
                String fieldName = entry.getKey();
                String columnName = entry.getValue();
                
                Field field = entityClass.getDeclaredField(fieldName);
                field.setAccessible(true);
                
                Object value = rs.getObject(columnName);
                if (value != null) {
                    // 类型转换
                    value = convertValue(value, field.getType());
                    field.set(entity, value);
                }
            }
            
            return entity;
        } catch (Exception e) {
            throw new RuntimeException("Failed to map row to entity", e);
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
        
        // 简单的类型转换
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
     * 获取字段值
     */
    @Nullable
    private Object getFieldValue(@NotNull T entity, @NotNull String fieldName) {
        try {
            Field field = entityClass.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(entity);
        } catch (Exception e) {
            log.error("Failed to get field value: {}", fieldName, e);
            return null;
        }
    }
    
    /**
     * 获取主键值
     */
    @Nullable
    @SuppressWarnings("unchecked")
    private ID getPrimaryKeyValue(@NotNull T entity) {
        try {
            primaryKeyField.setAccessible(true);
            return (ID) primaryKeyField.get(entity);
        } catch (Exception e) {
            log.error("Failed to get primary key value", e);
            return null;
        }
    }
    
    /**
     * 设置主键值
     */
    private void setPrimaryKeyValue(@NotNull T entity, @NotNull Object id) {
        try {
            primaryKeyField.setAccessible(true);
            primaryKeyField.set(entity, convertValue(id, primaryKeyField.getType()));
        } catch (Exception e) {
            log.error("Failed to set primary key value", e);
        }
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
    
    /**
     * 获取表名
     */
    @NotNull
    private String getTableName() {
        String tableName = metadata.getTableName();
        String schema = metadata.getSchema();
        if (schema != null && !schema.isEmpty()) {
            return schema + "." + tableName;
        }
        return tableName;
    }
}

