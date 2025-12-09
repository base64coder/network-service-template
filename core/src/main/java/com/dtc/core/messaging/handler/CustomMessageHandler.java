package com.dtc.core.messaging.handler;

import java.nio.charset.StandardCharsets;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import com.dtc.core.messaging.MessageHandlerRegistry;
import com.dtc.core.messaging.NetworkMessageEvent;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * 自定义协议消息处理器
 * 专门处理自定义协议的消息
 * 
 * @author Network Service Template
 */
@Singleton
public class CustomMessageHandler {

    private static final Logger log = LoggerFactory.getLogger(CustomMessageHandler.class);

    private final MessageHandlerRegistry messageHandlerRegistry;

    @Inject
    public CustomMessageHandler(@Nullable MessageHandlerRegistry messageHandlerRegistry) {
        this.messageHandlerRegistry = messageHandlerRegistry;
    }

    /**
     * 处理自定义协议消息
     */
    public void handleMessage(@NotNull NetworkMessageEvent event) {
        log.debug("🔍 Processing Custom message: {}", event.getEventId());

        try {
            Object message = event.getMessage();
            ChannelHandlerContext ctx = event.getChannelContext();

            log.debug("Processing Custom message: {}", message != null ? message.getClass().getSimpleName() : "null");

            // 处理不同类型的自定义消息
            if (message instanceof ByteBuf) {
                handleByteBufMessage(ctx, (ByteBuf) message);
            } else if (message instanceof byte[]) {
                handleByteArrayMessage(ctx, (byte[]) message);
            } else if (message instanceof String) {
                handleStringMessage(ctx, (String) message);
            } else {
                log.warn("⚠️  Unexpected message type in Custom handler: {}",
                        message != null ? message.getClass().getSimpleName() : "null");
            }

        } catch (Exception e) {
            log.error("❌ Error processing Custom message: {}", event.getEventId(), e);
            handleError(event, e);
        }
    }

    /**
     * 处理ByteBuf消息
     */
    private void handleByteBufMessage(@NotNull ChannelHandlerContext ctx, @NotNull ByteBuf message) {
        try {
            int messageLength = message.readableBytes();
            log.debug("Processing Custom ByteBuf message: {} bytes", messageLength);

            // 解析自定义协议消息
            CustomProtocolMessage protocolMessage = parseCustomProtocolMessage(message);
            
            // 尝试使用注解驱动的处理器
            if (messageHandlerRegistry != null) {
                // 使用消息类型作为路由
                MessageHandlerRegistry.HandlerMethod handler = 
                    messageHandlerRegistry.findHandler("Custom", protocolMessage.getType());
                
                if (handler != null) {
                    try {
                        // 调用用户定义的处理器方法
                        handler.invoke(ctx, protocolMessage.getData());
                        return;
                    } catch (Exception e) {
                        log.error("Failed to invoke Custom handler", e);
                    }
                }
            }
            
            // 如果没有找到注解驱动的处理器，使用默认处理
            log.debug("No annotation-driven handler found for Custom message type: {}, using default handler", protocolMessage.getType());
            handleCustomProtocolMessageByType(ctx, protocolMessage);

        } catch (Exception e) {
            log.error("❌ Error processing Custom ByteBuf message", e);
            sendErrorResponse(ctx, "Error processing custom message: " + e.getMessage());
        }
    }

    /**
     * 处理字节数组消息
     */
    private void handleByteArrayMessage(@NotNull ChannelHandlerContext ctx, @NotNull byte[] message) {
        try {
            log.debug("Processing Custom byte array message: {} bytes", message.length);

            // 将字节数组转换为ByteBuf处理
            ByteBuf buffer = ctx.alloc().buffer(message.length);
            buffer.writeBytes(message);
            handleByteBufMessage(ctx, buffer);
            buffer.release();

        } catch (Exception e) {
            log.error("❌ Error processing Custom byte array message", e);
            sendErrorResponse(ctx, "Error processing custom message: " + e.getMessage());
        }
    }

    /**
     * 处理字符串消息
     * 优先使用注解驱动的处理器，如果没有找到则使用默认处理
     */
    private void handleStringMessage(@NotNull ChannelHandlerContext ctx, @NotNull String message) {
        try {
            log.debug("Processing Custom string message: {}", message);

            // 尝试使用注解驱动的处理器
            if (messageHandlerRegistry != null) {
                MessageHandlerRegistry.HandlerMethod handler = 
                    messageHandlerRegistry.findHandler("Custom", message.trim());
                
                if (handler != null) {
                    try {
                        // 调用用户定义的处理器方法
                        handler.invoke(ctx, message);
                        return;
                    } catch (Exception e) {
                        log.error("Failed to invoke Custom handler", e);
                    }
                }
            }
            
            // 如果没有找到注解驱动的处理器，使用默认处理
            log.debug("No annotation-driven handler found for Custom message: {}, using default handler", message);
            // 解析JSON格式的自定义消息
            CustomProtocolMessage protocolMessage = parseJsonMessage(message);

            // 处理消息
            handleCustomProtocolMessage(ctx, protocolMessage);

        } catch (Exception e) {
            log.error("❌ Error processing Custom string message", e);
            sendErrorResponse(ctx, "Error processing custom message: " + e.getMessage());
        }
    }

    /**
     * 解析自定义协议消息
     */
    @NotNull
    private CustomProtocolMessage parseCustomProtocolMessage(@NotNull ByteBuf message) {
        // 假设自定义协议格式：
        // [4字节长度][1字节类型][N字节数据]

        if (message.readableBytes() < 5) {
            throw new IllegalArgumentException("Message too short");
        }

        int length = message.readInt();
        byte type = message.readByte();

        byte[] data = new byte[length - 1];
        message.readBytes(data);

        String typeString = getCustomMessageTypeName(type);
        String dataString = new String(data, java.nio.charset.StandardCharsets.UTF_8);

        return new CustomProtocolMessage(typeString, dataString);
    }

    /**
     * 解析JSON消息
     */
    @NotNull
    private CustomProtocolMessage parseJsonMessage(@NotNull String jsonMessage) {
        try {
            // 简单的JSON解析（实际项目中可以使用Jackson等库）
            // 假设JSON格式：{"type": "HELLO", "data": "Hello World"}

            if (jsonMessage.contains("\"type\":\"HELLO\"")) {
                return new CustomProtocolMessage("HELLO", "Hello from client");
            } else if (jsonMessage.contains("\"type\":\"DATA\"")) {
                return new CustomProtocolMessage("DATA", "Data from client");
            } else {
                return new CustomProtocolMessage("UNKNOWN", jsonMessage);
            }

        } catch (Exception e) {
            log.error("❌ Error parsing JSON message", e);
            return new CustomProtocolMessage("ERROR", "Invalid JSON format");
        }
    }

    // ========== 自定义协议消息处理方法 ==========

    /**
     * 处理HELLO消息
     */
    private void handleHelloMessage(@NotNull ChannelHandlerContext ctx, @NotNull CustomProtocolMessage message) {
        log.debug("Processing Custom HELLO message: {}", message.getData());

        try {
            // 发送HELLO响应
            String response = "Hello from server!";
            sendCustomResponse(ctx, "HELLO", response);

            log.debug("✅ Custom HELLO response sent");

        } catch (Exception e) {
            log.error("❌ Error processing Custom HELLO message", e);
        }
    }

    /**
     * 处理DATA消息
     */
    private void handleDataMessage(@NotNull ChannelHandlerContext ctx, @NotNull CustomProtocolMessage message) {
        log.debug("Processing Custom DATA message: {}", message.getData());

        try {
            // 处理数据
            String processedData = "Processed: " + message.getData();
            sendCustomResponse(ctx, "DATA", processedData);

            log.debug("✅ Custom DATA response sent");

        } catch (Exception e) {
            log.error("❌ Error processing Custom DATA message", e);
        }
    }

    /**
     * 处理COMMAND消息
     */
    private void handleCommandMessage(@NotNull ChannelHandlerContext ctx, @NotNull CustomProtocolMessage message) {
        log.debug("Processing Custom COMMAND message: {}", message.getData());

        try {
            // 处理命令
            String commandResult = "Command executed: " + message.getData();
            sendCustomResponse(ctx, "RESPONSE", commandResult);

            log.debug("✅ Custom COMMAND response sent");

        } catch (Exception e) {
            log.error("❌ Error processing Custom COMMAND message", e);
        }
    }

    /**
     * 处理RESPONSE消息
     */
    private void handleResponseMessage(@NotNull ChannelHandlerContext ctx, @NotNull CustomProtocolMessage message) {
        log.debug("Processing Custom RESPONSE message: {}", message.getData());

        try {
            // 处理响应消息
            log.debug("Received response: {}", message.getData());

        } catch (Exception e) {
            log.error("❌ Error processing Custom RESPONSE message", e);
        }
    }

    /**
     * 处理未知消息
     */
    private void handleUnknownMessage(@NotNull ChannelHandlerContext ctx, @NotNull CustomProtocolMessage message) {
        log.debug("Processing Custom unknown message: {}", message.getData());

        try {
            // 发送未知消息响应
            sendCustomResponse(ctx, "ERROR", "Unknown message type: " + message.getType());

        } catch (Exception e) {
            log.error("❌ Error processing Custom unknown message", e);
        }
    }

    /**
     * 根据消息类型处理自定义协议消息（默认处理逻辑）
     */
    private void handleCustomProtocolMessageByType(@NotNull ChannelHandlerContext ctx,
            @NotNull CustomProtocolMessage message) {
        // 根据消息类型调用相应的处理方法
        switch (message.getType()) {
            case "HELLO":
                handleHelloMessage(ctx, message);
                break;
            case "DATA":
                handleDataMessage(ctx, message);
                break;
            case "COMMAND":
                handleCommandMessage(ctx, message);
                break;
            case "RESPONSE":
                handleResponseMessage(ctx, message);
                break;
            default:
                handleUnknownMessage(ctx, message);
                break;
        }
    }
    
    /**
     * 处理自定义协议消息（用于字符串消息）
     */
    private void handleCustomProtocolMessage(@NotNull ChannelHandlerContext ctx,
            @NotNull CustomProtocolMessage message) {
        handleCustomProtocolMessageByType(ctx, message);
    }

    /**
     * 发送自定义响应
     */
    private void sendCustomResponse(@NotNull ChannelHandlerContext ctx, @NotNull String type, @NotNull String data) {
        try {
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            byte typeByte = getCustomMessageTypeByte(type);

            ByteBuf response = ctx.alloc().buffer(4 + 1 + dataBytes.length);
            response.writeInt(1 + dataBytes.length); // 消息长度
            response.writeByte(typeByte); // 消息类型
            response.writeBytes(dataBytes); // 消息数据
            ctx.writeAndFlush(response);

        } catch (Exception e) {
            log.error("❌ Error sending custom response", e);
        }
    }

    /**
     * 获取自定义消息类型名称
     */
    @NotNull
    private String getCustomMessageTypeName(byte type) {
        switch (type) {
            case 0x01:
                return "HELLO";
            case 0x02:
                return "DATA";
            case 0x03:
                return "COMMAND";
            case 0x04:
                return "RESPONSE";
            case 0x05:
                return "ERROR";
            default:
                return "UNKNOWN";
        }
    }

    /**
     * 获取自定义消息类型字节
     */
    private byte getCustomMessageTypeByte(@NotNull String type) {
        switch (type) {
            case "HELLO":
                return 0x01;
            case "DATA":
                return 0x02;
            case "COMMAND":
                return 0x03;
            case "RESPONSE":
                return 0x04;
            case "ERROR":
                return 0x05;
            default:
                return 0x00;
        }
    }

    /**
     * 发送错误响应
     */
    private void sendErrorResponse(@NotNull ChannelHandlerContext ctx, @NotNull String errorMessage) {
        try {
            sendCustomResponse(ctx, "ERROR", errorMessage);
        } catch (Exception e) {
            log.error("❌ Failed to send error response to Custom client: {}", ctx.channel().remoteAddress(), e);
        }
    }

    /**
     * 处理错误
     */
    private void handleError(@NotNull NetworkMessageEvent event, @NotNull Exception error) {
        log.error("🔴 Error handling Custom message: {}", event.getEventId(), error);

        try {
            ChannelHandlerContext ctx = event.getChannelContext();
            if (ctx != null && ctx.channel().isActive()) {
                sendErrorResponse(ctx, "Internal server error");
            }
        } catch (Exception e) {
            log.error("❌ Failed to send error response to Custom client", e);
        }
    }

    /**
     * 自定义协议消息类
     */
    private static class CustomProtocolMessage {
        private final String type;
        private final String data;

        public CustomProtocolMessage(@NotNull String type, @NotNull String data) {
            this.type = type;
            this.data = data;
        }

        @NotNull
        public String getType() {
            return type;
        }

        @NotNull
        public String getData() {
            return data;
        }
    }
}
