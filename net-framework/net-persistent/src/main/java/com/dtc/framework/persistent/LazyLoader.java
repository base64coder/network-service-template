package com.dtc.framework.persistent;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.function.Supplier;

/**
 * 懒加载加载器接口
 * 提供延迟加载功能，支持Lazy Loading模式
 * 
 * @author Network Service Template
 */
public interface LazyLoader<T> {
    
    /**
     * 获取值，如果尚未加载则触发加载
     * 
     * @return 加载的值
     */
    @Nullable
    T get();
    
    /**
     * 是否已加载
     * 
     * @return 是否已加载
     */
    boolean isLoaded();
    
    /**
     * 强制加载
     */
    void load();
    
    /**
     * 创建懒加载器
     */
    @NotNull
    static <T> LazyLoader<T> of(@NotNull Supplier<T> supplier) {
        return new LazyLoaderImpl<>(supplier);
    }
    
    /**
     * 懒加载器实现
     */
    class LazyLoaderImpl<T> implements LazyLoader<T> {
        private final Supplier<T> supplier;
        private volatile T value;
        private volatile boolean loaded = false;
        private final Object lock = new Object();
        
        public LazyLoaderImpl(@NotNull Supplier<T> supplier) {
            this.supplier = supplier;
        }
        
        @Override
        @Nullable
        public T get() {
            if (!loaded) {
                synchronized (lock) {
                    if (!loaded) {
                        value = supplier.get();
                        loaded = true;
                    }
                }
            }
            return value;
        }
        
        @Override
        public boolean isLoaded() {
            return loaded;
        }
        
        @Override
        public void load() {
            get();
        }
    }
}
