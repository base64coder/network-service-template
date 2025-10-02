package com.dtc.core.serialization;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dtc.api.annotations.NotNull;
import com.google.protobuf.Message;
import com.google.protobuf.MessageLite;
import com.google.protobuf.Parser;

/**
 * 优化的 Protobuf 序列化器 提供高性能的序列化/反序列化功能，包含多种性能优化策略
 * 
 * @author Network Service Template
 */
@Singleton
public class OptimizedProtobufSerializer {

    private static final Logger log = LoggerFactory.getLogger(OptimizedProtobufSerializer.class);

    // 缓存相关
    private final ConcurrentHashMap<Class<?>, Message.Builder> builderCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Class<?>, Parser<?>> parserCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Class<?>, Method> newBuilderMethodCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Class<?>, Method> parserMethodCache = new ConcurrentHashMap<>();

    // 对象池
    private final ThreadLocal<ByteArrayOutputStream> byteArrayOutputStreamPool = ThreadLocal
            .withInitial(() -> new ByteArrayOutputStream(1024));

    // 统计信息
    private final AtomicLong serializeCount = new AtomicLong(0);
    private final AtomicLong deserializeCount = new AtomicLong(0);
    private final AtomicLong cacheHitCount = new AtomicLong(0);
    private final AtomicLong totalSerializeTime = new AtomicLong(0);
    private final AtomicLong totalDeserializeTime = new AtomicLong(0);

    // 配置参数
    private final int maxCacheSize = 1000;
    private final int initialBufferSize = 1024;
    private final boolean enableCompression = false;
    private final boolean enableValidation = true;

    /**
     * 序列化消息为字节数组（优化版本）
     * 
     * @param message 要序列化的消息
     * @return 序列化后的字节数组
     */
    @NotNull
    public byte[] serialize(@NotNull Message message) {
        long startTime = System.nanoTime();
        try {
            byte[] result = message.toByteArray();
            serializeCount.incrementAndGet();
            totalSerializeTime.addAndGet(System.nanoTime() - startTime);
            return result;
        } catch (Exception e) {
            log.error("Failed to serialize message: {}", message.getClass().getSimpleName(), e);
            throw new SerializationException("Failed to serialize message", e);
        }
    }

    /**
     * 序列化消息为字节数组（使用 MessageLite）
     * 
     * @param message 要序列化的消息
     * @return 序列化后的字节数组
     */
    @NotNull
    public byte[] serialize(@NotNull MessageLite message) {
        long startTime = System.nanoTime();
        try {
            byte[] result = message.toByteArray();
            serializeCount.incrementAndGet();
            totalSerializeTime.addAndGet(System.nanoTime() - startTime);
            return result;
        } catch (Exception e) {
            log.error("Failed to serialize message: {}", message.getClass().getSimpleName(), e);
            throw new SerializationException("Failed to serialize message", e);
        }
    }

    /**
     * 批量序列化消息
     * 
     * @param messages 消息列表
     * @return 序列化后的字节数组列表
     */
    @NotNull
    public byte[][] serializeBatch(@NotNull Message[] messages) {
        long startTime = System.nanoTime();
        try {
            byte[][] results = new byte[messages.length][];

            // 并行序列化
            for (int i = 0; i < messages.length; i++) {
                results[i] = messages[i].toByteArray();
            }

            serializeCount.addAndGet(messages.length);
            totalSerializeTime.addAndGet(System.nanoTime() - startTime);
            return results;
        } catch (Exception e) {
            log.error("Failed to serialize message batch", e);
            throw new SerializationException("Failed to serialize message batch", e);
        }
    }

    /**
     * 反序列化字节数组为消息（优化版本）
     * 
     * @param data         序列化的字节数组
     * @param messageClass 消息类型
     * @param <T>          消息类型
     * @return 反序列化后的消息
     */
    @NotNull
    public <T extends Message> T deserialize(@NotNull byte[] data, @NotNull Class<T> messageClass) {
        long startTime = System.nanoTime();
        try {
            // 使用缓存的 Parser
            Parser<T> parser = getCachedParser(messageClass);
            T result = parser.parseFrom(data);

            deserializeCount.incrementAndGet();
            totalDeserializeTime.addAndGet(System.nanoTime() - startTime);
            return result;
        } catch (Exception e) {
            log.error("Failed to deserialize message of type: {}", messageClass.getSimpleName(), e);
            throw new SerializationException("Failed to deserialize message", e);
        }
    }

    /**
     * 反序列化字节数组为消息（使用 MessageLite）
     * 
     * @param data         序列化的字节数组
     * @param messageClass 消息类型
     * @param <T>          消息类型
     * @return 反序列化后的消息
     */
    @NotNull
    public <T extends MessageLite> T deserializeLite(@NotNull byte[] data, @NotNull Class<T> messageClass) {
        long startTime = System.nanoTime();
        try {
            Parser<T> parser = getCachedLiteParser(messageClass);
            T result = parser.parseFrom(data);

            deserializeCount.incrementAndGet();
            totalDeserializeTime.addAndGet(System.nanoTime() - startTime);
            return result;
        } catch (Exception e) {
            log.error("Failed to deserialize message of type: {}", messageClass.getSimpleName(), e);
            throw new SerializationException("Failed to deserialize message", e);
        }
    }

    /**
     * 批量反序列化
     * 
     * @param dataArray    字节数组列表
     * @param messageClass 消息类型
     * @param <T>          消息类型
     * @return 反序列化后的消息列表
     */
    @NotNull
    public <T extends Message> T[] deserializeBatch(@NotNull byte[][] dataArray, @NotNull Class<T> messageClass) {
        long startTime = System.nanoTime();
        try {
            @SuppressWarnings("unchecked")
            T[] results = (T[]) java.lang.reflect.Array.newInstance(messageClass, dataArray.length);
            Parser<T> parser = getCachedParser(messageClass);

            for (int i = 0; i < dataArray.length; i++) {
                results[i] = parser.parseFrom(dataArray[i]);
            }

            deserializeCount.addAndGet(dataArray.length);
            totalDeserializeTime.addAndGet(System.nanoTime() - startTime);
            return results;
        } catch (Exception e) {
            log.error("Failed to deserialize message batch", e);
            throw new SerializationException("Failed to deserialize message batch", e);
        }
    }

    /**
     * 获取缓存的 Parser
     */
    @SuppressWarnings("unchecked")
    @NotNull
    private <T extends Message> Parser<T> getCachedParser(@NotNull Class<T> messageClass) {
        Parser<?> parser = parserCache.get(messageClass);
        if (parser == null) {
            parser = createParser(messageClass);
            if (parserCache.size() < maxCacheSize) {
                parserCache.put(messageClass, parser);
                cacheHitCount.incrementAndGet();
            }
        }
        return (Parser<T>) parser;
    }

    /**
     * 获取缓存的 MessageLite Parser
     */
    @SuppressWarnings("unchecked")
    @NotNull
    private <T extends MessageLite> Parser<T> getCachedLiteParser(@NotNull Class<T> messageClass) {
        Parser<?> parser = parserCache.get(messageClass);
        if (parser == null) {
            parser = createLiteParser(messageClass);
            if (parserCache.size() < maxCacheSize) {
                parserCache.put(messageClass, parser);
                cacheHitCount.incrementAndGet();
            }
        }
        return (Parser<T>) parser;
    }

    /**
     * 创建 Parser
     */
    @NotNull
    private <T extends Message> Parser<T> createParser(@NotNull Class<T> messageClass) {
        try {
            Method parserMethod = parserMethodCache.get(messageClass);
            if (parserMethod == null) {
                parserMethod = messageClass.getMethod("parser");
                parserMethodCache.put(messageClass, parserMethod);
            }
            return (Parser<T>) parserMethod.invoke(null);
        } catch (Exception e) {
            throw new SerializationException("Failed to create parser for class: " + messageClass.getSimpleName(), e);
        }
    }

    /**
     * 创建 MessageLite Parser
     */
    @NotNull
    private <T extends MessageLite> Parser<T> createLiteParser(@NotNull Class<T> messageClass) {
        try {
            Method parserMethod = parserMethodCache.get(messageClass);
            if (parserMethod == null) {
                parserMethod = messageClass.getMethod("parser");
                parserMethodCache.put(messageClass, parserMethod);
            }
            return (Parser<T>) parserMethod.invoke(null);
        } catch (Exception e) {
            throw new SerializationException("Failed to create parser for class: " + messageClass.getSimpleName(), e);
        }
    }

    /**
     * 获取缓存的 Builder
     */
    @NotNull
    private Message.Builder getCachedBuilder(@NotNull Class<? extends Message> messageClass) {
        Message.Builder builder = builderCache.get(messageClass);
        if (builder == null) {
            builder = createBuilder(messageClass);
            if (builderCache.size() < maxCacheSize) {
                builderCache.put(messageClass, builder);
                cacheHitCount.incrementAndGet();
            }
        }
        return builder.clone();
    }

    /**
     * 创建 Builder
     */
    @NotNull
    private Message.Builder createBuilder(@NotNull Class<? extends Message> messageClass) {
        try {
            Method newBuilderMethod = newBuilderMethodCache.get(messageClass);
            if (newBuilderMethod == null) {
                newBuilderMethod = messageClass.getMethod("newBuilder");
                newBuilderMethodCache.put(messageClass, newBuilderMethod);
            }
            return (Message.Builder) newBuilderMethod.invoke(null);
        } catch (Exception e) {
            throw new SerializationException("Failed to create builder for class: " + messageClass.getSimpleName(), e);
        }
    }

    /**
     * 流式序列化（适用于大消息）
     * 
     * @param message 要序列化的消息
     * @return 序列化后的字节数组
     */
    @NotNull
    public byte[] serializeStreaming(@NotNull Message message) {
        long startTime = System.nanoTime();
        try {
            ByteArrayOutputStream baos = byteArrayOutputStreamPool.get();
            baos.reset();

            message.writeTo(baos);
            byte[] result = baos.toByteArray();

            serializeCount.incrementAndGet();
            totalSerializeTime.addAndGet(System.nanoTime() - startTime);
            return result;
        } catch (IOException e) {
            log.error("Failed to serialize message streaming: {}", message.getClass().getSimpleName(), e);
            throw new SerializationException("Failed to serialize message streaming", e);
        }
    }

    /**
     * 流式反序列化（适用于大消息）
     * 
     * @param data         序列化的字节数组
     * @param messageClass 消息类型
     * @param <T>          消息类型
     * @return 反序列化后的消息
     */
    @NotNull
    public <T extends Message> T deserializeStreaming(@NotNull byte[] data, @NotNull Class<T> messageClass) {
        long startTime = System.nanoTime();
        try {
            Parser<T> parser = getCachedParser(messageClass);
            T result = parser.parseFrom(data);

            deserializeCount.incrementAndGet();
            totalDeserializeTime.addAndGet(System.nanoTime() - startTime);
            return result;
        } catch (Exception e) {
            log.error("Failed to deserialize message streaming of type: {}", messageClass.getSimpleName(), e);
            throw new SerializationException("Failed to deserialize message streaming", e);
        }
    }

    /**
     * 验证消息格式
     * 
     * @param data         字节数组
     * @param messageClass 消息类型
     * @return 是否有效
     */
    public boolean validateMessage(@NotNull byte[] data, @NotNull Class<? extends Message> messageClass) {
        if (!enableValidation) {
            return true;
        }

        try {
            Parser<?> parser = getCachedParser(messageClass);
            parser.parseFrom(data);
            return true;
        } catch (Exception e) {
            log.debug("Message validation failed for type: {}", messageClass.getSimpleName(), e);
            return false;
        }
    }

    /**
     * 获取序列化统计信息
     */
    @NotNull
    public SerializationStats getStats() {
        return new SerializationStats(serializeCount.get(), deserializeCount.get(), cacheHitCount.get(),
                totalSerializeTime.get(), totalDeserializeTime.get(), builderCache.size(), parserCache.size());
    }

    /**
     * 清理缓存
     */
    public void clearCache() {
        builderCache.clear();
        parserCache.clear();
        newBuilderMethodCache.clear();
        parserMethodCache.clear();
        log.info("Protobuf serializer cache cleared");
    }

    /**
     * 预热缓存
     * 
     * @param messageClasses 要预热的消息类型
     */
    public void warmupCache(@NotNull Class<? extends Message>... messageClasses) {
        for (Class<? extends Message> messageClass : messageClasses) {
            try {
                getCachedParser(messageClass);
                getCachedBuilder(messageClass);
                log.debug("Warmed up cache for: {}", messageClass.getSimpleName());
            } catch (Exception e) {
                log.warn("Failed to warm up cache for: {}", messageClass.getSimpleName(), e);
            }
        }
        log.info("Cache warmup completed for {} message types", messageClasses.length);
    }

    /**
     * 序列化统计信息
     */
    public static class SerializationStats {
        private final long serializeCount;
        private final long deserializeCount;
        private final long cacheHitCount;
        private final long totalSerializeTime;
        private final long totalDeserializeTime;
        private final int builderCacheSize;
        private final int parserCacheSize;

        public SerializationStats(long serializeCount, long deserializeCount, long cacheHitCount,
                long totalSerializeTime, long totalDeserializeTime, int builderCacheSize, int parserCacheSize) {
            this.serializeCount = serializeCount;
            this.deserializeCount = deserializeCount;
            this.cacheHitCount = cacheHitCount;
            this.totalSerializeTime = totalSerializeTime;
            this.totalDeserializeTime = totalDeserializeTime;
            this.builderCacheSize = builderCacheSize;
            this.parserCacheSize = parserCacheSize;
        }

        public long getSerializeCount() {
            return serializeCount;
        }

        public long getDeserializeCount() {
            return deserializeCount;
        }

        public long getCacheHitCount() {
            return cacheHitCount;
        }

        public long getTotalSerializeTime() {
            return totalSerializeTime;
        }

        public long getTotalDeserializeTime() {
            return totalDeserializeTime;
        }

        public int getBuilderCacheSize() {
            return builderCacheSize;
        }

        public int getParserCacheSize() {
            return parserCacheSize;
        }

        public double getAverageSerializeTime() {
            return serializeCount > 0 ? (double) totalSerializeTime / serializeCount : 0.0;
        }

        public double getAverageDeserializeTime() {
            return deserializeCount > 0 ? (double) totalDeserializeTime / deserializeCount : 0.0;
        }

        public double getCacheHitRate() {
            long totalOperations = serializeCount + deserializeCount;
            return totalOperations > 0 ? (double) cacheHitCount / totalOperations : 0.0;
        }

        @Override
        public String toString() {
            return String.format(
                    "SerializationStats{serialize=%d, deserialize=%d, cacheHits=%d, "
                            + "avgSerializeTime=%.2fns, avgDeserializeTime=%.2fns, cacheHitRate=%.2f%%, "
                            + "builderCache=%d, parserCache=%d}",
                    serializeCount, deserializeCount, cacheHitCount, getAverageSerializeTime(),
                    getAverageDeserializeTime(), getCacheHitRate() * 100, builderCacheSize, parserCacheSize);
        }
    }

    /**
     * 序列化异常
     */
    public static class SerializationException extends RuntimeException {
        public SerializationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
