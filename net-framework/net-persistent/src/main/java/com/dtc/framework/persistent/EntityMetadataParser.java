package com.dtc.framework.persistent;

import com.dtc.api.annotations.NotNull;
import com.dtc.annotations.persistence.*;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 实体元数据解析器
 * 解析实体类型上的注解，生成元数据
 * 
 * @author Network Service Template
 */
public class EntityMetadataParser {
    
    /**
     * 解析实体类型并生成元数据
     * 
     * @param entityClass 实体类型
     * @return 元数据
     */
    @NotNull
    public static EntityMetadata parse(@NotNull Class<?> entityClass) {
        // 检查是否有@Table注解
        Table tableAnnotation = entityClass.getAnnotation(Table.class);
        if (tableAnnotation == null) {
            throw new IllegalArgumentException("Entity class must be annotated with @Table: " + entityClass.getName());
        }
        
        String tableName = tableAnnotation.value();
        String schema = tableAnnotation.schema();
        boolean camelToUnderline = tableAnnotation.camelToUnderline();
        
        // 解析字段
        Field primaryKeyField = null;
        Map<String, String> fieldColumnMapping = new HashMap<>();
        List<String> columnNames = new ArrayList<>();
        
        List<Field> oneToOneFields = new ArrayList<>();
        List<Field> oneToManyFields = new ArrayList<>();
        List<Field> manyToOneFields = new ArrayList<>();
        List<Field> manyToManyFields = new ArrayList<>();
        
        Field[] fields = entityClass.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            
            // 检查主键
            if (field.isAnnotationPresent(Id.class)) {
                if (primaryKeyField != null) {
                    throw new IllegalArgumentException("Entity can only have one @Id field: " + entityClass.getName());
                }
                primaryKeyField = field;
            }
            
            // 检查字段映射
            Column columnAnnotation = field.getAnnotation(Column.class);
            if (columnAnnotation != null && !columnAnnotation.ignore()) {
                String columnName = columnAnnotation.value();
                if (columnName.isEmpty()) {
                    // 使用camelToUnderline转换
                    columnName = camelToUnderline ? camelToUnderline(field.getName()) : field.getName();
                }
                fieldColumnMapping.put(field.getName(), columnName);
                columnNames.add(columnName);
            } else if (!field.isAnnotationPresent(Id.class) && 
                       !field.isAnnotationPresent(OneToOne.class) &&
                       !field.isAnnotationPresent(OneToMany.class) &&
                       !field.isAnnotationPresent(ManyToOne.class) &&
                       !field.isAnnotationPresent(ManyToMany.class)) {
                // 普通字段，自动映射
                String columnName = camelToUnderline ? camelToUnderline(field.getName()) : field.getName();
                fieldColumnMapping.put(field.getName(), columnName);
                columnNames.add(columnName);
            }
            
            // 检查关联关系
            if (field.isAnnotationPresent(OneToOne.class)) {
                oneToOneFields.add(field);
            } else if (field.isAnnotationPresent(OneToMany.class)) {
                oneToManyFields.add(field);
            } else if (field.isAnnotationPresent(ManyToOne.class)) {
                manyToOneFields.add(field);
            } else if (field.isAnnotationPresent(ManyToMany.class)) {
                manyToManyFields.add(field);
            }
        }
        
        if (primaryKeyField == null) {
            throw new IllegalArgumentException("Entity must have one @Id field: " + entityClass.getName());
        }
        
        return new EntityMetadataImpl(
            entityClass,
            tableName,
            schema,
            primaryKeyField,
            fieldColumnMapping,
            columnNames,
            oneToOneFields,
            oneToManyFields,
            manyToOneFields,
            manyToManyFields
        );
    }
    
    /**
     * 驼峰转下划线
     */
    @NotNull
    private static String camelToUnderline(@NotNull String camel) {
        if (camel.isEmpty()) {
            return camel;
        }
        
        StringBuilder result = new StringBuilder();
        result.append(Character.toLowerCase(camel.charAt(0)));
        
        for (int i = 1; i < camel.length(); i++) {
            char c = camel.charAt(i);
            if (Character.isUpperCase(c)) {
                result.append('_').append(Character.toLowerCase(c));
            } else {
                result.append(c);
            }
        }
        
        return result.toString();
    }
    
    /**
     * 实体元数据实现
     */
    private static class EntityMetadataImpl implements EntityMetadata {
        private final Class<?> entityClass;
        private final String tableName;
        private final String schema;
        private final Field primaryKeyField;
        private final Map<String, String> fieldColumnMapping;
        private final List<String> columnNames;
        private final List<Field> oneToOneFields;
        private final List<Field> oneToManyFields;
        private final List<Field> manyToOneFields;
        private final List<Field> manyToManyFields;
        
        public EntityMetadataImpl(
            Class<?> entityClass,
            String tableName,
            String schema,
            Field primaryKeyField,
            Map<String, String> fieldColumnMapping,
            List<String> columnNames,
            List<Field> oneToOneFields,
            List<Field> oneToManyFields,
            List<Field> manyToOneFields,
            List<Field> manyToManyFields
        ) {
            this.entityClass = entityClass;
            this.tableName = tableName;
            this.schema = schema;
            this.primaryKeyField = primaryKeyField;
            this.fieldColumnMapping = new ConcurrentHashMap<>(fieldColumnMapping);
            this.columnNames = new ArrayList<>(columnNames);
            this.oneToOneFields = new ArrayList<>(oneToOneFields);
            this.oneToManyFields = new ArrayList<>(oneToManyFields);
            this.manyToOneFields = new ArrayList<>(manyToOneFields);
            this.manyToManyFields = new ArrayList<>(manyToManyFields);
        }
        
        @Override
        public Class<?> getEntityClass() {
            return entityClass;
        }
        
        @Override
        public String getTableName() {
            return tableName;
        }
        
        @Override
        public String getSchema() {
            return schema;
        }
        
        @Override
        public Field getPrimaryKeyField() {
            return primaryKeyField;
        }
        
        @Override
        public Map<String, String> getFieldColumnMapping() {
            return new HashMap<>(fieldColumnMapping);
        }
        
        @Override
        public List<String> getColumnNames() {
            return new ArrayList<>(columnNames);
        }
        
        @Override
        public List<Field> getOneToOneFields() {
            return new ArrayList<>(oneToOneFields);
        }
        
        @Override
        public List<Field> getOneToManyFields() {
            return new ArrayList<>(oneToManyFields);
        }
        
        @Override
        public List<Field> getManyToOneFields() {
            return new ArrayList<>(manyToOneFields);
        }
        
        @Override
        public List<Field> getManyToManyFields() {
            return new ArrayList<>(manyToManyFields);
        }
        
        @Override
        public String getColumnName(String fieldName) {
            return fieldColumnMapping.get(fieldName);
        }
        
        @Override
        public String getFieldName(String columnName) {
            for (Map.Entry<String, String> entry : fieldColumnMapping.entrySet()) {
                if (entry.getValue().equals(columnName)) {
                    return entry.getKey();
                }
            }
            return null;
        }
    }
}
