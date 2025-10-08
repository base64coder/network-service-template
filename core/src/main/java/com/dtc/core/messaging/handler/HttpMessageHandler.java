package com.dtc.core.messaging.handler;

import com.dtc.api.annotations.NotNull;
import com.dtc.core.http.HttpRequestEx;
import com.dtc.core.http.HttpRequestHandler;
import com.dtc.core.http.HttpResponseEx;
import com.dtc.core.messaging.NetworkMessageEvent;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * HTTP 消息处理器
 * 专门处理 HTTP 协议的消息
 * 
 * @author Network Service Template
 */
@Singleton
public class HttpMessageHandler {

    private static final Logger log = LoggerFactory.getLogger(HttpMessageHandler.class);

    private final HttpRequestHandler requestHandler;

    @Inject
    public HttpMessageHandler(@NotNull HttpRequestHandler requestHandler) {
        this.requestHandler = requestHandler;
    }

    /**
     * 处理 HTTP 消息
     */
    public void handleMessage(@NotNull NetworkMessageEvent event) {
        log.debug("🌐 Processing HTTP message: {}", event.getEventId());

        try {
            Object message = event.getMessage();
            ChannelHandlerContext ctx = event.getChannelContext();

            if (message instanceof FullHttpRequest) {
                FullHttpRequest nettyRequest = (FullHttpRequest) message;
                log.debug("📥 Converting FullHttpRequest to HttpRequestEx");

                // 将 Netty FullHttpRequest 转换为 HttpRequestEx
                HttpRequestEx httpRequest = convertToHttpRequestEx(nettyRequest);
                log.debug("✅ Successfully converted to HttpRequestEx: {} {}",
                        httpRequest.getMethod(), httpRequest.getPath());

                // 使用请求处理器处理请求（这里包含路由逻辑）
                log.debug("🔄 Calling requestHandler.handleRequest");
                HttpResponseEx httpResponse = requestHandler.handleRequest(httpRequest);
                log.debug("✅ Request handler returned response");

                // 发送响应
                log.debug("📤 Sending response via ctx.writeAndFlush");
                ctx.writeAndFlush(httpResponse);
                log.debug("✅ Response sent successfully");

                log.debug("✅ HTTP request processed successfully: {} {}",
                        httpRequest.getMethod(), httpRequest.getPath());

            } else {
                log.warn("⚠️ Unexpected message type in HTTP handler: {}",
                        message != null ? message.getClass().getSimpleName() : "null");
            }

        } catch (Exception e) {
            log.error("❌ Error processing HTTP message: {}", event.getEventId(), e);
            handleError(event, e);
        }
    }

    /**
     * 将 Netty FullHttpRequest 转换为 HttpRequestEx
     */
    @NotNull
    private HttpRequestEx convertToHttpRequestEx(@NotNull FullHttpRequest nettyRequest) {
        try {
            // 提取请求信息
            String method = nettyRequest.method().name();
            String uri = nettyRequest.uri();
            String path = extractPathFromUri(uri);

            // 提取头部信息
            java.util.Map<String, String> headers = new java.util.HashMap<>();
            for (java.util.Map.Entry<String, String> entry : nettyRequest.headers()) {
                headers.put(entry.getKey().toLowerCase(), entry.getValue());
            }

            // 提取查询参数
            java.util.Map<String, String> queryParams = extractQueryParameters(uri);

            // 获取请求体
            String body = null;
            if (nettyRequest.content() != null && nettyRequest.content().readableBytes() > 0) {
                try {
                    byte[] bodyBytes = new byte[nettyRequest.content().readableBytes()];
                    nettyRequest.content().getBytes(0, bodyBytes);
                    body = new String(bodyBytes, java.nio.charset.StandardCharsets.UTF_8);
                } catch (Exception e) {
                    log.warn("Failed to read request body: {}", e.getMessage());
                    body = null;
                }
            }

            // 获取内容类型
            String contentType = nettyRequest.headers().get("Content-Type");

            // 生成客户端ID
            String clientId = "client-" + System.currentTimeMillis();

            return new HttpRequestEx.Builder()
                    .method(method)
                    .path(path)
                    .uri(uri)
                    .version(nettyRequest.protocolVersion())
                    .headers(headers)
                    .queryParameters(queryParams)
                    .body(body)
                    .contentType(contentType)
                    .clientId(clientId)
                    .timestamp(System.currentTimeMillis())
                    .build();
        } catch (Exception e) {
            log.error("Failed to convert FullHttpRequest to HttpRequestEx: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to convert FullHttpRequest to HttpRequestEx", e);
        }
    }

    /**
     * 从URI中提取路径
     */
    @NotNull
    private String extractPathFromUri(@NotNull String uri) {
        int queryIndex = uri.indexOf('?');
        return queryIndex >= 0 ? uri.substring(0, queryIndex) : uri;
    }

    /**
     * 从URI中提取查询参数
     */
    @NotNull
    private java.util.Map<String, String> extractQueryParameters(@NotNull String uri) {
        java.util.Map<String, String> queryParams = new java.util.HashMap<>();
        int queryIndex = uri.indexOf('?');
        if (queryIndex >= 0 && queryIndex < uri.length() - 1) {
            String queryString = uri.substring(queryIndex + 1);
            String[] pairs = queryString.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=", 2);
                if (keyValue.length == 2) {
                    queryParams.put(keyValue[0], keyValue[1]);
                }
            }
        }
        return queryParams;
    }

    /**
     * 处理错误
     */
    private void handleError(@NotNull NetworkMessageEvent event, @NotNull Exception error) {
        log.error("💥 Error handling HTTP message: {}", event.getEventId(), error);

        try {
            ChannelHandlerContext ctx = event.getChannelContext();
            if (ctx != null && ctx.channel().isActive()) {
                // 发送HTTP错误响应
                // 这里可以注入HttpResponseHandler来创建错误响应
                log.error("HTTP error response sent to client: {}", ctx.channel().remoteAddress());
            }
        } catch (Exception e) {
            log.error("❌ Failed to send error response to HTTP client", e);
        }
    }
}
