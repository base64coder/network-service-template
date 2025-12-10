package com.dtc.core.serialization;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import com.google.protobuf.Message;
import com.google.protobuf.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Singleton;
import java.lang.ref.WeakReference;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 序列化缓存管理器
 * 提供序列化相关的缓存管理和统计功能
 * 
 * @author Network Service Template
 */
@Singleton
public class SerializationCacheManager {

    private static final Logger log = LoggerFactory.getLogger(SerializationCacheManager.class);

    // 缓存配置
    private static final int DEFAULT_MAX_CACHE_SIZE = 1000;
    private static final long DEFAULT_CACHE_TTL_MS = 300000; // 5分钟
    private static final double DEFAULT_CACHE_LOAD_FACTOR = 0.75f;

    // 缓存存储
    private final ConcurrentHashMap<Class<?>, CacheEntry<Parser<?>>> parserCache;
    private final ConcurrentHashMap<Class<?>, CacheEntry<Object>> builderCache;
    private final ConcurrentHashMap<String, CacheEntry<byte[]>> serializedDataCache;

    // 缓存统计
    private final AtomicLong cacheHits = new AtomicLong(0);
    private final AtomicLong cacheMisses = new AtomicLong(0);
    private final AtomicLong cacheEvictions = new AtomicLong(0);

    // 读写锁
    private final ReadWriteLock cacheLock = new ReentrantReadWriteLock();

    // 配置参数
    private final int maxCacheSize;
    private final long cacheTtlMs;
    private final boolean enableWeakReferences;
    private final boolean enableLruEviction;

    // 清理线程
    private final Thread cacheCleanupThread;
    private volatile boolean running = false;

    public SerializationCacheManager() {
        this(DEFAULT_MAX_CACHE_SIZE, DEFAULT_CACHE_TTL_MS, true, true);
    }

    public SerializationCacheManager(int maxCacheSize, long cacheTtlMs, boolean enableWeakReferences,
            boolean enableLruEviction) {
        this.maxCacheSize = maxCacheSize;
        this.cacheTtlMs = cacheTtlMs;
        this.enableWeakReferences = enableWeakReferences;
        this.enableLruEviction = enableLruEviction;

        this.parserCache = new ConcurrentHashMap<>();
        this.builderCache = new ConcurrentHashMap<>();
        this.serializedDataCache = new ConcurrentHashMap<>();

        // 启动缓存清理线程
        this.cacheCleanupThread = new Thread(this::cleanupExpiredEntries, "CacheCleanup");
        this.cacheCleanupThread.setDaemon(true);
        this.cacheCleanupThread.start();

        log.info("Serialization cache manager initialized with maxSize={}, ttl={}ms", maxCacheSize, cacheTtlMs);
    }

    /**
     * 获取缓存的Parser
     */
    @SuppressWarnings("unchecked")
    @Nullable
    public <T extends Message> Parser<T> getCachedParser(@NotNull Class<T> messageClass) {
        CacheEntry<Parser<?>> entry = parserCache.get(messageClass);

        if (entry != null && !entry.isExpired()) {
            entry.updateAccessTime();
            cacheHits.incrementAndGet();
            return (Parser<T>) entry.getValue();
        }

        cacheMisses.incrementAndGet();
        return null;
    }

    /**
     * 缓存 Parser
     */
    public <T extends Message> void cacheParser(@NotNull Class<T> messageClass, @NotNull Parser<T> parser) {
        if (parserCache.size() >= maxCacheSize) {
            evictLeastRecentlyUsed();
        }

        CacheEntry<Parser<?>> entry = new CacheEntry<>(parser, cacheTtlMs);
        parserCache.put(messageClass, entry);
    }

    /**
     * 获取缓存的Builder
     */
    @Nullable
    public Object getCachedBuilder(@NotNull Class<? extends Message> messageClass) {
        CacheEntry<Object> entry = builderCache.get(messageClass);

        if (entry != null && !entry.isExpired()) {
            entry.updateAccessTime();
            cacheHits.incrementAndGet();
            return entry.getValue();
        }

        cacheMisses.incrementAndGet();
        return null;
    }

    /**
     * 缓存 Builder
     */
    public void cacheBuilder(@NotNull Class<? extends Message> messageClass, @NotNull Object builder) {
        if (builderCache.size() >= maxCacheSize) {
            evictLeastRecentlyUsed();
        }

        CacheEntry<Object> entry = new CacheEntry<>(builder, cacheTtlMs);
        builderCache.put(messageClass, entry);
    }

    /**
     * 获取缓存的序列化数据
     */
    @Nullable
    public byte[] getCachedSerializedData(@NotNull String key) {
        CacheEntry<byte[]> entry = serializedDataCache.get(key);

        if (entry != null && !entry.isExpired()) {
            entry.updateAccessTime();
            cacheHits.incrementAndGet();
            return entry.getValue();
        }

        cacheMisses.incrementAndGet();
        return null;
    }

    /**
     * 缓存序列化数据
     */
    public void cacheSerializedData(@NotNull String key, @NotNull byte[] data) {
        if (serializedDataCache.size() >= maxCacheSize) {
            evictLeastRecentlyUsed();
        }

        CacheEntry<byte[]> entry = new CacheEntry<>(data, cacheTtlMs);
        serializedDataCache.put(key, entry);
    }

    /**
     * 清理过期条目
     */
    private void cleanupExpiredEntries() {
        running = true;

        while (running) {
            try {
                Thread.sleep(cacheTtlMs / 2); // 每TTL/2时间清理一次
                long currentTime = System.currentTimeMillis();
                int cleanedCount = 0;

                // 清理 Parser 缓存
                cleanedCount += cleanupCache(parserCache, currentTime);

                // 清理 Builder 缓存
                cleanedCount += cleanupCache(builderCache, currentTime);

                // 清理序列化数据缓存
                cleanedCount += cleanupCache(serializedDataCache, currentTime);

                if (cleanedCount > 0) {
                    log.debug("Cleaned up {} expired cache entries", cleanedCount);
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("Error during cache cleanup", e);
            }
        }
    }

    /**
     * 清理指定缓存中的过期条目
     */
    private <T> int cleanupCache(ConcurrentHashMap<?, CacheEntry<T>> cache, long currentTime) {
        AtomicInteger cleanedCount = new AtomicInteger();

        cache.entrySet().removeIf(entry -> {
            CacheEntry<T> cacheEntry = entry.getValue();
            if (cacheEntry.isExpired(currentTime)) {
                cleanedCount.getAndIncrement();
                return true;
            }
            return false;
        });

        return cleanedCount.get();
    }

    /**
     * LRU 淘汰策略
     */
    private void evictLeastRecentlyUsed() {
        if (!enableLruEviction) {
            return;
        }

        // 查找最久未访问的条目
        long oldestAccessTime = Long.MAX_VALUE;
        Object oldestKey = null;

        // 检查所有缓存
        for (var entry : parserCache.entrySet()) {
            if (entry.getValue().getLastAccessTime() < oldestAccessTime) {
                oldestAccessTime = entry.getValue().getLastAccessTime();
                oldestKey = entry.getKey();
            }
        }

        for (var entry : builderCache.entrySet()) {
            if (entry.getValue().getLastAccessTime() < oldestAccessTime) {
                oldestAccessTime = entry.getValue().getLastAccessTime();
                oldestKey = entry.getKey();
            }
        }

        for (var entry : serializedDataCache.entrySet()) {
            if (entry.getValue().getLastAccessTime() < oldestAccessTime) {
                oldestAccessTime = entry.getValue().getLastAccessTime();
                oldestKey = entry.getKey();
            }
        }

        // 移除最久未访问的条目
        if (oldestKey != null) {
            parserCache.remove(oldestKey);
            builderCache.remove(oldestKey);
            serializedDataCache.remove(oldestKey);
            cacheEvictions.incrementAndGet();
        }
    }

    /**
     * 获取缓存统计信息
     */
    @NotNull
    public CacheStats getCacheStats() {
        long totalHits = cacheHits.get();
        long totalMisses = cacheMisses.get();
        long totalRequests = totalHits + totalMisses;

        return new CacheStats(totalHits, totalMisses, totalRequests, parserCache.size(), builderCache.size(),
                serializedDataCache.size(), cacheEvictions.get(),
                totalRequests > 0 ? (double) totalHits / totalRequests : 0.0);
    }

    /**
     * 清除所有缓存
     */
    public void clearAllCaches() {
        cacheLock.writeLock().lock();
        try {
            parserCache.clear();
            builderCache.clear();
            serializedDataCache.clear();

            cacheHits.set(0);
            cacheMisses.set(0);
            cacheEvictions.set(0);

            log.info("All caches cleared");
        } finally {
            cacheLock.writeLock().unlock();
        }
    }

    /**
     * 停止缓存管理器
     */
    public void shutdown() {
        running = false;
        cacheCleanupThread.interrupt();
        clearAllCaches();
        log.info("Serialization cache manager shutdown");
    }

    /**
     * 缓存条目
     */
    private static class CacheEntry<T> {
        private final T value;
        private final long createTime;
        private final long ttlMs;
        private volatile long lastAccessTime;
        private final WeakReference<T> weakReference;

        public CacheEntry(T value, long ttlMs) {
            this.value = value;
            this.createTime = System.currentTimeMillis();
            this.ttlMs = ttlMs;
            this.lastAccessTime = createTime;
            this.weakReference = new WeakReference<>(value);
        }

        public T getValue() {
            return value;
        }

        public long getLastAccessTime() {
            return lastAccessTime;
        }

        public void updateAccessTime() {
            this.lastAccessTime = System.currentTimeMillis();
        }

        public boolean isExpired() {
            return isExpired(System.currentTimeMillis());
        }

        public boolean isExpired(long currentTime) {
            return currentTime - createTime > ttlMs;
        }

        public boolean isWeakReferenceCleared() {
            return weakReference.get() == null;
        }
    }

    /**
     * 缓存统计信息
     */
    public static class CacheStats {
        private final long hits;
        private final long misses;
        private final long totalRequests;
        private final int parserCacheSize;
        private final int builderCacheSize;
        private final int serializedDataCacheSize;
        private final long evictions;
        private final double hitRate;

        public CacheStats(long hits, long misses, long totalRequests, int parserCacheSize, int builderCacheSize,
                int serializedDataCacheSize, long evictions, double hitRate) {
            this.hits = hits;
            this.misses = misses;
            this.totalRequests = totalRequests;
            this.parserCacheSize = parserCacheSize;
            this.builderCacheSize = builderCacheSize;
            this.serializedDataCacheSize = serializedDataCacheSize;
            this.evictions = evictions;
            this.hitRate = hitRate;
        }

        public long getHits() {
            return hits;
        }

        public long getMisses() {
            return misses;
        }

        public long getTotalRequests() {
            return totalRequests;
        }

        public int getParserCacheSize() {
            return parserCacheSize;
        }

        public int getBuilderCacheSize() {
            return builderCacheSize;
        }

        public int getSerializedDataCacheSize() {
            return serializedDataCacheSize;
        }

        public long getEvictions() {
            return evictions;
        }

        public double getHitRate() {
            return hitRate;
        }

        @Override
        public String toString() {
            return String.format(
                    "CacheStats{hits=%d, misses=%d, hitRate=%.2f%%, "
                            + "parserCache=%d, builderCache=%d, dataCache=%d, evictions=%d}",
                    hits, misses, hitRate * 100, parserCacheSize, builderCacheSize, serializedDataCacheSize, evictions);
        }
    }
}
