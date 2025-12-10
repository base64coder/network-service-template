package com.dtc.core.network.tcp;

import com.dtc.api.annotations.NotNull;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TCP 协议处理器抽象类
 * 定义 TCP 协议的处理逻辑
 * 
 * @author Network Service Template
 */
public abstract class TcpProtocolHandler {

    private static final Logger log = LoggerFactory.getLogger(TcpProtocolHandler.class);

    public TcpProtocolHandler() {
        log.info("Creating TCP Protocol Handler instance");
    }

    /**
     * 处理 TCP 连接
     */
    public abstract void handleConnect(@NotNull ChannelHandlerContext ctx, @NotNull String clientId);

    /**
     * 处理 TCP 断开连接
     */
    public abstract void handleDisconnect(@NotNull ChannelHandlerContext ctx, @NotNull String clientId);

    /**
     * 处理 TCP 消息
     */
    public abstract void handleMessage(@NotNull ChannelHandlerContext ctx, @NotNull Object message);

    /**
     * 处理 TCP 异常
     */
    public abstract void handleException(@NotNull ChannelHandlerContext ctx, @NotNull Throwable cause);

    /**
     * 获取协议名称
     */
    @NotNull
    public abstract String getProtocolName();

    /**
     * 获取协议版本
     */
    @NotNull
    public abstract String getProtocolVersion();

    /**
     * 检查是否支持该消息类型
     */
    public abstract boolean supports(@NotNull Class<?> messageType);
}
