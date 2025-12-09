package com.dtc.core.network.custom;

import com.dtc.api.annotations.NotNull;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.nio.charset.StandardCharsets;

/**
 * 默认自定义协议编解码器实现
 * 提供简单的长度前缀编解码器实现
 * 
 * @author Network Service Template
 */
@Singleton
public class DefaultCustomCodec extends CustomCodecFactory {

    private static final Logger log = LoggerFactory.getLogger(DefaultCustomCodec.class);

    public DefaultCustomCodec() {
        log.info("Creating Default Custom Codec instance");
    }

    @Override
    public void decode(@NotNull ChannelHandlerContext ctx, @NotNull ByteBuf in, @NotNull java.util.List<Object> out)
            throws Exception {
        if (in.readableBytes() < 4) {
            return; // 数据不完整，等待更多数据
        }

        // 读取消息长度（前4字节）
        int messageLength = in.readInt();
        if (in.readableBytes() < messageLength) {
            in.resetReaderIndex(); // 重置读取位置
            return; // 数据不完整，等待更多数据
        }

        // 读取消息数据
        byte[] messageBytes = new byte[messageLength];
        in.readBytes(messageBytes);
        String message = new String(messageBytes, StandardCharsets.UTF_8);

        log.debug("Decoded custom message: {}", message);
        out.add(message);
    }

    @Override
    public void encode(@NotNull ChannelHandlerContext ctx, @NotNull Object msg, @NotNull ByteBuf out) throws Exception {
        if (msg == null) {
            return;
        }

        String message = msg.toString();
        byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
        int messageLength = messageBytes.length;

        // 写入消息长度（4字节）
        out.writeInt(messageLength);
        // 写入消息数据
        out.writeBytes(messageBytes);

        log.debug("Encoded custom message: {}", message);
    }

    @Override
    @NotNull
    public String getCodecName() {
        return "DefaultCustomCodec";
    }

    @Override
    @NotNull
    public String getCodecVersion() {
        return "1.0.0";
    }

    @Override
    public boolean supports(@NotNull Class<?> messageType) {
        return String.class.isAssignableFrom(messageType);
    }
}
