package com.dtc.custom;

import com.dtc.api.ExtensionMain;
import com.dtc.api.MessageHandler;
import com.dtc.api.ProtocolExtension;
import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import com.dtc.api.parameter.ExtensionStartInput;
import com.dtc.api.parameter.ExtensionStartOutput;
import com.dtc.api.parameter.ExtensionStopInput;
import com.dtc.api.parameter.ExtensionStopOutput;
import com.dtc.core.netty.codec.CodecFactory;
import com.dtc.core.netty.codec.MessageDecoder;
import com.dtc.core.netty.codec.MessageEncoder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 自定义协议扩展示例
 * 展示如何通过扩展实现自定义编解码器
 * 
 * @author Network Service Template
 */
@Singleton
public class CustomProtocolExtension implements ExtensionMain, ProtocolExtension {

    private static final Logger log = LoggerFactory.getLogger(CustomProtocolExtension.class);

    private final @NotNull CodecFactory codecFactory;
    private volatile boolean started = false;
    private final AtomicLong messageCount = new AtomicLong(0);

    @Inject
    public CustomProtocolExtension(@NotNull CodecFactory codecFactory) {
        this.codecFactory = codecFactory;
    }

    /**
     * 创建自定义解码器
     */
    @NotNull
    public ChannelHandler createCustomDecoder() {
        return new CustomMessageDecoder();
    }

    /**
     * 创建自定义编码器
     */
    @NotNull
    public ChannelHandler createCustomEncoder() {
        return new CustomMessageEncoder();
    }

    /**
     * 自定义消息解码器
     */
    public static class CustomMessageDecoder extends MessageDecoder {

        @Override
        protected boolean isDecodable(@NotNull ByteBuf in) {
            // 检查是否有足够的数据进行解码
            return in.readableBytes() >= 4; // 至少需要4字节的头部
        }

        @Override
        protected Object doDecode(@NotNull ChannelHandlerContext ctx, @NotNull ByteBuf in) {
            try {
                // 读取消息长度
                int messageLength = in.readInt();
                
                // 检查消息长度
                if (messageLength <= 0 || messageLength > 64 * 1024) {
                    log.warn("Invalid message length: {}", messageLength);
                    return null;
                }

                // 检查是否有完整的消息
                if (in.readableBytes() < messageLength) {
                    return null; // 需要更多数据
                }

                // 读取消息内容
                byte[] messageData = new byte[messageLength];
                in.readBytes(messageData);

                // 解析自定义协议
                String message = new String(messageData, StandardCharsets.UTF_8);
                log.debug("Decoded custom message: {}", message);

                return message;

            } catch (Exception e) {
                log.error("Failed to decode custom message", e);
                return null;
            }
        }
    }

    /**
     * 自定义消息编码器
     */
    public static class CustomMessageEncoder extends MessageEncoder {

        @Override
        protected void doEncode(@NotNull ChannelHandlerContext ctx, @NotNull Object msg, @NotNull ByteBuf out) {
            try {
                String message;
                if (msg instanceof String) {
                    message = (String) msg;
                } else {
                    message = msg.toString();
                }

                byte[] messageData = message.getBytes(StandardCharsets.UTF_8);
                
                // 写入消息长度
                out.writeInt(messageData.length);
                
                // 写入消息内容
                out.writeBytes(messageData);

                log.debug("Encoded custom message: {} bytes", messageData.length);

            } catch (Exception e) {
                log.error("Failed to encode custom message", e);
                throw new RuntimeException("Custom message encoding failed", e);
            }
        }
    }

    // ========== ExtensionMain 接口实现 ==========

    @Override
    public void extensionStart(@NotNull ExtensionStartInput input, @NotNull ExtensionStartOutput output) {
        log.info("Starting Custom Protocol Extension v{}", input.getExtensionVersion());

        try {
            // 初始化自定义协议处理器
            initializeCustomHandler();

            started = true;
            log.info("Custom Protocol Extension started successfully");
        } catch (Exception e) {
            log.error("Failed to start Custom Protocol Extension", e);
            output.preventStartup("Failed to initialize custom handler: " + e.getMessage());
        }
    }

    @Override
    public void extensionStop(@NotNull ExtensionStopInput input, @NotNull ExtensionStopOutput output) {
        log.info("Stopping Custom Protocol Extension v{}", input.getExtensionVersion());

        try {
            // 清理自定义协议处理器
            cleanupCustomHandler();

            started = false;
            log.info("Custom Protocol Extension stopped successfully");
        } catch (Exception e) {
            log.error("Failed to stop Custom Protocol Extension", e);
            output.preventStop("Failed to cleanup custom handler: " + e.getMessage());
        }
    }

    // ========== ProtocolExtension 接口实现 ==========

    @Override
    @NotNull
    public String getProtocolName() {
        return "CustomProtocol";
    }

    @Override
    @NotNull
    public String getProtocolVersion() {
        return "1.0.0";
    }

    @Override
    public int getDefaultPort() {
        return 9999;
    }

    @Override
    public void onConnect(@NotNull ChannelHandlerContext ctx, @NotNull String clientId) {
        log.info("Client {} connected to custom protocol", clientId);
    }

    @Override
    public void onDisconnect(@NotNull ChannelHandlerContext ctx, @NotNull String clientId) {
        log.info("Client {} disconnected from custom protocol", clientId);
    }

    @Override
    public void onMessage(@NotNull ChannelHandlerContext ctx, @NotNull Object message) {
        messageCount.incrementAndGet();
        log.debug("Received custom protocol message: {}", message);
        
        // 处理自定义协议消息
        if (message instanceof String) {
            String msg = (String) message;
            log.info("Processing custom message: {}", msg);
        }
    }

    @Override
    public void onException(@NotNull ChannelHandlerContext ctx, @NotNull Throwable cause) {
        log.error("Custom protocol exception occurred", cause);
    }

    @Override
    @Nullable
    public MessageHandler getMessageHandler() {
        return new CustomMessageHandler();
    }

    // ========== 私有方法 ==========

    private void initializeCustomHandler() {
        log.debug("Initializing custom protocol handler");
        // 这里可以添加自定义协议的初始化逻辑
    }

    private void cleanupCustomHandler() {
        log.debug("Cleaning up custom protocol handler");
        // 这里可以添加自定义协议的清理逻辑
    }

    /**
     * 自定义消息处理器
     */
    private class CustomMessageHandler implements MessageHandler {

        @Override
        @Nullable
        public Object handleMessage(@NotNull ChannelHandlerContext ctx, @NotNull Object message) {
            // 处理接收到的消息
            onMessage(ctx, message);
            return null; // 继续处理链
        }

        @Override
        @Nullable
        public Object handleOutboundMessage(@NotNull ChannelHandlerContext ctx, @NotNull Object message) {
            // 处理发送的消息
            return message; // 直接发送
        }

        @Override
        public int getPriority() {
            return 50; // 中等优先级
        }

        @Override
        public boolean supports(@NotNull Class<?> messageType) {
            return String.class.isAssignableFrom(messageType);
        }
    }
}
