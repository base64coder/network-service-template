package com.dtc.core.network.custom;

import com.dtc.api.annotations.NotNull;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Singleton;

/**
 * Custom 消息辅助工具类
 * 提供自定义协议消息发送的辅助方法
 * 
 * @author Network Service Template
 */
@Singleton
public class CustomMessageHelper {

    private static final Logger log = LoggerFactory.getLogger(CustomMessageHelper.class);

    public CustomMessageHelper() {
        log.info("Creating Custom Message Helper instance");
    }

    /**
     * 发送响应
     */
    public void sendResponse(@NotNull ChannelHandlerContext ctx, @NotNull String response) {
        try {
            ByteBuf buffer = ctx.alloc().buffer();
            buffer.writeBytes(response.getBytes());
            ctx.writeAndFlush(buffer);
            log.debug("Sent Custom response: {}", response);
        } catch (Exception e) {
            log.error("Failed to send Custom response", e);
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
            log.debug("Sent Custom binary data: {} bytes", data.length);
        } catch (Exception e) {
            log.error("Failed to send Custom binary data", e);
        }
    }

    /**
     * 广播消息到所有连接的客户端
     */
    public void broadcastMessage(@NotNull String message) {
        log.debug("Broadcasting Custom message to all connected clients");
        // 可以通过路由管理器实现消息广播逻辑
    }
}
