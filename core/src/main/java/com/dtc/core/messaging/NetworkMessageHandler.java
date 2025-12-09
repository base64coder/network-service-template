package com.dtc.core.messaging;

import java.util.concurrent.atomic.AtomicLong;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dtc.api.annotations.NotNull;
import com.dtc.core.serialization.ProtobufSerializer;
import com.google.protobuf.Message;

/**
 * 网络消息处理器
 * 处理网络消息的接收和转发，将消息封装为事件并放入队列
 * 
 * @author Network Service Template
 */
@Singleton
public class NetworkMessageHandler {

    private static final Logger log = LoggerFactory.getLogger(NetworkMessageHandler.class);

    private final @NotNull ProtobufSerializer serializer;
    private final @NotNull NetworkMessageQueue messageQueue;
    private final @NotNull AtomicLong receivedCount = new AtomicLong(0);
    private final @NotNull AtomicLong forwardedCount = new AtomicLong(0);

    @Inject
    public NetworkMessageHandler(@NotNull ProtobufSerializer serializer, @NotNull NetworkMessageQueue messageQueue) {
        this.serializer = serializer;
        this.messageQueue = messageQueue;
    }

    /**
     * 处理接收到的消息
     * 
     * @param message 接收到的消息
     * @return 是否处理成功
     */
    public boolean handleMessage(@NotNull Message message) {
        try {
            receivedCount.incrementAndGet();

            log.debug("Handling message: {} (size: {} bytes)", message.getClass().getSimpleName(),
                    message.getSerializedSize());

            // 创建网络消息事件并放入队列
            NetworkMessageEvent event = NetworkMessageEvent.builder()
                    .protocolType("tcp") // 默认协议类型
                    .message(message)
                    .messageType(message.getClass().getSimpleName())
                    .messageSize(message.getSerializedSize())
                    .build();

            boolean success = messageQueue.publish(event);

            if (success) {
                forwardedCount.incrementAndGet();
                log.debug("Message forwarded to queue: {}", message.getClass().getSimpleName());
            } else {
                log.warn("Failed to forward message to queue: {}", message.getClass().getSimpleName());
            }

            return success;
        } catch (Exception e) {
            log.error("Error handling message: {}", message.getClass().getSimpleName(), e);
            return false;
        }
    }

    /**
     * 处理原始字节数据
     * 
     * @param data 原始字节数据
     * @return 是否处理成功
     */
    public boolean handleRawData(@NotNull byte[] data) {
        try {
            receivedCount.incrementAndGet();

            log.debug("Handling raw data: {} bytes", data.length);

            // 创建网络消息事件并放入队列
            NetworkMessageEvent event = NetworkMessageEvent.builder()
                    .protocolType("custom") // 原始数据默认为custom协议
                    .message(data)
                    .messageType("RawData")
                    .messageSize(data.length)
                    .build();

            boolean success = messageQueue.publish(event);

            if (success) {
                forwardedCount.incrementAndGet();
                log.debug("Raw data forwarded to queue: {} bytes", data.length);
            } else {
                log.warn("Failed to forward raw data to queue: {} bytes", data.length);
            }

            return success;
        } catch (Exception e) {
            log.error("Error handling raw data: {} bytes", data.length, e);
            return false;
        }
    }

    /**
     * 获取处理器统计信息
     */
    @NotNull
    public HandlerStats getStats() {
        return new HandlerStats(receivedCount.get(), forwardedCount.get());
    }

    /**
     * 处理器统计信息类
     */
    public static class HandlerStats {
        private final long receivedCount;
        private final long forwardedCount;

        public HandlerStats(long receivedCount, long forwardedCount) {
            this.receivedCount = receivedCount;
            this.forwardedCount = forwardedCount;
        }

        public long getReceivedCount() {
            return receivedCount;
        }

        public long getForwardedCount() {
            return forwardedCount;
        }

        @Override
        public String toString() {
            return String.format("HandlerStats{received=%d, forwarded=%d}", receivedCount, forwardedCount);
        }
    }
}
