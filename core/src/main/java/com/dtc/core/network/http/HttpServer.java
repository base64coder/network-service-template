package com.dtc.core.network.http;

import com.dtc.api.annotations.NotNull;
import com.dtc.core.statistics.StatisticsCollector;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * HTTP服务器
 * 负责处理HTTP请求和响应，通过Netty服务器接收请求并分发处理
 * 注册路由和HTTP服务器的启动和停止通过NettyServer来管理
 * 
 * @author Network Service Template
 */
@Singleton
public class HttpServer extends ChannelInboundHandlerAdapter {

    private static final Logger log = LoggerFactory.getLogger(HttpServer.class);
    private final StatisticsCollector statisticsCollector;

    @Inject
    public HttpServer(@NotNull StatisticsCollector statisticsCollector) {
        this.statisticsCollector = statisticsCollector;
    }

    // ========== ChannelInboundHandlerAdapter 方法 ==========

    @Override
    public void channelActive(@NotNull ChannelHandlerContext ctx) throws Exception {
        statisticsCollector.onConnectionEstablished();
        log.debug("🔍 New HTTP connection established. Active connections: {}",
                statisticsCollector.getActiveConnections());
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(@NotNull ChannelHandlerContext ctx) throws Exception {
        statisticsCollector.onConnectionClosed();
        log.debug("🔍 HTTP connection closed. Active connections: {}", statisticsCollector.getActiveConnections());
        super.channelInactive(ctx);
    }

    @Override
    public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            handleHttpRequest(ctx, (FullHttpRequest) msg);
        } else {
            log.warn("⚠️  Received unexpected message type: {}", msg.getClass().getSimpleName());
            ctx.fireChannelRead(msg);
        }
    }

    @Override
    public void exceptionCaught(@NotNull ChannelHandlerContext ctx, @NotNull Throwable cause) throws Exception {
        log.error("❌ HTTP server exception", cause);
        ctx.close();
    }

    // ========== HTTP请求处理方法 ==========

    /**
     * 处理HTTP请求
     * 
     * @param ctx     ChannelHandlerContext
     * @param request HTTP请求
     */
    private void handleHttpRequest(@NotNull ChannelHandlerContext ctx, @NotNull FullHttpRequest request) {
        long startTime = System.currentTimeMillis();
        statisticsCollector.onRequestStart();

        try {
            log.debug("📥 Received HTTP request: {} {}", request.method(), request.uri());

            // 创建HTTP请求对象
            HttpRequestEx requestEx = createHttpRequestEx(request);

            // 处理请求
            HttpResponseEx responseEx = processRequest(requestEx);

            // 发送响应
            sendResponse(ctx, responseEx);
            log.debug("📤 Sent HTTP response: {}", responseEx.getStatusCode());

            // 记录请求完成
            long processingTime = System.currentTimeMillis() - startTime;
            statisticsCollector.onRequestComplete(processingTime);

        } catch (Exception e) {
            log.error("❌ Failed to handle HTTP request", e);
            statisticsCollector.onRequestError();
            sendErrorResponse(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR, "Internal Server Error");
        }
    }

    /**
     * 创建HTTP请求对象
     * 
     * @param request Netty HTTP请求
     * @return HttpRequestEx对象
     */
    @NotNull
    private HttpRequestEx createHttpRequestEx(@NotNull FullHttpRequest request) {
        // 使用Builder模式创建HttpRequestEx对象
        return new HttpRequestEx.Builder()
                .method(request.method().name())
                .path(request.uri())
                .uri(request.uri())
                .version(request.protocolVersion())
                .body(request.content() != null ? request.content().toString() : null)
                .contentType(request.headers().get("Content-Type"))
                .clientId(null) // 可以通过路由管理器获取客户端ID
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 处理HTTP请求
     * 
     * @param request HTTP请求
     * @return HTTP响应
     */
    @NotNull
    private HttpResponseEx processRequest(@NotNull HttpRequestEx request) {
        try {
            // 简单的请求处理，可以通过路由管理器实现更复杂的路由处理
            String uri = request.getUri();

            if ("/health".equals(uri)) {
                return createHealthResponse();
            } else if ("/status".equals(uri)) {
                return createStatusResponse();
            } else if ("/".equals(uri)) {
                return createWelcomeResponse();
            } else {
                return createNotFoundResponse();
            }

        } catch (Exception e) {
            log.error("❌ Failed to process request", e);
            return createErrorResponse(500, "Internal Server Error: " + e.getMessage());
        }
    }

    /**
     * 发送HTTP响应
     * 
     * @param ctx      ChannelHandlerContext
     * @param response HTTP响应
     */
    private void sendResponse(@NotNull ChannelHandlerContext ctx, @NotNull HttpResponseEx response) {
        try {
            // 简单的响应发送，可以通过路由管理器转换为Netty响应
            FullHttpResponse httpResponse = createNettyResponse(response);
            ctx.writeAndFlush(httpResponse);
            log.debug("📤 Sent HTTP response: {}", httpResponse.status());
        } catch (Exception e) {
            log.error("❌ Failed to send response", e);
            sendErrorResponse(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR, "Response Error");
        }
    }

    /**
     * 创建Netty HTTP响应
     */
    @NotNull
    private FullHttpResponse createNettyResponse(@NotNull HttpResponseEx response) {
        HttpResponseStatus status = HttpResponseStatus.valueOf(response.getStatusCode());
        FullHttpResponse httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status);

        // 设置内容类型
        httpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8");

        // 设置响应内容
        String content = response.getBody();
        if (content != null) {
            httpResponse.content().writeBytes(content.getBytes());
        }

        // 设置内容长度头部
        httpResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, httpResponse.content().readableBytes());

        return httpResponse;
    }

    // ========== 响应创建方法 ==========

    /**
     * 创建健康检查响应
     */
    @NotNull
    private HttpResponseEx createHealthResponse() {
        String content = "{\"status\":\"healthy\",\"timestamp\":" + System.currentTimeMillis() + "}";
        return new HttpResponseEx.Builder()
                .statusCode(200)
                .statusMessage("OK")
                .body(content)
                .contentType("application/json")
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 创建状态响应
     */
    @NotNull
    private HttpResponseEx createStatusResponse() {
        String content = String.format("{\"status\":\"running\",\"activeConnections\":%d,\"timestamp\":%d}",
                statisticsCollector.getActiveConnections(), System.currentTimeMillis());
        return new HttpResponseEx.Builder()
                .statusCode(200)
                .statusMessage("OK")
                .body(content)
                .contentType("application/json")
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 创建欢迎响应
     */
    @NotNull
    private HttpResponseEx createWelcomeResponse() {
        String content = "{\"message\":\"Welcome to Network Service Template HTTP API\",\"version\":\"1.0.0\"}";
        return new HttpResponseEx.Builder()
                .statusCode(200)
                .statusMessage("OK")
                .body(content)
                .contentType("application/json")
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 创建404响应
     */
    @NotNull
    private HttpResponseEx createNotFoundResponse() {
        String content = "{\"error\":\"Not Found\",\"message\":\"The requested resource was not found\"}";
        return new HttpResponseEx.Builder()
                .statusCode(404)
                .statusMessage("Not Found")
                .body(content)
                .contentType("application/json")
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 创建错误响应
     */
    @NotNull
    private HttpResponseEx createErrorResponse(int statusCode, String message) {
        String content = String.format("{\"error\":\"Server Error\",\"message\":\"%s\"}", message);
        return new HttpResponseEx.Builder()
                .statusCode(statusCode)
                .statusMessage("Server Error")
                .body(content)
                .contentType("application/json")
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 发送错误响应
     * 
     * @param ctx     ChannelHandlerContext
     * @param status  响应状态
     * @param message 错误消息
     */
    private void sendErrorResponse(@NotNull ChannelHandlerContext ctx, @NotNull HttpResponseStatus status,
            @NotNull String message) {
        try {
            FullHttpResponse response = new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1, status);
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
            response.content().writeBytes(message.getBytes());
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());

            ctx.writeAndFlush(response);
        } catch (Exception e) {
            log.error("❌ Failed to send error response", e);
            ctx.close();
        }
    }

    // ========== 统计信息方法 ==========

    /**
     * 获取活动连接数
     * 
     * @return 活动连接数
     */
    public int getActiveConnections() {
        return statisticsCollector.getActiveConnections();
    }

    /**
     * 获取总客户端数
     * 
     * @return 总客户端数
     */
    public int getTotalClients() {
        return statisticsCollector.getTotalClients();
    }

    /**
     * 获取总请求数
     * 
     * @return 总请求数
     */
    public long getTotalRequests() {
        return statisticsCollector.getTotalRequests();
    }

    /**
     * 获取统计信息
     * 
     * @return 统计信息
     */
    @NotNull
    public StatisticsCollector.StatisticsInfo getStatistics() {
        return statisticsCollector.getStatistics();
    }
}
