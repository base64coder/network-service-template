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
 * Protobuf 消息解码器
 * 将字节流解码为Protobuf消息
 * 
 * @author Network Service Template
 */
@Singleton
public class ProtobufDecoder extends MessageDecoder {

    private static final Logger log = LoggerFactory.getLogger(ProtobufDecoder.class);
    private static final int MAX_MESSAGE_SIZE = 1024 * 1024; // 1MB

    private final @NotNull ProtobufSerializer serializer;

    @Inject
    public ProtobufDecoder(@NotNull ProtobufSerializer serializer) {
        this.serializer = serializer;
    }

    @Override
    protected boolean isDecodable(@NotNull ByteBuf in) {
        // 检查是否有足够的数据可以解码
        if (in.readableBytes() < 4) {
            return false; // 数据不完整，至少需要4字节的长度字段
        }

        // 读取消息长度
        int messageLength = in.getInt(in.readerIndex());

        // 检查消息长度是否合法
        if (messageLength <= 0 || messageLength > MAX_MESSAGE_SIZE) {
            log.warn("Invalid message length: {}", messageLength);
            return false;
        }

        // 检查是否有足够的数据
        return in.readableBytes() >= 4 + messageLength;
    }

    @Override
    protected Object doDecode(@NotNull ChannelHandlerContext ctx, @NotNull ByteBuf in) {
        try {
            // 读取消息长度
            int messageLength = in.readInt();

            // 读取消息数据
            byte[] messageData = new byte[messageLength];
            in.readBytes(messageData);

            // 可以通过路由管理器实现消息类型解析和反序列化逻辑
            // 注意：这里需要知道消息类型才能正确反序列化
            // 通常可以通过协议头或消息类型字段来确定消息类型，然后调用serializer.deserialize
            // 为了简化，这里直接返回字节数组，由上层处理
            log.debug("Decoded Protobuf message: {} bytes", messageLength);

            return messageData;

        } catch (Exception e) {
            log.error("Failed to decode Protobuf message", e);
            return null;
        }
    }

    /**
     * 解码为指定类型的 Protobuf 消息
     * 
     * @param data         消息数据
     * @param messageClass 消息类型
     * @param <T>          消息类型
     * @return 解码后的消息
     */
    @NotNull
    public <T extends Message> T decodeMessage(@NotNull byte[] data, @NotNull Class<T> messageClass) {
        return serializer.deserialize(data, messageClass);
    }
}
