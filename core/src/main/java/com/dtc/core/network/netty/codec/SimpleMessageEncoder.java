package com.dtc.core.network.netty.codec;

import com.dtc.api.annotations.NotNull;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 简单消息编码器
 * 处理简单的字节流消息
 * 
 * @author Network Service Template
 */
public class SimpleMessageEncoder extends MessageEncoder {

    private static final Logger log = LoggerFactory.getLogger(SimpleMessageEncoder.class);

    @Override
    protected void doEncode(@NotNull ChannelHandlerContext ctx, @NotNull Object msg, @NotNull ByteBuf out) {
        try {
            if (msg instanceof byte[]) {
                encodeByteArray((byte[]) msg, out);
            } else if (msg instanceof String) {
                encodeString((String) msg, out);
            } else {
                log.warn("Unsupported message type for simple encoding: {}", msg.getClass().getSimpleName());
            }

        } catch (Exception e) {
            log.error("Failed to encode simple message", e);
            throw new RuntimeException("Simple message encoding failed", e);
        }
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

    /**
     * 编码字符串
     */
    private void encodeString(@NotNull String message, @NotNull ByteBuf out) {
        byte[] data = message.getBytes();

        // 写入长度字段
        out.writeInt(data.length);

        // 写入数据
        out.writeBytes(data);

        log.debug("Encoded string message: {} bytes", data.length);
    }
}
