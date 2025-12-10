package com.dtc.core.serialization;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;

import jakarta.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import com.google.protobuf.Message;
import com.google.protobuf.MessageLite;
import com.google.protobuf.Parser;
import com.google.protobuf.ExtensionRegistryLite;

/**
 * 优化的 Protobuf 序列化器
 * 提供高性能的序列化/反序列化功能，包括缓存和性能优化策略
 * 
 * @author Network Service Template
 */
@Singleton
public class OptimizedProtobufSerializer {

    private static final Logger log = LoggerFactory.getLogger(OptimizedProtobufSerializer.class);

    // 缓存存储
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
     * 序列化消息为字节数组（使用缓存优化）
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
     * 序列化消息为字节数组，支持MessageLite类型
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
     * @param messages 消息数组
     * @return 序列化后的字节数组数组
     */
    @NotNull
    public byte[][] serializeBatch(@NotNull Message[] messages) {
        long startTime = System.nanoTime();
        try {
            byte[][] results = new byte[messages.length][];

            // 逐个序列化
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
     * 反序列化字节数组为消息（使用缓存优化）
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
            // 使用缓存的Parser
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
     * 反序列化字节数组为消息，支持MessageLite类型
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
     * @param dataArray    字节数组数组
     * @param messageClass 消息类型
     * @param <T>          消息类型
     * @return 反序列化后的消息数组
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
     * 获取缓存的Parser
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
     * 获取缓存的MessageLite Parser
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
                // 使用 ByteBuddy 创建解析器
                parserMethod = createByteBuddyParserMethod(messageClass);
                if (parserMethod == null) {
                    // 如果没有 parser() 方法，则尝试使用默认的解析器方法
                    log.warn("No parser() method found for class: {}, using fallback parser",
                            messageClass.getSimpleName());
                    return createFallbackParser(messageClass);
                }
                parserMethodCache.put(messageClass, parserMethod);
            }
            return (Parser<T>) parserMethod.invoke(null);
        } catch (Exception e) {
            log.warn("Failed to create parser for class: {}, using fallback parser", messageClass.getSimpleName(), e);
            return createFallbackParser(messageClass);
        }
    }

    /**
     * 使用 ByteBuddy 创建解析器方法
     * 
     * @param messageClass 消息类型
     * @return 解析器方法
     */
    @Nullable
    private <T extends Message> Method createByteBuddyParserMethod(@NotNull Class<T> messageClass) {
        try {
            // 尝试获取已有的 parser 方法
            try {
                return messageClass.getMethod("parser");
            } catch (NoSuchMethodException e) {
                log.debug("No parser() method found for class: {}, creating ByteBuddy parser",
                        messageClass.getSimpleName());

                // 使用 ByteBuddy 创建新的解析器
                Class<?> parserClass = new ByteBuddy()
                        .subclass(Object.class)
                        .implement(Parser.class)
                        .method(ElementMatchers.any())
                        .intercept(MethodDelegation.to(new ByteBuddyParserInterceptor<T>(messageClass)))
                        .make()
                        .load(messageClass.getClassLoader())
                        .getLoaded();

                // 返回解析器实例的 parser 方法
                Object parserInstance = parserClass.getDeclaredConstructor().newInstance();
                return parserInstance.getClass().getMethod("parseFrom", byte[].class);

            }
        } catch (Exception e) {
            log.warn("Failed to create ByteBuddy parser for class: {}", messageClass.getSimpleName(), e);
            return null;
        }
    }

    /**
     * ByteBuddy 解析器拦截器
     */
    public static class ByteBuddyParserInterceptor<T extends Message> {
        private final Class<T> messageClass;
        private static final Logger log = LoggerFactory.getLogger(ByteBuddyParserInterceptor.class);

        public ByteBuddyParserInterceptor(Class<T> messageClass) {
            this.messageClass = messageClass;
        }

        @net.bytebuddy.implementation.bind.annotation.RuntimeType
        public Object intercept(@net.bytebuddy.implementation.bind.annotation.AllArguments Object[] args,
                @net.bytebuddy.implementation.bind.annotation.Origin java.lang.reflect.Method method) {
            try {
                String methodName = method.getName();
                if ("parseFrom".equals(methodName) && args.length == 1 && args[0] instanceof byte[]) {
                    return parseFromByteArray((byte[]) args[0]);
                } else if ("parseFrom".equals(methodName) && args.length == 2 && args[0] instanceof byte[]) {
                    return parseFromByteArrayWithRegistry((byte[]) args[0], (ExtensionRegistryLite) args[1]);
                }
                // 其他方法的默认实现
                return null;
            } catch (Exception e) {
                log.error("Error in ByteBuddy parser interceptor", e);
                throw new RuntimeException("Failed to parse message", e);
            }
        }

        @SuppressWarnings("unchecked")
        private T parseFromByteArray(byte[] data) throws Exception {
            Method parseFromMethod = messageClass.getMethod("parseFrom", byte[].class);
            return (T) parseFromMethod.invoke(null, data);
        }

        @SuppressWarnings("unchecked")
        private T parseFromByteArrayWithRegistry(byte[] data, ExtensionRegistryLite registry) throws Exception {
            Method parseFromMethod = messageClass.getMethod("parseFrom", byte[].class, ExtensionRegistryLite.class);
            return (T) parseFromMethod.invoke(null, data, registry);
        }
    }

    /**
     * 创建备用 Parser，如果没有找到标准的 parser() 方法
     */
    @SuppressWarnings("unchecked")
    @NotNull
    private <T extends Message> Parser<T> createFallbackParser(@NotNull Class<T> messageClass) {
        // 简单实现，直接抛出异常，实际应实现更复杂的逻辑
        throw new SerializationException("No parser() method found for class: " + messageClass.getSimpleName() +
                ". This class is not a proper Protobuf generated class.", new RuntimeException("Invalid class"));
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
     * 获取缓存的Builder
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
     * 流式序列化消息，使用对象池优化
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
     * 流式反序列化消息，使用对象池优化
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
     * 清除缓存
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
