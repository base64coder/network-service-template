package com.dtc.core.network.netty.codec;

import com.dtc.api.annotations.NotNull;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 消息编码器基类
 * 提供通用的消息编码基础实现
 * 
 * @author Network Service Template
 */
public abstract class MessageEncoder extends MessageToByteEncoder<Object> {

    private static final Logger log = LoggerFactory.getLogger(MessageEncoder.class);

    @Override
    protected void encode(@NotNull ChannelHandlerContext ctx, @NotNull Object msg, @NotNull ByteBuf out)
            throws Exception {
        try {
            // 调用子类实现的编码逻辑
            doEncode(ctx, msg, out);
            log.debug("Message encoded successfully: {} bytes", out.readableBytes());

        } catch (Exception e) {
            log.error("Failed to encode message: {}", msg.getClass().getSimpleName(), e);
            // 可以通过路由管理器实现错误处理逻辑
            handleEncodeError(ctx, msg, e);
        }
    }

    /**
     * 调用子类实现的编码逻辑
     * 
     * @param ctx 通道上下文
     * @param msg 要编码的消息
     * @param out 输出缓冲区
     */
    protected abstract void doEncode(@NotNull ChannelHandlerContext ctx, @NotNull Object msg, @NotNull ByteBuf out);

    /**
     * 处理编码错误
     * 
     * @param ctx   通道上下文
     * @param msg   原始消息
     * @param error 错误信息
     */
    protected void handleEncodeError(@NotNull ChannelHandlerContext ctx, @NotNull Object msg,
            @NotNull Exception error) {
        log.warn("Encode error occurred for message: {}", msg.getClass().getSimpleName());
        // 可以通过路由管理器关闭连接或发送错误响应
    }
}
