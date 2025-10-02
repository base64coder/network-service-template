package com.dtc.core.messaging;

import com.dtc.api.annotations.NotNull;
import com.dtc.core.serialization.ProtobufSerializer;
import com.google.protobuf.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 网络消息处理器 处理网络消息的接收、序列化和转发
 * 
 * @author Network Service Template
 */
@Singleton
public class NetworkMessageHandler {

    private static final Logger log = LoggerFactory.getLogger(NetworkMessageHandler.class);

    private final @NotNull ProtobufSerializer serializer;
    private final @NotNull MessageProcessor messageProcessor;
    private final @NotNull AtomicLong receivedCount = new AtomicLong(0);
    private final @NotNull AtomicLong forwardedCount = new AtomicLong(0);

    @Inject
    public NetworkMessageHandler(@NotNull ProtobufSerializer serializer, @NotNull MessageProcessor messageProcessor) {
        this.serializer = serializer;
        this.messageProcessor = messageProcessor;
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

            // 使用消息处理器处理消息
            boolean success = messageProcessor.processMessage(message);

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

            // 使用消息处理器处理原始数据
            boolean success = messageProcessor.processRawData(data);

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
     * 获取处理统计信息
     */
    @NotNull
    public HandlerStats getStats() {
        return new HandlerStats(receivedCount.get(), forwardedCount.get(), messageProcessor.getStats());
    }

    /**
     * 处理器统计信息
     */
    public static class HandlerStats {
        private final long receivedCount;
        private final long forwardedCount;
        private final MessageProcessor.ProcessingStats processingStats;

        public HandlerStats(long receivedCount, long forwardedCount, MessageProcessor.ProcessingStats processingStats) {
            this.receivedCount = receivedCount;
            this.forwardedCount = forwardedCount;
            this.processingStats = processingStats;
        }

        public long getReceivedCount() {
            return receivedCount;
        }

        public long getForwardedCount() {
            return forwardedCount;
        }

        public MessageProcessor.ProcessingStats getProcessingStats() {
            return processingStats;
        }

        @Override
        public String toString() {
            return String.format("HandlerStats{received=%d, forwarded=%d, processing=%s}", receivedCount,
                    forwardedCount, processingStats);
        }
    }
}
