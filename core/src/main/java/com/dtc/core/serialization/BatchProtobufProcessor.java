package com.dtc.core.serialization;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import com.google.protobuf.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * 批量 Protobuf 处理器
 * 提供高性能的批量序列化/反序列化功能
 * 
 * @author Network Service Template
 */
@Singleton
public class BatchProtobufProcessor {

    private static final Logger log = LoggerFactory.getLogger(BatchProtobufProcessor.class);

    private final OptimizedProtobufSerializer serializer;
    private final ScheduledExecutorService scheduler;
    private final BlockingQueue<Message> messageQueue;
    private final List<Consumer<Message[]>> batchConsumers;

    // 配置参数
    private final int maxBatchSize;
    private final long maxBatchDelayMs;
    private final int queueCapacity;

    // 统计信息
    private final AtomicLong totalBatches = new AtomicLong(0);
    private final AtomicLong totalMessages = new AtomicLong(0);
    private final AtomicLong totalProcessingTime = new AtomicLong(0);

    private volatile boolean running = false;

    public BatchProtobufProcessor() {
        this(1000, 100, 10000); // 默认配置
    }

    public BatchProtobufProcessor(int maxBatchSize, long maxBatchDelayMs, int queueCapacity) {
        this.serializer = new OptimizedProtobufSerializer();
        this.scheduler = Executors.newScheduledThreadPool(2);
        this.messageQueue = new LinkedBlockingQueue<>(queueCapacity);
        this.batchConsumers = new ArrayList<>();
        this.maxBatchSize = maxBatchSize;
        this.maxBatchDelayMs = maxBatchDelayMs;
        this.queueCapacity = queueCapacity;
    }

    /**
     * 启动批量处理器
     */
    public void start() {
        if (running) {
            return;
        }

        running = true;

        // 启动批量处理线程
        scheduler.scheduleAtFixedRate(this::processBatch, maxBatchDelayMs, maxBatchDelayMs, TimeUnit.MILLISECONDS);

        // 启动立即处理线程
        scheduler.scheduleAtFixedRate(this::processImmediateBatch, 10, 10, TimeUnit.MILLISECONDS);

        log.info("Batch Protobuf processor started with batchSize={}, delay={}ms, queueCapacity={}", maxBatchSize,
                maxBatchDelayMs, queueCapacity);
    }

    /**
     * 停止批量处理器
     */
    public void stop() {
        if (!running) {
            return;
        }

        running = false;
        scheduler.shutdown();

        // 处理剩余消息
        processRemainingMessages();

        log.info("Batch Protobuf processor stopped");
    }

    /**
     * 添加消息到批量处理器队列
     * 
     * @param message 要处理的消息
     * @return 是否成功添加
     */
    public boolean addMessage(@NotNull Message message) {
        if (!running) {
            return false;
        }

        try {
            boolean added = messageQueue.offer(message);
            if (added) {
                totalMessages.incrementAndGet();
            }
            return added;
        } catch (Exception e) {
            log.error("Failed to add message to batch queue", e);
            return false;
        }
    }

    /**
     * 批量添加消息
     * 
     * @param messages 消息列表
     * @return 成功添加的消息数量
     */
    public int addMessages(@NotNull List<Message> messages) {
        if (!running || messages.isEmpty()) {
            return 0;
        }

        int addedCount = 0;
        for (Message message : messages) {
            if (messageQueue.offer(message)) {
                addedCount++;
                totalMessages.incrementAndGet();
            }
        }

        return addedCount;
    }

    /**
     * 添加批量消息消费者
     * 
     * @param consumer 消息消费者
     */
    public void addBatchConsumer(@NotNull Consumer<Message[]> consumer) {
        synchronized (batchConsumers) {
            batchConsumers.add(consumer);
        }
    }

    /**
     * 移除批量消息消费者
     * 
     * @param consumer 消息消费者
     */
    public void removeBatchConsumer(@NotNull Consumer<Message[]> consumer) {
        synchronized (batchConsumers) {
            batchConsumers.remove(consumer);
        }
    }

    /**
     * 处理批量消息
     */
    private void processBatch() {
        if (!running || messageQueue.isEmpty()) {
            return;
        }

        List<Message> batch = new ArrayList<>(maxBatchSize);
        messageQueue.drainTo(batch, maxBatchSize);

        if (!batch.isEmpty()) {
            processMessageBatch(batch.toArray(new Message[0]));
        }
    }

    /**
     * 立即处理批量消息（当队列达到最大批次大小时）
     */
    private void processImmediateBatch() {
        if (!running || messageQueue.size() < maxBatchSize) {
            return;
        }

        List<Message> batch = new ArrayList<>(maxBatchSize);
        messageQueue.drainTo(batch, maxBatchSize);

        if (!batch.isEmpty()) {
            processMessageBatch(batch.toArray(new Message[0]));
        }
    }

    /**
     * 处理消息批次
     * 
     * @param messages 消息数组
     */
    private void processMessageBatch(@NotNull Message[] messages) {
        long startTime = System.nanoTime();

        try {
            // 批量序列化
            byte[][] serializedData = serializer.serializeBatch(messages);

            // 通知所有消息消费者
            synchronized (batchConsumers) {
                for (Consumer<Message[]> consumer : batchConsumers) {
                    try {
                        consumer.accept(messages);
                    } catch (Exception e) {
                        log.error("Error in batch consumer", e);
                    }
                }
            }

            totalBatches.incrementAndGet();
            totalProcessingTime.addAndGet(System.nanoTime() - startTime);

            log.debug("Processed batch of {} messages in {}ns", messages.length, System.nanoTime() - startTime);

        } catch (Exception e) {
            log.error("Error processing message batch", e);
        }
    }

    /**
     * 处理剩余消息
     */
    private void processRemainingMessages() {
        List<Message> remainingMessages = new ArrayList<>();
        messageQueue.drainTo(remainingMessages);

        if (!remainingMessages.isEmpty()) {
            log.info("Processing {} remaining messages", remainingMessages.size());
            processMessageBatch(remainingMessages.toArray(new Message[0]));
        }
    }

    /**
     * 获取队列状态
     */
    @NotNull
    public QueueStatus getQueueStatus() {
        return new QueueStatus(messageQueue.size(), queueCapacity, totalBatches.get(), totalMessages.get(),
                totalProcessingTime.get());
    }

    /**
     * 预热处理器
     * 
     * @param messageClasses 要预热的消息类型
     */
    public void warmup(@NotNull Class<? extends Message>... messageClasses) {
        serializer.warmupCache(messageClasses);
        log.info("Batch processor warmed up for {} message types", messageClasses.length);
    }

    /**
     * 队列状态
     */
    public static class QueueStatus {
        private final int currentSize;
        private final int capacity;
        private final long totalBatches;
        private final long totalMessages;
        private final long totalProcessingTime;

        public QueueStatus(int currentSize, int capacity, long totalBatches, long totalMessages,
                long totalProcessingTime) {
            this.currentSize = currentSize;
            this.capacity = capacity;
            this.totalBatches = totalBatches;
            this.totalMessages = totalMessages;
            this.totalProcessingTime = totalProcessingTime;
        }

        public int getCurrentSize() {
            return currentSize;
        }

        public int getCapacity() {
            return capacity;
        }

        public long getTotalBatches() {
            return totalBatches;
        }

        public long getTotalMessages() {
            return totalMessages;
        }

        public long getTotalProcessingTime() {
            return totalProcessingTime;
        }

        public double getQueueUtilization() {
            return capacity > 0 ? (double) currentSize / capacity : 0.0;
        }

        public double getAverageBatchSize() {
            return totalBatches > 0 ? (double) totalMessages / totalBatches : 0.0;
        }

        public double getAverageProcessingTime() {
            return totalBatches > 0 ? (double) totalProcessingTime / totalBatches : 0.0;
        }

        @Override
        public String toString() {
            return String.format(
                    "QueueStatus{size=%d/%d, utilization=%.2f%%, batches=%d, "
                            + "avgBatchSize=%.2f, avgProcessingTime=%.2fns}",
                    currentSize, capacity, getQueueUtilization() * 100, totalBatches, getAverageBatchSize(),
                    getAverageProcessingTime());
        }
    }
}
