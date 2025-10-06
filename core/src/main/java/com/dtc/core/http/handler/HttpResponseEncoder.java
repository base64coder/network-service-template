package com.dtc.core.http.handler;

import com.dtc.core.http.HttpResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HTTP 响应编码器 将 HTTP 响应对象编码为字节流
 * 
 * @author Network Service Template
 */
public class HttpResponseEncoder extends MessageToByteEncoder<HttpResponse> {

    private static final Logger log = LoggerFactory.getLogger(HttpResponseEncoder.class);

    @Override
    protected void encode(ChannelHandlerContext ctx, HttpResponse response, ByteBuf out) {
        try {
            // 创建 Netty HTTP 响应
            FullHttpResponse httpResponse = createNettyHttpResponse(response);

            // 写入响应
            out.writeBytes(httpResponse.content());

        } catch (Exception e) {
            log.error("Error encoding HTTP response", e);
            ctx.fireExceptionCaught(e);
        }
    }

    /**
     * 创建 Netty HTTP 响应
     */
    private FullHttpResponse createNettyHttpResponse(HttpResponse response) {
        // 创建响应状态
        HttpResponseStatus status = HttpResponseStatus.valueOf(response.getStatusCode());

        // 创建响应头
        DefaultHttpHeaders headers = new DefaultHttpHeaders();
        response.getHeaders().forEach(headers::set);

        // 设置内容类型
        if (response.getContentType() != null) {
            headers.set(HttpHeaderNames.CONTENT_TYPE, response.getContentType());
        }

        // 设置内容长度
        if (response.getBody() != null) {
            byte[] bodyBytes = response.getBody().getBytes();
            headers.set(HttpHeaderNames.CONTENT_LENGTH, bodyBytes.length);

            // 创建完整响应
            DefaultFullHttpResponse httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status,
                    io.netty.buffer.Unpooled.wrappedBuffer(bodyBytes));
            httpResponse.headers().set(headers);

            return httpResponse;
        } else {
            // 创建空响应
            DefaultFullHttpResponse httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status);
            httpResponse.headers().set(headers);

            return httpResponse;
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("HTTP response encoder exception", cause);
        ctx.fireExceptionCaught(cause);
    }
}
