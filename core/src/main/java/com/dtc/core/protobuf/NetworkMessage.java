package com.dtc.core.protobuf;

import com.dtc.api.annotations.NotNull;
import com.google.protobuf.Message;
import com.google.protobuf.MessageLite;

/**
 * 网络消息接口 定义网络传输的消息规范
 * 
 * @author Network Service Template
 */
public interface NetworkMessage extends MessageLite {

    /**
     * 获取消息类型
     */
    @NotNull
    String getMessageType();

    /**
     * 获取消息ID
     */
    long getMessageId();

    /**
     * 获取时间戳
     */
    long getTimestamp();

    /**
     * 获取消息优先级
     */
    int getPriority();

    /**
     * 获取消息大小
     */
    int getMessageSize();
}
