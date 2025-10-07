package com.dtc.core.messaging;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import io.netty.channel.ChannelHandlerContext;

/**
 * 网络消息事件
 * 封装从网络接收到的消息，用于Disruptor队列处理
 * 
 * @author Network Service Template
 */
public class NetworkMessageEvent {

    private String eventId;
    private String protocolType;
    private String clientId;
    private Object message;
    private ChannelHandlerContext channelContext;
    private long timestamp;
    private String sourceAddress;
    private int messageSize;
    private String messageType;
    private boolean isRequest;
    private boolean isResponse;
    private String correlationId;
    private int priority;

    public NetworkMessageEvent() {
        this.timestamp = System.currentTimeMillis();
        this.priority = 0; // 默认优先级
    }

    // ========== Builder模式 ==========

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final NetworkMessageEvent event = new NetworkMessageEvent();

        public Builder eventId(String eventId) {
            event.eventId = eventId;
            return this;
        }

        public Builder protocolType(String protocolType) {
            event.protocolType = protocolType;
            return this;
        }

        public Builder clientId(String clientId) {
            event.clientId = clientId;
            return this;
        }

        public Builder message(Object message) {
            event.message = message;
            return this;
        }

        public Builder channelContext(ChannelHandlerContext channelContext) {
            event.channelContext = channelContext;
            return this;
        }

        public Builder sourceAddress(String sourceAddress) {
            event.sourceAddress = sourceAddress;
            return this;
        }

        public Builder messageSize(int messageSize) {
            event.messageSize = messageSize;
            return this;
        }

        public Builder messageType(String messageType) {
            event.messageType = messageType;
            return this;
        }

        public Builder isRequest(boolean isRequest) {
            event.isRequest = isRequest;
            return this;
        }

        public Builder isResponse(boolean isResponse) {
            event.isResponse = isResponse;
            return this;
        }

        public Builder correlationId(String correlationId) {
            event.correlationId = correlationId;
            return this;
        }

        public Builder priority(int priority) {
            event.priority = priority;
            return this;
        }

        public NetworkMessageEvent build() {
            if (event.eventId == null) {
                event.eventId = generateEventId();
            }
            return event;
        }

        private String generateEventId() {
            return "event-" + System.currentTimeMillis() + "-" + Thread.currentThread().getId();
        }
    }

    // ========== Getters ==========

    @NotNull
    public String getEventId() {
        return eventId;
    }

    @Nullable
    public String getProtocolType() {
        return protocolType;
    }

    @Nullable
    public String getClientId() {
        return clientId;
    }

    @Nullable
    public Object getMessage() {
        return message;
    }

    @Nullable
    public ChannelHandlerContext getChannelContext() {
        return channelContext;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Nullable
    public String getSourceAddress() {
        return sourceAddress;
    }

    public int getMessageSize() {
        return messageSize;
    }

    @Nullable
    public String getMessageType() {
        return messageType;
    }

    public boolean isRequest() {
        return isRequest;
    }

    public boolean isResponse() {
        return isResponse;
    }

    @Nullable
    public String getCorrelationId() {
        return correlationId;
    }

    public int getPriority() {
        return priority;
    }

    // ========== Setters ==========

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public void setProtocolType(String protocolType) {
        this.protocolType = protocolType;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public void setMessage(Object message) {
        this.message = message;
    }

    public void setChannelContext(ChannelHandlerContext channelContext) {
        this.channelContext = channelContext;
    }

    public void setSourceAddress(String sourceAddress) {
        this.sourceAddress = sourceAddress;
    }

    public void setMessageSize(int messageSize) {
        this.messageSize = messageSize;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public void setRequest(boolean request) {
        isRequest = request;
    }

    public void setResponse(boolean response) {
        isResponse = response;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    // ========== 工具方法 ==========

    /**
     * 重置事件数据
     */
    public void clear() {
        this.eventId = null;
        this.protocolType = null;
        this.clientId = null;
        this.message = null;
        this.channelContext = null;
        this.sourceAddress = null;
        this.messageSize = 0;
        this.messageType = null;
        this.isRequest = false;
        this.isResponse = false;
        this.correlationId = null;
        this.priority = 0;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 检查事件是否有效
     */
    public boolean isValid() {
        return eventId != null && message != null && channelContext != null;
    }

    /**
     * 获取事件年龄（毫秒）
     */
    public long getAge() {
        return System.currentTimeMillis() - timestamp;
    }

    @Override
    public String toString() {
        return String.format("NetworkMessageEvent{eventId='%s', protocolType='%s', clientId='%s', " +
                "messageType='%s', size=%d, age=%dms, priority=%d}",
                eventId, protocolType, clientId, messageType, messageSize, getAge(), priority);
    }
}
