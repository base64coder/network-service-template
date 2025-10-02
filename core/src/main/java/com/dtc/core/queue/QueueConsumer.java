package com.dtc.core.queue;

import com.dtc.api.annotations.NotNull;

/**
 * 队列消费者接口 定义消息消费的处理逻辑
 * 
 * @author Network Service Template
 */
public interface QueueConsumer<T> {

    /**
     * 消费消息
     * 
     * @param data       消息数据
     * @param sequence   序列号
     * @param endOfBatch 是否为批次结束
     */
    void consume(@NotNull T data, long sequence, boolean endOfBatch);
}
