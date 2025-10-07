package com.dtc.core.tcp;

import com.dtc.api.annotations.NotNull;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;

/**
 * TCP 消息处理器
 * 负责处理 TCP 协议消息的编码、解码和路由
 * 
 * @author Network Service Template
 */
@Singleton
public class TcpMessageHandler {

    private static final Logger log = LoggerFactory.getLogger(TcpMessageHandler.class);

    public TcpMessageHandler() {
        log.info("Creating TCP Message Handler instance");
    }

    /**
     * 处理 TCP 消息
     */
    public void handleMessage(@NotNull ChannelHandlerContext ctx, @NotNull Object message) {
        log.debug("Handling TCP message from client: {}", ctx.channel().remoteAddress());

        if (message instanceof ByteBuf) {
            handleByteBufMessage(ctx, (ByteBuf) message);
        } else {
            log.warn("Received unexpected message type: {}", message.getClass().getSimpleName());
        }
    }

    /**
     * 处理 ByteBuf 消息
     */
    private void handleByteBufMessage(@NotNull ChannelHandlerContext ctx, @NotNull ByteBuf message) {
        try {
            // 读取消息内容
            byte[] data = new byte[message.readableBytes()];
            message.getBytes(message.readerIndex(), data);

            // 处理消息内容
            String content = new String(data);
            log.debug("Processing TCP message: {}", content);

            // 实现具体的 TCP 消息处理逻辑
            processTcpData(ctx, content);

        } catch (Exception e) {
            log.error("Failed to process TCP message", e);
        }
    }

    /**
     * 处理 TCP 数据
     */
    private void processTcpData(@NotNull ChannelHandlerContext ctx, @NotNull String data) {
        log.debug("Processing TCP data: {}", data);

        // 示例：简单的回显处理
        if (data.trim().equals("ping")) {
            sendResponse(ctx, "pong");
        } else if (data.trim().equals("hello")) {
            sendResponse(ctx, "Hello from TCP Server!");
        } else {
            sendResponse(ctx, "Echo: " + data);
        }
    }

    /**
     * 发送响应
     */
    public void sendResponse(@NotNull ChannelHandlerContext ctx, @NotNull String response) {
        try {
            ByteBuf buffer = ctx.alloc().buffer();
            buffer.writeBytes(response.getBytes());
            ctx.writeAndFlush(buffer);
            log.debug("Sent TCP response: {}", response);
        } catch (Exception e) {
            log.error("Failed to send TCP response", e);
        }
    }

    /**
     * 发送二进制数据
     */
    public void sendBinaryData(@NotNull ChannelHandlerContext ctx, @NotNull byte[] data) {
        try {
            ByteBuf buffer = ctx.alloc().buffer();
            buffer.writeBytes(data);
            ctx.writeAndFlush(buffer);
            log.debug("Sent TCP binary data: {} bytes", data.length);
        } catch (Exception e) {
            log.error("Failed to send TCP binary data", e);
        }
    }

    /**
     * 广播消息到所有连接的客户端
     */
    public void broadcastMessage(@NotNull String message) {
        log.debug("Broadcasting TCP message to all connected clients");
        // 实现消息广播逻辑
    }
}
