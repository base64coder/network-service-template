package com.dtc.core.messaging.handler;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import com.dtc.core.messaging.MessageHandlerRegistry;
import com.dtc.core.messaging.NetworkMessageEvent;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

/**
 * WebSocket 消息处理器
 * 负责处理 WebSocket 协议类型的消息
 * 
 * @author Network Service Template
 */
@Singleton
public class WebSocketMessageHandler {

    private static final Logger log = LoggerFactory.getLogger(WebSocketMessageHandler.class);

    private final MessageHandlerRegistry messageHandlerRegistry;

    @Inject
    public WebSocketMessageHandler(@Nullable MessageHandlerRegistry messageHandlerRegistry) {
        this.messageHandlerRegistry = messageHandlerRegistry;
    }

    /**
     * 处理 WebSocket 消息
     */
    public void handleMessage(@NotNull NetworkMessageEvent event) {
        log.debug("🔍 Processing WebSocket message: {}", event.getEventId());

        try {
            Object message = event.getMessage();
            ChannelHandlerContext ctx = event.getChannelContext();

            if (message instanceof WebSocketFrame) {
                WebSocketFrame webSocketFrame = (WebSocketFrame) message;

                log.debug("Processing WebSocket frame: {}", webSocketFrame.getClass().getSimpleName());

                // 处理不同类型的WebSocket帧
                if (webSocketFrame instanceof TextWebSocketFrame) {
                    handleTextFrame(ctx, (TextWebSocketFrame) webSocketFrame);
                } else {
                    handleOtherFrame(ctx, webSocketFrame);
                }

            } else {
                log.warn("⚠️  Unexpected message type in WebSocket handler: {}",
                        message != null ? message.getClass().getSimpleName() : "null");
            }

        } catch (Exception e) {
            log.error("❌ Error processing WebSocket message: {}", event.getEventId(), e);
            handleError(event, e);
        }
    }

    /**
     * 处理文本帧
     * 尝试使用注解驱动的处理器，如果未找到则使用默认处理器
     */
    private void handleTextFrame(@NotNull ChannelHandlerContext ctx, @NotNull TextWebSocketFrame textFrame) {
        String text = textFrame.text();
        log.debug("Received WebSocket text message: {}", text);

        try {
            // 查找注解驱动的处理器
            if (messageHandlerRegistry != null) {
                MessageHandlerRegistry.HandlerMethod handler = 
                    messageHandlerRegistry.findHandler("WebSocket", text.trim());
                
                if (handler != null) {
                    try {
                        // 调用用户定义的处理器方法
                        handler.invoke(ctx, text);
                        return;
                    } catch (Exception e) {
                        log.error("Failed to invoke WebSocket handler", e);
                    }
                }
            }
            
            // 如果未找到注解驱动的处理器，使用默认处理器
            log.debug("No annotation-driven handler found for WebSocket message: {}, using default handler", text);
            String response = processWebSocketMessage(text);

            // 发送响应
            TextWebSocketFrame responseFrame = new TextWebSocketFrame(response);
            ctx.writeAndFlush(responseFrame);

            log.debug("✅ WebSocket text message processed successfully");

        } catch (Exception e) {
            log.error("❌ Error processing WebSocket text frame", e);
            sendErrorResponse(ctx, "Error processing message: " + e.getMessage());
        }
    }

    /**
     * 处理其他类型的帧
     */
    private void handleOtherFrame(@NotNull ChannelHandlerContext ctx, @NotNull WebSocketFrame frame) {
        log.debug("Processing WebSocket frame type: {}", frame.getClass().getSimpleName());

        // 处理二进制帧或控制帧
        // 可以通过路由管理器来处理其他帧类型
    }

    /**
     * 处理WebSocket消息的业务逻辑
     */
    @NotNull
    private String processWebSocketMessage(@NotNull String message) {
        // 可以通过路由管理器来处理WebSocket消息的业务逻辑
        // 例如解析JSON、路由到不同的处理器等
        try {
            // 简单的回显处理
            return "Echo: " + message;
        } catch (Exception e) {
            log.error("❌ Error processing WebSocket message", e);
            return "Error: " + e.getMessage();
        }
    }

    /**
     * 发送错误响应
     */
    private void sendErrorResponse(@NotNull ChannelHandlerContext ctx, @NotNull String errorMessage) {
        try {
            TextWebSocketFrame errorFrame = new TextWebSocketFrame("ERROR: " + errorMessage);
            ctx.writeAndFlush(errorFrame);
        } catch (Exception e) {
            log.error("❌ Failed to send error response to WebSocket client: {}", ctx.channel().remoteAddress(), e);
        }
    }

    /**
     * 处理错误
     */
    private void handleError(@NotNull NetworkMessageEvent event, @NotNull Exception error) {
        log.error("🔴 Error handling WebSocket message: {}", event.getEventId(), error);

        try {
            ChannelHandlerContext ctx = event.getChannelContext();
            if (ctx != null && ctx.channel().isActive()) {
                sendErrorResponse(ctx, "Internal server error");
            }
        } catch (Exception e) {
            log.error("❌ Failed to send error response to WebSocket client", e);
        }
    }
}
