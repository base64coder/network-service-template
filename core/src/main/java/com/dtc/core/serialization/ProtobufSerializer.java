package com.dtc.core.serialization;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import com.google.protobuf.Message;
import com.google.protobuf.MessageLite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Singleton;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * Protobuf 序列化器
 * 提供消息的二进制序列化和反序列化功能
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
     * 序列化消息为字节数组，支持MessageLite类型
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
     * 反序列化字节数组为消息，支持MessageLite类型
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
     * 获取消息类型的Builder
     */
    @NotNull
    private Message.Builder getBuilderForClass(@NotNull Class<? extends Message> messageClass) {
        try {
            // 使用 ByteBuddy 创建 Builder
            return createByteBuddyBuilder(messageClass);
        } catch (Exception e) {
            log.error("Failed to create builder for class: {}", messageClass.getSimpleName(), e);
            throw new SerializationException("Failed to create builder", e);
        }
    }

    /**
     * 获取MessageLite类型的Builder
     */
    @NotNull
    private MessageLite.Builder getLiteBuilderForClass(@NotNull Class<? extends MessageLite> messageClass) {
        try {
            // 使用 ByteBuddy 创建 Builder
            return createByteBuddyLiteBuilder(messageClass);
        } catch (Exception e) {
            log.error("Failed to create builder for class: {}", messageClass.getSimpleName(), e);
            throw new SerializationException("Failed to create builder", e);
        }
    }

    /**
     * 使用ByteBuddy创建Message Builder
     */
    @NotNull
    private Message.Builder createByteBuddyBuilder(@NotNull Class<? extends Message> messageClass) {
        try {
            // 尝试通过反射获取Builder
            java.lang.reflect.Method newBuilderMethod = messageClass.getMethod("newBuilder");
            return (Message.Builder) newBuilderMethod.invoke(null);
        } catch (Exception e) {
            log.error("Failed to create builder using reflection for class: {}", messageClass.getSimpleName(), e);
            throw new SerializationException("Failed to create builder", e);
        }
    }

    /**
     * 使用ByteBuddy创建MessageLite Builder
     */
    @NotNull
    private MessageLite.Builder createByteBuddyLiteBuilder(@NotNull Class<? extends MessageLite> messageClass) {
        try {
            // 尝试通过反射获取Builder
            java.lang.reflect.Method newBuilderMethod = messageClass.getMethod("newBuilder");
            return (MessageLite.Builder) newBuilderMethod.invoke(null);
        } catch (Exception e) {
            log.error("Failed to create builder using reflection for class: {}", messageClass.getSimpleName(), e);
            throw new SerializationException("Failed to create builder", e);
        }
    }

    /**
     * 序列化异常
     */
    public static class SerializationException extends RuntimeException {
        public SerializationException(String message) {
            super(message);
        }

        public SerializationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
