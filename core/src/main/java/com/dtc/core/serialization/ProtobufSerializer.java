package com.dtc.core.serialization;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import com.google.protobuf.Message;
import com.google.protobuf.MessageLite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * Protobuf 序列化器 提供高效的二进制序列化/反序列化功能
 * 
 * @author Network Service Template
 */
@Singleton
public class ProtobufSerializer {

    private static final Logger log = LoggerFactory.getLogger(ProtobufSerializer.class);

    /**
     * 序列化消息为字节数组
     * 
     * @param message 要序列化的消息
     * @return 序列化后的字节数组
     */
    @NotNull
    public byte[] serialize(@NotNull Message message) {
        try {
            return message.toByteArray();
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
        try {
            return message.toByteArray();
        } catch (Exception e) {
            log.error("Failed to serialize message: {}", message.getClass().getSimpleName(), e);
            throw new SerializationException("Failed to serialize message", e);
        }
    }

    /**
     * 反序列化字节数组为消息
     * 
     * @param data         序列化的字节数组
     * @param messageClass 消息类型
     * @param <T>          消息类型
     * @return 反序列化后的消息
     */
    @NotNull
    public <T extends Message> T deserialize(@NotNull byte[] data, @NotNull Class<T> messageClass) {
        try {
            Message.Builder builder = getBuilderForClass(messageClass);
            return (T) builder.mergeFrom(data).build();
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
        try {
            MessageLite.Builder builder = getLiteBuilderForClass(messageClass);
            return (T) builder.mergeFrom(data).build();
        } catch (Exception e) {
            log.error("Failed to deserialize message of type: {}", messageClass.getSimpleName(), e);
            throw new SerializationException("Failed to deserialize message", e);
        }
    }

    /**
     * 获取消息类型的 Builder
     */
    @NotNull
    private Message.Builder getBuilderForClass(@NotNull Class<? extends Message> messageClass) {
        try {
            // 使用 ByteBuddy 创建 Builder
            return createByteBuddyBuilder(messageClass);
        } catch (Exception e) {
            throw new SerializationException("Failed to get builder for class: " + messageClass.getSimpleName(), e);
        }
    }

    /**
     * 使用 ByteBuddy 创建 Builder
     */
    @NotNull
    private Message.Builder createByteBuddyBuilder(@NotNull Class<? extends Message> messageClass) throws Exception {
        try {
            // 尝试直接调用 newBuilder 方法
            return (Message.Builder) messageClass.getMethod("newBuilder").invoke(null);
        } catch (Exception e) {
            log.debug("Failed to get builder using reflection, creating ByteBuddy builder for: {}",
                    messageClass.getSimpleName());

            // 使用 ByteBuddy 创建动态 Builder
            Class<?> builderClass = new ByteBuddy()
                    .subclass(Object.class)
                    .implement(Message.Builder.class)
                    .method(ElementMatchers.any())
                    .intercept(MethodDelegation.to(new ByteBuddyBuilderInterceptor(messageClass)))
                    .make()
                    .load(messageClass.getClassLoader())
                    .getLoaded();

            return (Message.Builder) builderClass.getDeclaredConstructor().newInstance();
        }
    }

    /**
     * 获取 MessageLite 类型的 Builder
     */
    @NotNull
    private MessageLite.Builder getLiteBuilderForClass(@NotNull Class<? extends MessageLite> messageClass) {
        try {
            return (MessageLite.Builder) messageClass.getMethod("newBuilder").invoke(null);
        } catch (Exception e) {
            throw new SerializationException("Failed to get builder for class: " + messageClass.getSimpleName(), e);
        }
    }

    /**
     * ByteBuddy Builder 拦截器
     */
    public static class ByteBuddyBuilderInterceptor {
        private final Class<?> messageClass;
        private static final Logger log = LoggerFactory.getLogger(ByteBuddyBuilderInterceptor.class);

        public ByteBuddyBuilderInterceptor(Class<?> messageClass) {
            this.messageClass = messageClass;
        }

        @net.bytebuddy.implementation.bind.annotation.RuntimeType
        public Object intercept(@net.bytebuddy.implementation.bind.annotation.AllArguments Object[] args,
                @net.bytebuddy.implementation.bind.annotation.Origin java.lang.reflect.Method method) {
            try {
                String methodName = method.getName();
                if ("build".equals(methodName)) {
                    // 返回一个默认的消息实例
                    return createDefaultMessage();
                }
                // 其他方法的默认实现
                return this;
            } catch (Exception e) {
                log.error("Error in ByteBuddy builder interceptor", e);
                throw new RuntimeException("Failed to build message", e);
            }
        }

        private Object createDefaultMessage() throws Exception {
            // 尝试创建默认的消息实例
            return messageClass.getDeclaredConstructor().newInstance();
        }
    }

    /**
     * ByteBuddy MessageLite Builder 拦截器
     */
    public static class ByteBuddyLiteBuilderInterceptor {
        private final Class<?> messageClass;
        private static final Logger log = LoggerFactory.getLogger(ByteBuddyLiteBuilderInterceptor.class);

        public ByteBuddyLiteBuilderInterceptor(Class<?> messageClass) {
            this.messageClass = messageClass;
        }

        @net.bytebuddy.implementation.bind.annotation.RuntimeType
        public Object intercept(@net.bytebuddy.implementation.bind.annotation.AllArguments Object[] args,
                @net.bytebuddy.implementation.bind.annotation.Origin java.lang.reflect.Method method) {
            try {
                String methodName = method.getName();
                if ("build".equals(methodName)) {
                    // 返回一个默认的消息实例
                    return createDefaultMessage();
                }
                // 其他方法的默认实现
                return this;
            } catch (Exception e) {
                log.error("Error in ByteBuddy lite builder interceptor", e);
                throw new RuntimeException("Failed to build lite message", e);
            }
        }

        private Object createDefaultMessage() throws Exception {
            // 尝试创建默认的消息实例
            return messageClass.getDeclaredConstructor().newInstance();
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
