package com.dtc.core.persistence;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Singleton;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据存储
 * 简单的内存数据存储实现
 * 
 * @author Network Service Template
 */
@Singleton
public class DataStore {

    private static final Logger log = LoggerFactory.getLogger(DataStore.class);

    private final @NotNull Map<String, Object> data = new ConcurrentHashMap<>();
    private volatile boolean initialized = false;

    /**
     * 初始化数据存储
     * 
     * @throws Exception 初始化异常
     */
    public void initialize() throws Exception {
        if (initialized) {
            return;
        }

        log.info("Initializing data store...");

        // 可以通过路由管理器实现数据存储的初始化逻辑
        // 例如：连接数据库或初始化缓存等
        initialized = true;
        log.info("Data store initialized successfully");
    }

    /**
     * 关闭数据存储
     * 
     * @throws Exception 关闭异常
     */
    public void shutdown() throws Exception {
        if (!initialized) {
            return;
        }

        log.info("Shutting down data store...");

        // 可以通过路由管理器实现数据存储的关闭逻辑
        // 例如：关闭数据库连接或清理缓存等

        data.clear();
        initialized = false;
        log.info("Data store shut down successfully");
    }

    /**
     * 存储数据
     * 
     * @param key   键
     * @param value 值
     */
    public void put(@NotNull String key, @NotNull Object value) {
        data.put(key, value);
        log.debug("Stored data: {} = {}", key, value.getClass().getSimpleName());
    }

    /**
     * 获取数据
     * 
     * @param key 键
     * @return 值
     */
    @Nullable
    public Object get(@NotNull String key) {
        return data.get(key);
    }

    /**
     * 删除数据
     * 
     * @param key 键
     * @return 被删除的值
     */
    @Nullable
    public Object remove(@NotNull String key) {
        return data.remove(key);
    }

    /**
     * 检查键是否存在
     * 
     * @param key 键
     * @return 是否存在
     */
    public boolean containsKey(@NotNull String key) {
        return data.containsKey(key);
    }

    /**
     * 获取所有数据
     * 
     * @return 数据映射
     */
    @NotNull
    public Map<String, Object> getAllData() {
        return Map.copyOf(data);
    }

    /**
     * 是否已初始化
     * 
     * @return 是否已初始化
     */
    public boolean isInitialized() {
        return initialized;
    }
}
