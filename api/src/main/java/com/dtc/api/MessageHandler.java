package com.dtc.api;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import io.netty.channel.ChannelHandlerContext;

/**
 * 消息处理器接口
 * 定义消息处理的核心方法
 * 
 * @author Network Service Template
 */
public interface MessageHandler {

    /**
     * 处理接收到的消息
     * 
     * @param ctx     通道上下文
     * @param message 消息对象
     * @return 处理结果，null表示继续处理，非null表示停止处理链
     */
    @Nullable
    Object handleMessage(@NotNull ChannelHandlerContext ctx, @NotNull Object message);

    /**
     * 处理发送的消息
     * 
     * @param ctx     通道上下文
     * @param message 消息对象
     * @return 处理后的消息，null表示不发送
     */
    @Nullable
    Object handleOutboundMessage(@NotNull ChannelHandlerContext ctx, @NotNull Object message);

    /**
     * 获取处理器优先级
     * 数值越小，优先级越高
     * 
     * @return 优先级
     */
    default int getPriority() {
        return 100;
    }

    /**
     * 是否支持该消息类型
     * 
     * @param messageType 消息类型
     * @return 是否支持
     */
    default boolean supports(@NotNull Class<?> messageType) {
        return true;
    }
}
