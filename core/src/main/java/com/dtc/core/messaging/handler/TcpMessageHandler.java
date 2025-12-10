package com.dtc.core.messaging.handler;

import java.nio.charset.StandardCharsets;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import com.dtc.core.messaging.MessageHandlerRegistry;
import com.dtc.core.messaging.NetworkMessageEvent;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * TCP 消息处理器
 * 负责处理 TCP 协议类型的消息，从Disruptor队列接收并分发处理
 * 
 * @author Network Service Template
 */
@Singleton
public class TcpMessageHandler {

    private static final Logger log = LoggerFactory.getLogger(TcpMessageHandler.class);

    private final MessageHandlerRegistry messageHandlerRegistry;

    @Inject
    public TcpMessageHandler(@Nullable MessageHandlerRegistry messageHandlerRegistry) {
        this.messageHandlerRegistry = messageHandlerRegistry;
    }

    /**
     * 处理 TCP 消息
     */
    public void handleMessage(@NotNull NetworkMessageEvent event) {
        log.debug("🔍 Processing TCP message: {}", event.getEventId());
        
        try {
            Object message = event.getMessage();
            ChannelHandlerContext ctx = event.getChannelContext();
            
            log.debug("Processing TCP message: {} bytes", 
                    message instanceof ByteBuf ? 
                    ((ByteBuf) message).readableBytes() : 
                    message instanceof byte[] ? ((byte[]) message).length : "unknown");
            
            // 处理不同类型的TCP消息
            if (message instanceof ByteBuf) {
                handleByteBufMessage(ctx, (ByteBuf) message);
            } else if (message instanceof byte[]) {
                handleByteArrayMessage(ctx, (byte[]) message);
            } else {
                log.warn("⚠️  Unexpected message type in TCP handler: {}", 
                        message != null ? message.getClass().getSimpleName() : "null");
            }
            
        } catch (Exception e) {
            log.error("❌ Error processing TCP message: {}", event.getEventId(), e);
            handleError(event, e);
        }
    }

    /**
     * 处理ByteBuf消息
     */
    private void handleByteBufMessage(@NotNull ChannelHandlerContext ctx, @NotNull ByteBuf message) {
        try {
            int messageLength = message.readableBytes();
            log.debug("Processing TCP ByteBuf message: {} bytes", messageLength);
            
            // 检查消息长度
            if (messageLength < 4) {
                log.warn("⚠️  TCP message too short: {} bytes", messageLength);
                return;
            }
            
            // 读取消息头部，前4字节为消息体长度
            int headerLength = message.getInt(0);
            log.debug("TCP message header length: {}", headerLength);
            
            // 检查消息是否完整
            if (messageLength < headerLength + 4) {
                log.warn("⚠️  TCP message incomplete: expected {} bytes, got {} bytes", 
                        headerLength + 4, messageLength);
                return;
            }
            
            // 读取消息体
            ByteBuf messageBody = message.slice(4, headerLength);
            processTcpMessage(ctx, messageBody);
            
        } catch (Exception e) {
            log.error("❌ Error processing TCP ByteBuf message", e);
            sendErrorResponse(ctx, "Error processing TCP message: " + e.getMessage());
        }
    }

    /**
     * 处理字节数组消息
     */
    private void handleByteArrayMessage(@NotNull ChannelHandlerContext ctx, @NotNull byte[] message) {
        try {
            log.debug("Processing TCP byte array message: {} bytes", message.length);
            
            // 将字节数组转换为ByteBuf处理
            ByteBuf buffer = ctx.alloc().buffer(message.length);
            buffer.writeBytes(message);
            handleByteBufMessage(ctx, buffer);
            buffer.release();
            
        } catch (Exception e) {
            log.error("❌ Error processing TCP byte array message", e);
            sendErrorResponse(ctx, "Error processing TCP message: " + e.getMessage());
        }
    }

    /**
     * 处理TCP消息体
     */
    private void processTcpMessage(@NotNull ChannelHandlerContext ctx, @NotNull ByteBuf messageBody) {
        try {
            // 读取消息类型，第1字节为消息类型
            if (messageBody.readableBytes() < 1) {
                log.warn("⚠️  TCP message body too short");
                return;
            }
            
            byte messageType = messageBody.readByte();
            log.debug("TCP message type: {}", messageType);
            
            // 根据消息类型处理
            switch (messageType) {
                case 0x01: // 心跳消息
                    handleHeartbeatMessage(ctx, messageBody);
                    break;
                case 0x02: // 数据消息
                    handleDataMessage(ctx, messageBody);
                    break;
                case 0x03: // 控制消息
                    handleControlMessage(ctx, messageBody);
                    break;
                case 0x04: // 错误消息
                    handleErrorMessage(ctx, messageBody);
                    break;
                default:
                    log.warn("⚠️  Unknown TCP message type: {}", messageType);
                    handleUnknownMessage(ctx, messageBody);
                    break;
            }
            
        } catch (Exception e) {
            log.error("❌ Error processing TCP message body", e);
            sendErrorResponse(ctx, "Error processing TCP message: " + e.getMessage());
        }
    }

    // ========== TCP消息类型处理方法 ==========

    /**
     * 处理心跳消息
     */
    private void handleHeartbeatMessage(@NotNull ChannelHandlerContext ctx, @NotNull ByteBuf messageBody) {
        log.debug("Processing TCP heartbeat message");
        
        try {
            // 发送心跳响应
            ByteBuf response = ctx.alloc().buffer(5);
            response.writeInt(1); // 消息体长度
            response.writeByte(0x01); // 心跳响应类型
            ctx.writeAndFlush(response);
            
            log.debug("✅ TCP heartbeat response sent");
            
        } catch (Exception e) {
            log.error("❌ Error processing TCP heartbeat message", e);
        }
    }

    /**
     * 处理数据消息
     */
    private void handleDataMessage(@NotNull ChannelHandlerContext ctx, @NotNull ByteBuf messageBody) {
        log.debug("Processing TCP data message");
        
        try {
            // 读取数据内容
            byte[] data = new byte[messageBody.readableBytes()];
            messageBody.readBytes(data);
            
            // 处理数据内容
            String dataString = new String(data, StandardCharsets.UTF_8);
            log.debug("TCP data message content: {}", dataString);
            
            // 查找注解驱动的处理器
            if (messageHandlerRegistry != null) {
                MessageHandlerRegistry.HandlerMethod handler = 
                    messageHandlerRegistry.findHandler("TCP", dataString.trim());
                
                if (handler != null) {
                    try {
                        // 调用用户定义的处理器方法
                        handler.invoke(ctx, dataString);
                        return;
                    } catch (Exception e) {
                        log.error("Failed to invoke TCP handler", e);
                    }
                }
            }
            
            // 如果未找到注解驱动的处理器，使用默认处理器
            log.debug("No annotation-driven handler found for TCP message: {}, using default handler", dataString);
            String responseData = "Echo: " + dataString;
            byte[] responseBytes = responseData.getBytes(StandardCharsets.UTF_8);
            
            ByteBuf response = ctx.alloc().buffer(4 + 1 + responseBytes.length);
            response.writeInt(1 + responseBytes.length); // 消息体长度
            response.writeByte(0x02); // 数据响应类型
            response.writeBytes(responseBytes);
            ctx.writeAndFlush(response);
            
            log.debug("✅ TCP data response sent");
            
        } catch (Exception e) {
            log.error("❌ Error processing TCP data message", e);
        }
    }

    /**
     * 处理控制消息
     */
    private void handleControlMessage(@NotNull ChannelHandlerContext ctx, @NotNull ByteBuf messageBody) {
        log.debug("Processing TCP control message");
        
        try {
            // 处理控制消息逻辑
            // 可以通过路由管理器来处理控制逻辑
            
            // 发送控制响应
            ByteBuf response = ctx.alloc().buffer(5);
            response.writeInt(1); // 消息体长度
            response.writeByte(0x03); // 控制响应类型
            ctx.writeAndFlush(response);
            
            log.debug("✅ TCP control response sent");
            
        } catch (Exception e) {
            log.error("❌ Error processing TCP control message", e);
        }
    }

    /**
     * 处理错误消息
     */
    private void handleErrorMessage(@NotNull ChannelHandlerContext ctx, @NotNull ByteBuf messageBody) {
        log.debug("Processing TCP error message");
        
        try {
            // 读取错误信息
            byte[] errorData = new byte[messageBody.readableBytes()];
            messageBody.readBytes(errorData);
            String errorMessage = new String(errorData, java.nio.charset.StandardCharsets.UTF_8);
            
            log.error("TCP error message: {}", errorMessage);
            
            // 发送错误确认
            ByteBuf response = ctx.alloc().buffer(5);
            response.writeInt(1); // 消息体长度
            response.writeByte(0x04); // 错误确认类型
            ctx.writeAndFlush(response);
            
        } catch (Exception e) {
            log.error("❌ Error processing TCP error message", e);
        }
    }

    /**
     * 处理未知消息
     */
    private void handleUnknownMessage(@NotNull ChannelHandlerContext ctx, @NotNull ByteBuf messageBody) {
        log.debug("Processing TCP unknown message");
        
        try {
            // 发送未知消息响应
            ByteBuf response = ctx.alloc().buffer(5);
            response.writeInt(1); // 消息体长度
            response.writeByte(0xFF); // 未知消息响应类型
            ctx.writeAndFlush(response);
            
        } catch (Exception e) {
            log.error("❌ Error processing TCP unknown message", e);
        }
    }

    /**
     * 发送错误响应
     */
    private void sendErrorResponse(@NotNull ChannelHandlerContext ctx, @NotNull String errorMessage) {
        try {
            // TCP错误响应处理
            String errorMsg = "ERROR: " + errorMessage;
            byte[] errorBytes = errorMsg.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            
            ByteBuf response = ctx.alloc().buffer(4 + 1 + errorBytes.length);
            response.writeInt(1 + errorBytes.length); // 消息体长度
            response.writeByte(0x04); // 错误消息类型
            response.writeBytes(errorBytes);
            ctx.writeAndFlush(response);
            
        } catch (Exception e) {
            log.error("❌ Failed to send error response to TCP client: {}", ctx.channel().remoteAddress(), e);
        }
    }

    /**
     * 处理错误
     */
    private void handleError(@NotNull NetworkMessageEvent event, @NotNull Exception error) {
        log.error("🔴 Error handling TCP message: {}", event.getEventId(), error);
        
        try {
            ChannelHandlerContext ctx = event.getChannelContext();
            if (ctx != null && ctx.channel().isActive()) {
                sendErrorResponse(ctx, "Internal server error");
            }
        } catch (Exception e) {
            log.error("❌ Failed to send error response to TCP client", e);
        }
    }
}
