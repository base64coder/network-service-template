package com.dtc.api;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import io.netty.channel.ChannelHandlerContext;

/**
 * 网络协议扩展接口
 * 定义协议处理的核心方法
 * 
 * @author Network Service Template
 */
public interface ProtocolExtension {

    /**
     * 获取协议名称
     * 
     * @return 协议名称
     */
    @NotNull
    String getProtocolName();

    /**
     * 获取协议版本
     * 
     * @return 协议版本
     */
    @NotNull
    String getProtocolVersion();

    /**
     * 获取默认端口
     * 
     * @return 默认端口号
     */
    int getDefaultPort();

    /**
     * 处理连接建立
     * 
     * @param ctx      通道上下文
     * @param clientId 客户端ID
     */
    void onConnect(@NotNull ChannelHandlerContext ctx, @NotNull String clientId);

    /**
     * 处理连接断开
     * 
     * @param ctx      通道上下文
     * @param clientId 客户端ID
     */
    void onDisconnect(@NotNull ChannelHandlerContext ctx, @NotNull String clientId);

    /**
     * 处理消息接收
     * 
     * @param ctx     通道上下文
     * @param message 接收到的消息
     */
    void onMessage(@NotNull ChannelHandlerContext ctx, @NotNull Object message);

    /**
     * 处理异常
     * 
     * @param ctx   通道上下文
     * @param cause 异常原因
     */
    void onException(@NotNull ChannelHandlerContext ctx, @NotNull Throwable cause);

    /**
     * 获取消息处理器
     * 
     * @return 消息处理器
     */
    @Nullable
    MessageHandler getMessageHandler();
}
