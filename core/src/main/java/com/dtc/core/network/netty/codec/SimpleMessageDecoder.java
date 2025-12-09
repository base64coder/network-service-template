package com.dtc.core.network.netty.codec;

import com.dtc.api.annotations.NotNull;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 简单消息解码器
 * 处理简单的字节流消息
 * 
 * @author Network Service Template
 */
public class SimpleMessageDecoder extends MessageDecoder {

    private static final Logger log = LoggerFactory.getLogger(SimpleMessageDecoder.class);
    private static final int MAX_MESSAGE_SIZE = 64 * 1024; // 64KB

    @Override
    protected boolean isDecodable(@NotNull ByteBuf in) {
        // 检查是否有足够的数据
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

            log.debug("Decoded simple message: {} bytes", messageLength);
            return messageData;

        } catch (Exception e) {
            log.error("Failed to decode simple message", e);
            return null;
        }
    }
}
