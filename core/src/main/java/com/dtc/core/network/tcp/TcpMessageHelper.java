package com.dtc.core.network.tcp;

import jakarta.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dtc.api.annotations.NotNull;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * TCP 消息辅助工具类
 * 提供TCP消息发送和处理的工具方法
 * 
 * @author Network Service Template
 */
@Singleton
public class TcpMessageHelper {

    private static final Logger log = LoggerFactory.getLogger(TcpMessageHelper.class);

    public TcpMessageHelper() {
        log.info("Creating TCP Message Helper instance");
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
        // 可以通过路由管理器实现消息广播逻辑
    }
}
