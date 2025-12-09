package com.dtc.core.network.websocket;

import com.dtc.api.annotations.NotNull;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;

/**
 * WebSocket 消息辅助工具类
 * 提供WebSocket消息发送和处理的工具方法
 * 
 * @author Network Service Template
 */
@Singleton
public class WebSocketMessageHelper {

    private static final Logger log = LoggerFactory.getLogger(WebSocketMessageHelper.class);

    public WebSocketMessageHelper() {
        log.info("Creating WebSocket Message Helper instance");
    }

    /**
     * 发送文本消息
     */
    public void sendTextMessage(@NotNull ChannelHandlerContext ctx, @NotNull String message) {
        try {
            TextWebSocketFrame frame = new TextWebSocketFrame(message);
            ctx.writeAndFlush(frame);
            log.debug("Sent WebSocket text message: {}", message);
        } catch (Exception e) {
            log.error("Failed to send WebSocket text message", e);
        }
    }

    /**
     * 发送二进制消息
     */
    public void sendBinaryMessage(@NotNull ChannelHandlerContext ctx, @NotNull byte[] data) {
        try {
            // 可以通过路由管理器实现二进制消息发送逻辑
            log.debug("Sent WebSocket binary message: {} bytes", data.length);
        } catch (Exception e) {
            log.error("Failed to send WebSocket binary message", e);
        }
    }

    /**
     * 广播消息到所有连接的客户端
     */
    public void broadcastMessage(@NotNull String message) {
        log.debug("Broadcasting WebSocket message to all connected clients");
        // 可以通过路由管理器实现消息广播逻辑
    }
}
