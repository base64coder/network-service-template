package com.dtc.core.netty.codec;

import com.dtc.api.annotations.NotNull;
import com.dtc.core.serialization.ProtobufSerializer;
import com.google.protobuf.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Protobuf 消息解码器 将字节流解码为 Protobuf 消息
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
        // 检查是否有足够的数据进行解码
        if (in.readableBytes() < 4) {
            return false; // 需要至少4字节的长度头
        }

        // 读取消息长度
        int messageLength = in.getInt(in.readerIndex());

        // 检查消息长度是否合理
        if (messageLength <= 0 || messageLength > MAX_MESSAGE_SIZE) {
            log.warn("Invalid message length: {}", messageLength);
            return false;
        }

        // 检查是否有完整的消息
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

            // 这里需要根据具体的消息类型进行反序列化
            // 由于我们不知道具体的消息类型，返回原始字节数组
            // 在实际使用中，应该根据协议头或其他信息确定消息类型
            log.debug("Decoded Protobuf message: {} bytes", messageLength);

            return messageData;

        } catch (Exception e) {
            log.error("Failed to decode Protobuf message", e);
            return null;
        }
    }

    /**
     * 解码为指定的 Protobuf 消息类型
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
