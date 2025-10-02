package com.dtc.core.netty.codec;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 消息解码器基类 提供通用的消息解码功能
 * 
 * @author Network Service Template
 */
public abstract class MessageDecoder extends ByteToMessageDecoder {

    private static final Logger log = LoggerFactory.getLogger(MessageDecoder.class);

    @Override
    protected void decode(@NotNull ChannelHandlerContext ctx, @NotNull ByteBuf in, @NotNull List<Object> out)
            throws Exception {
        try {
            // 检查是否有足够的数据进行解码
            if (!isDecodable(in)) {
                return;
            }

            // 执行具体的解码逻辑
            Object message = doDecode(ctx, in);
            if (message != null) {
                out.add(message);
                log.debug("Message decoded successfully: {} bytes", in.readableBytes());
            }

        } catch (Exception e) {
            log.error("Failed to decode message", e);
            // 可以在这里添加错误处理逻辑
            handleDecodeError(ctx, e);
        }
    }

    /**
     * 检查是否可以进行解码
     * 
     * @param in 输入缓冲区
     * @return 是否可以解码
     */
    protected abstract boolean isDecodable(@NotNull ByteBuf in);

    /**
     * 执行具体的解码逻辑
     * 
     * @param ctx 通道上下文
     * @param in  输入缓冲区
     * @return 解码后的消息，null表示需要更多数据
     */
    @Nullable
    protected abstract Object doDecode(@NotNull ChannelHandlerContext ctx, @NotNull ByteBuf in);

    /**
     * 处理解码错误
     * 
     * @param ctx   通道上下文
     * @param error 错误信息
     */
    protected void handleDecodeError(@NotNull ChannelHandlerContext ctx, @NotNull Exception error) {
        log.warn("Decode error occurred, closing connection: {}", ctx.channel().remoteAddress());
        ctx.close();
    }
}
