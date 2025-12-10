package com.dtc.core.network.netty.codec;

import com.dtc.api.annotations.NotNull;
import com.dtc.core.serialization.ProtobufSerializer;
import com.google.protobuf.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * Protobuf 消息编码器
 * 将Protobuf消息编码为字节流
 * 
 * @author Network Service Template
 */
@Singleton
public class ProtobufEncoder extends MessageEncoder {

    private static final Logger log = LoggerFactory.getLogger(ProtobufEncoder.class);

    private final @NotNull ProtobufSerializer serializer;

    @Inject
    public ProtobufEncoder(@NotNull ProtobufSerializer serializer) {
        this.serializer = serializer;
    }

    @Override
    protected void doEncode(@NotNull ChannelHandlerContext ctx, @NotNull Object msg, @NotNull ByteBuf out) {
        try {
            if (msg instanceof Message) {
                encodeProtobufMessage((Message) msg, out);
            } else if (msg instanceof byte[]) {
                encodeByteArray((byte[]) msg, out);
            } else {
                log.warn("Unsupported message type for Protobuf encoding: {}", msg.getClass().getSimpleName());
            }

        } catch (Exception e) {
            log.error("Failed to encode message", e);
            throw new RuntimeException("Protobuf encoding failed", e);
        }
    }

    /**
     * 编码 Protobuf 消息
     */
    private void encodeProtobufMessage(@NotNull Message message, @NotNull ByteBuf out) {
        // 序列化消息
        byte[] messageData = serializer.serialize(message);

        // 写入长度字段
        out.writeInt(messageData.length);

        // 写入消息数据
        out.writeBytes(messageData);

        log.debug("Encoded Protobuf message: {} bytes", messageData.length);
    }

    /**
     * 编码字节数组
     */
    private void encodeByteArray(@NotNull byte[] data, @NotNull ByteBuf out) {
        // 写入长度字段
        out.writeInt(data.length);

        // 写入数据
        out.writeBytes(data);

        log.debug("Encoded byte array: {} bytes", data.length);
    }
}
