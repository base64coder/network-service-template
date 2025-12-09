package com.dtc.core.persistence;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 实体元数据注册表
 * 缓存所有实体的元数据信息
 * 
 * @author Network Service Template
 */
public class EntityMetadataRegistry {
    
    private static final Map<Class<?>, EntityMetadata> metadataCache = new ConcurrentHashMap<>();
    
    /**
     * 注册实体元数据
     * 
     * @param entityClass 实体类型
     * @param metadata 元数据
     */
    public static void register(@NotNull Class<?> entityClass, @NotNull EntityMetadata metadata) {
        metadataCache.put(entityClass, metadata);
    }
    
    /**
     * 获取实体元数据
     * 
     * @param entityClass 实体类型
     * @return 元数据，如果不存在返回null
     */
    @Nullable
    public static EntityMetadata get(@NotNull Class<?> entityClass) {
        return metadataCache.get(entityClass);
    }
    
    /**
     * 获取或创建实体元数据
     * 如果不存在，则解析并注册
     * 
     * @param entityClass 实体类型
     * @return 元数据
     */
    @NotNull
    public static EntityMetadata getOrCreate(@NotNull Class<?> entityClass) {
        EntityMetadata metadata = metadataCache.get(entityClass);
        if (metadata == null) {
            metadata = EntityMetadataParser.parse(entityClass);
            metadataCache.put(entityClass, metadata);
        }
        return metadata;
    }
    
    /**
     * 清除所有元数据缓存
     */
    public static void clear() {
        metadataCache.clear();
    }
}
