package com.dtc.core.queue;

import com.dtc.api.annotations.NotNull;
import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Disruptor 队列实现
 * 基于高性能无锁队列Disruptor实现，提供高吞吐量的消息处理
 * 
 * @author Network Service Template
 */
@Singleton
public class DisruptorQueue<T> {

    private static final Logger log = LoggerFactory.getLogger(DisruptorQueue.class);
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 1024; // 1M

    private final Disruptor<QueueEvent<T>> disruptor;
    private final RingBuffer<QueueEvent<T>> ringBuffer;
    private final AtomicBoolean started = new AtomicBoolean(false);

    public DisruptorQueue() {
        this(DEFAULT_BUFFER_SIZE);
    }

    public DisruptorQueue(int bufferSize) {
        // 创建事件工厂
        QueueEventFactory<T> eventFactory = new QueueEventFactory<>();

        // 创建线程池
        Executor executor = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "DisruptorQueue-Worker");
            t.setDaemon(true);
            return t;
        });

        // 创建 Disruptor
        this.disruptor = new Disruptor<QueueEvent<T>>(eventFactory, bufferSize, executor, ProducerType.MULTI,
                new YieldingWaitStrategy());

        // 获取 RingBuffer
        this.ringBuffer = disruptor.getRingBuffer();
    }

    /**
     * 启动队列
     */
    public void start() {
        if (started.compareAndSet(false, true)) {
            disruptor.start();
            log.info("Disruptor queue started with buffer size: {}", ringBuffer.getBufferSize());
        }
    }

    /**
     * 停止队列
     */
    public void shutdown() {
        if (started.compareAndSet(true, false)) {
            disruptor.shutdown();
            log.info("Disruptor queue shutdown");
        }
    }

    /**
     * 发布消息到队列
     * 
     * @param data 要发布的数据
     * @return 是否发布成功
     */
    public boolean publish(@NotNull T data) {
        if (!started.get()) {
            log.warn("Queue is not started, cannot publish message");
            return false;
        }

        try {
            long sequence = ringBuffer.next();
            try {
                QueueEvent<T> event = ringBuffer.get(sequence);
                event.setData(data);
                event.setTimestamp(System.currentTimeMillis());
            } finally {
                ringBuffer.publish(sequence);
            }
            return true;
        } catch (Exception e) {
            log.error("Failed to publish message to queue", e);
            return false;
        }
    }

    /**
     * 添加消息消费者
     * 
     * @param consumer 消息消费者
     */
    public void addConsumer(@NotNull QueueConsumer<T> consumer) {
        @SuppressWarnings("unchecked")
        QueueEventHandler<T>[] handlers = new QueueEventHandler[] { new QueueEventHandler<>(consumer) };
        disruptor.handleEventsWith(handlers);
        log.info("Added consumer: {}", consumer.getClass().getSimpleName());
    }

    /**
     * 添加多个消息消费者，用于并行处理
     * 
     * @param consumers 消息消费者数组
     */
    @SafeVarargs
    public final void addConsumers(@NotNull QueueConsumer<T>... consumers) {
        @SuppressWarnings("unchecked")
        QueueEventHandler<T>[] handlers = (QueueEventHandler<T>[]) new QueueEventHandler[consumers.length];
        for (int i = 0; i < consumers.length; i++) {
            handlers[i] = new QueueEventHandler<>(consumers[i]);
        }
        disruptor.handleEventsWith(handlers);
        log.info("Added {} consumers for parallel processing", consumers.length);
    }

    /**
     * 获取队列状态
     */
    @NotNull
    public QueueStatus getStatus() {
        return new QueueStatus(started.get(), ringBuffer.getBufferSize(), ringBuffer.remainingCapacity(),
                ringBuffer.getCursor());
    }

    /**
     * 队列事件数据
     */
    public static class QueueEvent<T> {
        private T data;
        private long timestamp;

        public T getData() {
            return data;
        }

        public void setData(T data) {
            this.data = data;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }
    }

    /**
     * 队列事件工厂
     */
    public static class QueueEventFactory<T> implements EventFactory<QueueEvent<T>> {
        @Override
        public QueueEvent<T> newInstance() {
            return new QueueEvent<>();
        }
    }

    /**
     * 队列事件处理器
     */
    public static class QueueEventHandler<T> implements EventHandler<QueueEvent<T>> {
        private final QueueConsumer<T> consumer;

        public QueueEventHandler(@NotNull QueueConsumer<T> consumer) {
            this.consumer = consumer;
        }

        @Override
        public void onEvent(QueueEvent<T> event, long sequence, boolean endOfBatch) throws Exception {
            try {
                consumer.consume(event.getData(), sequence, endOfBatch);
            } catch (Exception e) {
                log.error("Error processing queue event", e);
                // 可以通过路由管理器实现错误处理逻辑
            }
        }
    }

    /**
     * 队列状态
     */
    public static class QueueStatus {
        private final boolean started;
        private final int bufferSize;
        private final long remainingCapacity;
        private final long cursor;

        public QueueStatus(boolean started, int bufferSize, long remainingCapacity, long cursor) {
            this.started = started;
            this.bufferSize = bufferSize;
            this.remainingCapacity = remainingCapacity;
            this.cursor = cursor;
        }

        public boolean isStarted() {
            return started;
        }

        public int getBufferSize() {
            return bufferSize;
        }

        public long getRemainingCapacity() {
            return remainingCapacity;
        }

        public long getCursor() {
            return cursor;
        }

        @Override
        public String toString() {
            return String.format("QueueStatus{started=%s, bufferSize=%d, remaining=%d, cursor=%d}", started, bufferSize,
                    remainingCapacity, cursor);
        }
    }
}
