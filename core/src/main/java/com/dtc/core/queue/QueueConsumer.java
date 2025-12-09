package com.dtc.core.queue;

import com.dtc.api.annotations.NotNull;

/**
 * 队列消息消费者接口
 * 负责处理队列中的消息消费逻辑
 * 
 * @author Network Service Template
 */
public interface QueueConsumer<T> {

    /**
     * 消费队列消息
     * 
     * @param data       消息数据
     * @param sequence   序列号
     * @param endOfBatch 是否为批次结束
     */
    void consume(@NotNull T data, long sequence, boolean endOfBatch);
}
