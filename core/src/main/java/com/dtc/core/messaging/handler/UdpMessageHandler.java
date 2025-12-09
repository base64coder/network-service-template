package com.dtc.core.messaging.handler;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import com.dtc.core.messaging.MessageHandlerRegistry;
import com.dtc.core.messaging.NetworkMessageEvent;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/**
 * UDP 消息处理器
 * 负责处理 UDP 协议类型的消息，从Disruptor队列接收并分发处理
 * 
 * @author Network Service Template
 */
@Singleton
public class UdpMessageHandler {

    private static final Logger log = LoggerFactory.getLogger(UdpMessageHandler.class);

    private final MessageHandlerRegistry messageHandlerRegistry;

    @Inject
    public UdpMessageHandler(@Nullable MessageHandlerRegistry messageHandlerRegistry) {
        this.messageHandlerRegistry = messageHandlerRegistry;
    }

    /**
     * 处理 UDP 消息
     */
    public void handleMessage(@NotNull NetworkMessageEvent event) {
        log.debug("Processing UDP message: {}", event.getEventId());
        
        try {
            Object message = event.getMessage();
            ChannelHandlerContext ctx = event.getChannelContext();
            
            if (message instanceof DatagramPacket) {
                handleDatagramPacket(ctx, (DatagramPacket) message);
            } else {
                log.warn("Unexpected message type in UDP handler: {}", 
                        message != null ? message.getClass().getSimpleName() : "null");
            }
            
        } catch (Exception e) {
            log.error("Error processing UDP message: {}", event.getEventId(), e);
            handleError(event, e);
        }
    }

    /**
     * 处理DatagramPacket消息
     * 尝试使用注解驱动的处理器，如果未找到则使用默认处理器
     */
    private void handleDatagramPacket(@NotNull ChannelHandlerContext ctx, @NotNull DatagramPacket packet) {
        try {
            InetSocketAddress sender = packet.sender();
            ByteBuf content = packet.content();
            
            int messageLength = content.readableBytes();
            log.debug("Processing UDP DatagramPacket from {}: {} bytes", sender, messageLength);
            
            // 读取消息内容
            byte[] data = new byte[messageLength];
            content.getBytes(content.readerIndex(), data);
            
            // 处理消息内容
            String messageContent = new String(data, StandardCharsets.UTF_8);
            log.debug("UDP message content: {}", messageContent);
            
            // 查找注解驱动的处理器
            if (messageHandlerRegistry != null) {
                MessageHandlerRegistry.HandlerMethod handler = 
                    messageHandlerRegistry.findHandler("UDP", messageContent.trim());
                
                if (handler != null) {
                    try {
                        // 调用用户定义的处理器方法
                        handler.invoke(ctx, sender, messageContent, data);
                        return;
                    } catch (Exception e) {
                        log.error("Failed to invoke UDP handler", e);
                    }
                }
            }
            
            // 如果未找到注解驱动的处理器，使用默认处理器
            log.debug("No annotation-driven handler found for UDP message: {}, using default handler", messageContent);
            if (messageContent.trim().equals("ping")) {
                sendResponse(ctx, sender, "pong");
            } else if (messageContent.trim().equals("hello")) {
                sendResponse(ctx, sender, "Hello from UDP Server!");
            } else {
                sendResponse(ctx, sender, "Echo: " + messageContent);
            }
            
        } catch (Exception e) {
            log.error("Error processing UDP DatagramPacket", e);
            if (packet.sender() != null) {
                sendErrorResponse(ctx, packet.sender(), "Error processing UDP message: " + e.getMessage());
            }
        }
    }

    /**
     * 发送响应消息
     */
    private void sendResponse(@NotNull ChannelHandlerContext ctx, 
                             @NotNull InetSocketAddress recipient, 
                             @NotNull String response) {
        try {
            ByteBuf buffer = ctx.alloc().buffer();
            buffer.writeBytes(response.getBytes(StandardCharsets.UTF_8));
            DatagramPacket packet = new DatagramPacket(buffer, recipient);
            ctx.writeAndFlush(packet);
            log.debug("UDP response sent to {}: {}", recipient, response);
        } catch (Exception e) {
            log.error("Failed to send UDP response", e);
        }
    }

    /**
     * 发送错误响应消息
     */
    private void sendErrorResponse(@NotNull ChannelHandlerContext ctx, 
                                  @NotNull InetSocketAddress recipient, 
                                  @NotNull String errorMessage) {
        try {
            String errorMsg = "ERROR: " + errorMessage;
            ByteBuf buffer = ctx.alloc().buffer();
            buffer.writeBytes(errorMsg.getBytes(StandardCharsets.UTF_8));
            DatagramPacket packet = new DatagramPacket(buffer, recipient);
            ctx.writeAndFlush(packet);
        } catch (Exception e) {
            log.error("Failed to send error response to UDP client: {}", recipient, e);
        }
    }

    /**
     * 处理错误
     */
    private void handleError(@NotNull NetworkMessageEvent event, @NotNull Exception error) {
        log.error("Error handling UDP message: {}", event.getEventId(), error);
        
        try {
            ChannelHandlerContext ctx = event.getChannelContext();
            Object message = event.getMessage();
            
            if (ctx != null && message instanceof DatagramPacket) {
                DatagramPacket packet = (DatagramPacket) message;
                if (packet.sender() != null) {
                    sendErrorResponse(ctx, packet.sender(), "Internal server error");
                }
            }
        } catch (Exception e) {
            log.error("Failed to send error response to UDP client", e);
        }
    }
}
