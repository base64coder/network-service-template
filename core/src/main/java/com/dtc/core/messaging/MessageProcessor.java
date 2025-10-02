package com.dtc.core.messaging;

import java.util.concurrent.atomic.AtomicLong;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dtc.api.annotations.NotNull;
import com.dtc.core.queue.DisruptorQueue;
import com.dtc.core.queue.QueueConsumer;
import com.dtc.core.serialization.ProtobufSerializer;
import com.google.protobuf.Message;

/**
 * 消息处理器 集成 Protobuf 序列化和 Disruptor 队列，提供高性能消息处理
 * 
 * @author Network Service Template
 */
@Singleton
public class MessageProcessor {

    private static final Logger log = LoggerFactory.getLogger(MessageProcessor.class);
    private static final int DEFAULT_QUEUE_SIZE = 1024 * 1024; // 1M

    private final @NotNull ProtobufSerializer serializer;
    private final @NotNull DisruptorQueue<byte[]> messageQueue;
    private final @NotNull AtomicLong processedCount = new AtomicLong(0);
    private final @NotNull AtomicLong errorCount = new AtomicLong(0);

    @Inject
    public MessageProcessor(@NotNull ProtobufSerializer serializer) {
        this.serializer = serializer;
        this.messageQueue = new DisruptorQueue<>(DEFAULT_QUEUE_SIZE);

        // 添加消息消费者
        messageQueue.addConsumer(new MessageConsumer());

        log.info("Message processor initialized with queue size: {}", DEFAULT_QUEUE_SIZE);
    }

    /**
     * 启动消息处理器
     */
    public void start() {
        messageQueue.start();
        log.info("Message processor started");
    }

    /**
     * 停止消息处理器
     */
    public void shutdown() {
        messageQueue.shutdown();
        log.info("Message processor shutdown");
    }

    /**
     * 处理 Protobuf 消息
     * 
     * @param message 要处理的消息
     * @return 是否成功提交到队列
     */
    public boolean processMessage(@NotNull Message message) {
        try {
            // 序列化消息
            byte[] serializedData = serializer.serialize(message);

            // 发布到队列
            boolean success = messageQueue.publish(serializedData);

            if (success) {
                log.debug("Message published to queue: {}", message.getClass().getSimpleName());
            } else {
                log.warn("Failed to publish message to queue: {}", message.getClass().getSimpleName());
            }

            return success;
        } catch (Exception e) {
            log.error("Error processing message: {}", message.getClass().getSimpleName(), e);
            errorCount.incrementAndGet();
            return false;
        }
    }

    /**
     * 处理原始字节数据
     * 
     * @param data 原始字节数据
     * @return 是否成功提交到队列
     */
    public boolean processRawData(@NotNull byte[] data) {
        try {
            boolean success = messageQueue.publish(data);

            if (success) {
                log.debug("Raw data published to queue: {} bytes", data.length);
            } else {
                log.warn("Failed to publish raw data to queue: {} bytes", data.length);
            }

            return success;
        } catch (Exception e) {
            log.error("Error processing raw data", e);
            errorCount.incrementAndGet();
            return false;
        }
    }

    /**
     * 获取处理统计信息
     */
    @NotNull
    public ProcessingStats getStats() {
        return new ProcessingStats(processedCount.get(), errorCount.get(), messageQueue.getStatus());
    }

    /**
     * 消息消费者
     */
    private class MessageConsumer implements QueueConsumer<byte[]> {
        @Override
        public void consume(@NotNull byte[] data, long sequence, boolean endOfBatch) {
            try {
                // 这里可以添加具体的消息处理逻辑
                // 例如：反序列化、业务处理、转发等
                log.debug("Processing message: sequence={}, size={} bytes, endOfBatch={}", sequence, data.length,
                        endOfBatch);

                processedCount.incrementAndGet();

                // 可以在这里添加具体的业务处理逻辑
                // handleBusinessLogic(data);

            } catch (Exception e) {
                log.error("Error consuming message at sequence: {}", sequence, e);
                errorCount.incrementAndGet();
            }
        }
    }

    /**
     * 处理统计信息
     */
    public static class ProcessingStats {
        private final long processedCount;
        private final long errorCount;
        private final DisruptorQueue.QueueStatus queueStatus;

        public ProcessingStats(long processedCount, long errorCount, DisruptorQueue.QueueStatus queueStatus) {
            this.processedCount = processedCount;
            this.errorCount = errorCount;
            this.queueStatus = queueStatus;
        }

        public long getProcessedCount() {
            return processedCount;
        }

        public long getErrorCount() {
            return errorCount;
        }

        public DisruptorQueue.QueueStatus getQueueStatus() {
            return queueStatus;
        }

        @Override
        public String toString() {
            return String.format("ProcessingStats{processed=%d, errors=%d, queue=%s}", processedCount, errorCount,
                    queueStatus);
        }
    }
}
