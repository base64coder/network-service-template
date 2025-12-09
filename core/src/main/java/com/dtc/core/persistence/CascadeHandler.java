package com.dtc.core.persistence;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import com.dtc.core.persistence.impl.DefaultRelationHandler;
import com.dtc.annotations.persistence.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.reflect.Field;
import java.util.Collection;

/**
 * 级联操作处理器
 * 处理实体之间的级联保存、更新、删除等操作
 * 
 * @author Network Service Template
 */
@Singleton
public class CascadeHandler {
    
    private static final Logger log = LoggerFactory.getLogger(CascadeHandler.class);
    
    private final DefaultRelationHandler relationHandler;
    
    @Inject
    public CascadeHandler(DefaultRelationHandler relationHandler) {
        this.relationHandler = relationHandler;
    }
    
    /**
     * 处理级联保存
     * 
     * @param entity 实体对象
     * @param repository 实体对象对应的Repository
     */
    public void handleCascadePersist(@NotNull Object entity, @NotNull BaseRepository<?, ?> repository) {
        EntityMetadata metadata = EntityMetadataRegistry.getOrCreate(entity.getClass());
        
        // 处理OneToOne级联
        for (Field field : metadata.getOneToOneFields()) {
            OneToOne annotation = field.getAnnotation(OneToOne.class);
            if (hasCascade(annotation.cascade(), CascadeType.ALL, CascadeType.PERSIST)) {
                Object related = getFieldValue(entity, field);
                if (related != null) {
                    // 递归保存关联实体
                    saveRelatedEntity(related, repository);
                }
            }
        }
        
        // 处理OneToMany级联
        for (Field field : metadata.getOneToManyFields()) {
            OneToMany annotation = field.getAnnotation(OneToMany.class);
            if (hasCascade(annotation.cascade(), CascadeType.ALL, CascadeType.PERSIST)) {
                Collection<?> related = getCollectionFieldValue(entity, field);
                if (related != null) {
                    for (Object item : related) {
                        saveRelatedEntity(item, repository);
                    }
                }
            }
        }
        
        // 处理ManyToOne级联
        for (Field field : metadata.getManyToOneFields()) {
            ManyToOne annotation = field.getAnnotation(ManyToOne.class);
            if (hasCascade(annotation.cascade(), CascadeType.ALL, CascadeType.PERSIST)) {
                Object related = getFieldValue(entity, field);
                if (related != null) {
                    saveRelatedEntity(related, repository);
                }
            }
        }
        
        // 处理ManyToMany级联
        for (Field field : metadata.getManyToManyFields()) {
            ManyToMany annotation = field.getAnnotation(ManyToMany.class);
            if (hasCascade(annotation.cascade(), CascadeType.ALL, CascadeType.PERSIST)) {
                Collection<?> related = getCollectionFieldValue(entity, field);
                if (related != null) {
                    for (Object item : related) {
                        saveRelatedEntity(item, repository);
                    }
                }
            }
        }
    }
    
    /**
     * 处理级联删除
     * 
     * @param entity 实体对象
     * @param repository 实体对象对应的Repository
     */
    public void handleCascadeRemove(@NotNull Object entity, @NotNull BaseRepository<?, ?> repository) {
        EntityMetadata metadata = EntityMetadataRegistry.getOrCreate(entity.getClass());
        
        // 处理OneToOne级联
        for (Field field : metadata.getOneToOneFields()) {
            OneToOne annotation = field.getAnnotation(OneToOne.class);
            if (hasCascade(annotation.cascade(), CascadeType.ALL, CascadeType.REMOVE)) {
                Object related = getFieldValue(entity, field);
                if (related != null) {
                    deleteRelatedEntity(related, repository);
                }
            }
        }
        
        // 处理OneToMany级联
        for (Field field : metadata.getOneToManyFields()) {
            OneToMany annotation = field.getAnnotation(OneToMany.class);
            if (hasCascade(annotation.cascade(), CascadeType.ALL, CascadeType.REMOVE)) {
                Collection<?> related = getCollectionFieldValue(entity, field);
                if (related != null) {
                    for (Object item : related) {
                        deleteRelatedEntity(item, repository);
                    }
                }
            }
        }
        
        // ManyToOne和ManyToMany通常不级联删除
    }
    
    /**
     * 处理级联更新
     * 
     * @param entity 实体对象
     * @param repository 实体对象对应的Repository
     */
    public void handleCascadeMerge(@NotNull Object entity, @NotNull BaseRepository<?, ?> repository) {
        EntityMetadata metadata = EntityMetadataRegistry.getOrCreate(entity.getClass());
        
        // 处理OneToOne级联
        for (Field field : metadata.getOneToOneFields()) {
            OneToOne annotation = field.getAnnotation(OneToOne.class);
            if (hasCascade(annotation.cascade(), CascadeType.ALL, CascadeType.MERGE)) {
                Object related = getFieldValue(entity, field);
                if (related != null) {
                    updateRelatedEntity(related, repository);
                }
            }
        }
        
        // 处理OneToMany级联
        for (Field field : metadata.getOneToManyFields()) {
            OneToMany annotation = field.getAnnotation(OneToMany.class);
            if (hasCascade(annotation.cascade(), CascadeType.ALL, CascadeType.MERGE)) {
                Collection<?> related = getCollectionFieldValue(entity, field);
                if (related != null) {
                    for (Object item : related) {
                        updateRelatedEntity(item, repository);
                    }
                }
            }
        }
        
        // 处理ManyToOne级联
        for (Field field : metadata.getManyToOneFields()) {
            ManyToOne annotation = field.getAnnotation(ManyToOne.class);
            if (hasCascade(annotation.cascade(), CascadeType.ALL, CascadeType.MERGE)) {
                Object related = getFieldValue(entity, field);
                if (related != null) {
                    updateRelatedEntity(related, repository);
                }
            }
        }
        
        // 处理ManyToMany级联
        for (Field field : metadata.getManyToManyFields()) {
            ManyToMany annotation = field.getAnnotation(ManyToMany.class);
            if (hasCascade(annotation.cascade(), CascadeType.ALL, CascadeType.MERGE)) {
                Collection<?> related = getCollectionFieldValue(entity, field);
                if (related != null) {
                    for (Object item : related) {
                        updateRelatedEntity(item, repository);
                    }
                }
            }
        }
    }
    
    // ========== 私有辅助方法 ==========
    
    /**
     * 检查是否包含指定的级联类型
     */
    private boolean hasCascade(@NotNull CascadeType[] cascadeTypes, @NotNull CascadeType... types) {
        for (CascadeType cascadeType : cascadeTypes) {
            if (cascadeType == CascadeType.ALL) {
                return true;
            }
            for (CascadeType type : types) {
                if (cascadeType == type) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * 保存关联实体
     */
    @SuppressWarnings("unchecked")
    private void saveRelatedEntity(@NotNull Object related, @NotNull BaseRepository<?, ?> repository) {
        try {
            BaseRepository<Object, Object> repo = (BaseRepository<Object, Object>) repository;
            repo.save(related);
        } catch (Exception e) {
            log.error("Failed to save related entity", e);
        }
    }
    
    /**
     * 删除关联实体
     */
    @SuppressWarnings("unchecked")
    private void deleteRelatedEntity(@NotNull Object related, @NotNull BaseRepository<?, ?> repository) {
        try {
            BaseRepository<Object, Object> repo = (BaseRepository<Object, Object>) repository;
            repo.delete(related);
        } catch (Exception e) {
            log.error("Failed to delete related entity", e);
        }
    }
    
    /**
     * 更新关联实体
     */
    @SuppressWarnings("unchecked")
    private void updateRelatedEntity(@NotNull Object related, @NotNull BaseRepository<?, ?> repository) {
        try {
            BaseRepository<Object, Object> repo = (BaseRepository<Object, Object>) repository;
            repo.update(related);
        } catch (Exception e) {
            log.error("Failed to update related entity", e);
        }
    }
    
    /**
     * 获取字段值
     */
    @Nullable
    private Object getFieldValue(@NotNull Object entity, @NotNull Field field) {
        try {
            field.setAccessible(true);
            return field.get(entity);
        } catch (Exception e) {
            log.error("Failed to get field value: {}", field.getName(), e);
            return null;
        }
    }
    
    /**
     * 获取集合字段值
     */
    @Nullable
    @SuppressWarnings("unchecked")
    private Collection<?> getCollectionFieldValue(@NotNull Object entity, @NotNull Field field) {
        Object value = getFieldValue(entity, field);
        if (value instanceof Collection) {
            return (Collection<?>) value;
        }
        return null;
    }
}
