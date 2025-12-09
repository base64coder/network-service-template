package com.dtc.core.network.http.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * HTTP 请求解码器
 * 将字节流解码为HTTP请求对象
 * 
 * @author Network Service Template
 */
public class HttpRequestDecoder extends ByteToMessageDecoder {

    private static final Logger log = LoggerFactory.getLogger(HttpRequestDecoder.class);

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        try {
            // 可以通过路由管理器实现自定义的 HTTP 请求解码逻辑
            // 这里使用 Netty 内置的 HTTP 解码器
            // 如果需要自定义解码逻辑，可以通过路由管理器实现

            // 简单示例：直接传递数据给下一个处理器
            if (in.readableBytes() > 0) {
                out.add(in.retain());
            }

        } catch (Exception e) {
            log.error("Error decoding HTTP request", e);
            ctx.fireExceptionCaught(e);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("HTTP request decoder exception", cause);
        ctx.fireExceptionCaught(cause);
    }
}
