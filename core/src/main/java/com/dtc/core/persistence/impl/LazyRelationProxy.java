package com.dtc.core.persistence.impl;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import com.dtc.core.persistence.LazyLoader;
import com.dtc.core.persistence.RelationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;

/**
 * 懒加载关联代理
 * 提供延迟加载关联实体
 * 
 * @author Network Service Template
 */
public class LazyRelationProxy {
    
    private static final Logger log = LoggerFactory.getLogger(LazyRelationProxy.class);
    
    /**
     * 创建懒加载的关联实体对象代理
     * 
     * @param entity 源实体
     * @param field 关联字段
     * @param relationHandler 关联关系处理器
     * @param targetType 目标类型
     * @return 代理对象
     */
    @SuppressWarnings("unchecked")
    @NotNull
    public static <T> T createProxy(@NotNull Object entity, @NotNull Field field,
                                     @NotNull RelationHandler relationHandler,
                                     @NotNull Class<T> targetType) {
        
        LazyLoader<T> loader = LazyLoader.of(() -> {
            try {
                if (field.isAnnotationPresent(com.dtc.annotations.persistence.OneToOne.class)) {
                    com.dtc.annotations.persistence.OneToOne annotation = 
                        field.getAnnotation(com.dtc.annotations.persistence.OneToOne.class);
                    return (T) relationHandler.handleOneToOne(entity, field, annotation);
                } else if (field.isAnnotationPresent(com.dtc.annotations.persistence.ManyToOne.class)) {
                    com.dtc.annotations.persistence.ManyToOne annotation = 
                        field.getAnnotation(com.dtc.annotations.persistence.ManyToOne.class);
                    return (T) relationHandler.handleManyToOne(entity, field, annotation);
                }
            } catch (Exception e) {
                log.error("Failed to load lazy relation", e);
            }
            return null;
        });
        
        return (T) Proxy.newProxyInstance(
            targetType.getClassLoader(),
            new Class[]{targetType},
            new LazyInvocationHandler<>(loader)
        );
    }
    
    /**
     * 创建懒加载的集合代理
     */
    @SuppressWarnings("unchecked")
    @NotNull
    public static <T extends Collection<?>> T createCollectionProxy(@NotNull Object entity, @NotNull Field field,
                                                                     @NotNull RelationHandler relationHandler,
                                                                     @NotNull Class<T> collectionType) {
        
        LazyLoader<Collection<?>> loader = LazyLoader.of(() -> {
            try {
                if (field.isAnnotationPresent(com.dtc.annotations.persistence.OneToMany.class)) {
                    com.dtc.annotations.persistence.OneToMany annotation = 
                        field.getAnnotation(com.dtc.annotations.persistence.OneToMany.class);
                    return relationHandler.handleOneToMany(entity, field, annotation);
                } else if (field.isAnnotationPresent(com.dtc.annotations.persistence.ManyToMany.class)) {
                    com.dtc.annotations.persistence.ManyToMany annotation = 
                        field.getAnnotation(com.dtc.annotations.persistence.ManyToMany.class);
                    return relationHandler.handleManyToMany(entity, field, annotation);
                }
            } catch (Exception e) {
                log.error("Failed to load lazy collection relation", e);
            }
            return null;
        });
        
        return (T) Proxy.newProxyInstance(
            collectionType.getClassLoader(),
            new Class[]{collectionType},
            new LazyCollectionInvocationHandler(loader)
        );
    }
    
    /**
     * 懒加载调用处理器
     */
    private static class LazyInvocationHandler<T> implements InvocationHandler {
        private final LazyLoader<T> loader;
        
        public LazyInvocationHandler(@NotNull LazyLoader<T> loader) {
            this.loader = loader;
        }
        
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            T target = loader.get();
            if (target == null) {
                // 如果加载失败，返回默认值
                Class<?> returnType = method.getReturnType();
                if (returnType.isPrimitive()) {
                    if (returnType == boolean.class) return false;
                    if (returnType == byte.class) return (byte) 0;
                    if (returnType == char.class) return '\0';
                    if (returnType == short.class) return (short) 0;
                    if (returnType == int.class) return 0;
                    if (returnType == long.class) return 0L;
                    if (returnType == float.class) return 0.0f;
                    if (returnType == double.class) return 0.0;
                }
                return null;
            }
            return method.invoke(target, args);
        }
    }
    
    /**
     * 懒加载集合调用处理器
     */
    private static class LazyCollectionInvocationHandler implements InvocationHandler {
        private final LazyLoader<Collection<?>> loader;
        
        public LazyCollectionInvocationHandler(@NotNull LazyLoader<Collection<?>> loader) {
            this.loader = loader;
        }
        
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Collection<?> target = loader.get();
            if (target == null) {
                target = java.util.Collections.emptyList();
            }
            return method.invoke(target, args);
        }
    }
}
