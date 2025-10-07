package com.dtc.core.netty.codec;

import com.dtc.api.annotations.NotNull;
import com.dtc.core.http.handler.HttpRequestDecoder;
import com.dtc.core.http.handler.HttpResponseEncoder;
import com.dtc.core.serialization.ProtobufSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * 编解码器工厂 根据协议类型创建相应的编解码器
 * 
 * @author Network Service Template
 */
@Singleton
public class CodecFactory {

    private static final Logger log = LoggerFactory.getLogger(CodecFactory.class);

    private final @NotNull ProtobufSerializer serializer;

    @Inject
    public CodecFactory(@NotNull ProtobufSerializer serializer) {
        this.serializer = serializer;
    }

    /**
     * 创建解码器
     * 
     * @param protocolType 协议类型
     * @return 解码器
     */
    @NotNull
    public ChannelHandler createDecoder(@NotNull String protocolType) {
        switch (protocolType.toLowerCase()) {
            case "http":
                return new HttpRequestDecoder();
            case "protobuf":
                return new ProtobufDecoder(serializer);
            case "custom":
                return new CustomMessageDecoder();
            case "tcp":
            case "simple":
            default:
                return new SimpleMessageDecoder();
        }
    }

    /**
     * 创建编码器
     * 
     * @param protocolType 协议类型
     * @return 编码器
     */
    @NotNull
    public ChannelHandler createEncoder(@NotNull String protocolType) {
        switch (protocolType.toLowerCase()) {
            case "http":
                return new HttpResponseEncoder();
            case "protobuf":
                return new ProtobufEncoder(serializer);
            case "custom":
                return new CustomMessageEncoder();
            case "tcp":
            case "simple":
            default:
                return new SimpleMessageEncoder();
        }
    }

    /**
     * 创建编解码器对
     * 
     * @param protocolType 协议类型
     * @return 编解码器对
     */
    @NotNull
    public CodecPair createCodecPair(@NotNull String protocolType) {
        return new CodecPair(createDecoder(protocolType), createEncoder(protocolType));
    }

    /**
     * 编解码器对
     */
    public static class CodecPair {
        private final @NotNull ChannelHandler decoder;
        private final @NotNull ChannelHandler encoder;

        public CodecPair(@NotNull ChannelHandler decoder, @NotNull ChannelHandler encoder) {
            this.decoder = decoder;
            this.encoder = encoder;
        }

        @NotNull
        public ChannelHandler getDecoder() {
            return decoder;
        }

        @NotNull
        public ChannelHandler getEncoder() {
            return encoder;
        }
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
                String message = new String(messageData, java.nio.charset.StandardCharsets.UTF_8);
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

                byte[] messageData = message.getBytes(java.nio.charset.StandardCharsets.UTF_8);

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
}
