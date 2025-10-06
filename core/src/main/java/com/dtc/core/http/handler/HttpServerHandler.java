package com.dtc.core.http.handler;

import com.dtc.api.annotations.NotNull;
import com.dtc.core.http.HttpRequest;
import com.dtc.core.http.HttpResponse;
import com.dtc.core.http.HttpRequestHandler;
import com.dtc.core.http.HttpResponseHandler;
import com.dtc.core.http.HttpRouteManager;
import com.dtc.core.http.HttpMiddlewareManager;
import com.dtc.core.http.HttpServer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

/**
 * HTTP 服务器处理器 处理 HTTP 请求和响应
 * 
 * @author Network Service Template
 */
public class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static final Logger log = LoggerFactory.getLogger(HttpServerHandler.class);

    private final HttpRequestHandler requestHandler;
    private final HttpResponseHandler responseHandler;
    private final HttpRouteManager routeManager;
    private final HttpMiddlewareManager middlewareManager;
    private final HttpServer httpServer;

    @Inject
    public HttpServerHandler(@NotNull HttpRequestHandler requestHandler, @NotNull HttpResponseHandler responseHandler,
            @NotNull HttpRouteManager routeManager, @NotNull HttpMiddlewareManager middlewareManager,
            @NotNull HttpServer httpServer) {
        this.requestHandler = requestHandler;
        this.responseHandler = responseHandler;
        this.routeManager = routeManager;
        this.middlewareManager = middlewareManager;
        this.httpServer = httpServer;
    }

    @Override
    protected void channelRead0(@NotNull ChannelHandlerContext ctx, @NotNull FullHttpRequest request) {
        try {
            // 生成客户端 ID
            String clientId = generateClientId(ctx);

            // 添加客户端连接
            httpServer.addClientConnection(clientId, ctx.channel());

            // 转换 Netty 请求为内部请求模型
            HttpRequest httpRequest = convertToHttpRequest(request, clientId);

            // 处理请求
            HttpResponse httpResponse = requestHandler.handleRequest(httpRequest);

            // 发送响应
            responseHandler.sendResponse(clientId, httpResponse);

        } catch (Exception e) {
            log.error("Error handling HTTP request", e);
            handleException(ctx, e);
        }
    }

    @Override
    public void channelActive(@NotNull ChannelHandlerContext ctx) {
        log.debug("HTTP client connected: {}", ctx.channel().remoteAddress());
    }

    @Override
    public void channelInactive(@NotNull ChannelHandlerContext ctx) {
        log.debug("HTTP client disconnected: {}", ctx.channel().remoteAddress());

        // 移除客户端连接
        String clientId = getClientIdFromContext(ctx);
        if (clientId != null) {
            httpServer.removeClientConnection(clientId);
        }
    }

    @Override
    public void exceptionCaught(@NotNull ChannelHandlerContext ctx, @NotNull Throwable cause) {
        log.error("HTTP exception for client: {}", ctx.channel().remoteAddress(), cause);
        handleException(ctx, cause);
    }

    /**
     * 生成客户端 ID
     */
    @NotNull
    private String generateClientId(@NotNull ChannelHandlerContext ctx) {
        return "http-client-" + ctx.channel().id().asShortText();
    }

    /**
     * 从上下文获取客户端 ID
     */
    @NotNull
    private String getClientIdFromContext(@NotNull ChannelHandlerContext ctx) {
        return "http-client-" + ctx.channel().id().asShortText();
    }

    /**
     * 转换 Netty 请求为内部请求模型
     */
    @NotNull
    private HttpRequest convertToHttpRequest(@NotNull FullHttpRequest request, @NotNull String clientId) {
        // 解析查询参数
        Map<String, String> queryParameters = parseQueryParameters(request.uri());

        // 解析路径
        String path = extractPath(request.uri());

        // 解析头部
        Map<String, String> headers = new HashMap<>();
        request.headers().forEach(entry -> headers.put(entry.getKey().toLowerCase(), entry.getValue()));

        // 获取内容类型
        String contentType = headers.get("content-type");

        // 获取请求体
        String body = request.content().toString();

        return new HttpRequest.Builder().method(request.method().name()).path(path).uri(request.uri())
                .version(request.protocolVersion()).headers(headers).queryParameters(queryParameters).body(body)
                .contentType(contentType).clientId(clientId).timestamp(System.currentTimeMillis()).build();
    }

    /**
     * 解析查询参数
     */
    @NotNull
    private Map<String, String> parseQueryParameters(@NotNull String uri) {
        Map<String, String> parameters = new HashMap<>();

        int queryIndex = uri.indexOf('?');
        if (queryIndex != -1 && queryIndex < uri.length() - 1) {
            String queryString = uri.substring(queryIndex + 1);
            String[] pairs = queryString.split("&");

            for (String pair : pairs) {
                String[] keyValue = pair.split("=", 2);
                if (keyValue.length == 2) {
                    parameters.put(keyValue[0], keyValue[1]);
                }
            }
        }

        return parameters;
    }

    /**
     * 提取路径
     */
    @NotNull
    private String extractPath(@NotNull String uri) {
        int queryIndex = uri.indexOf('?');
        if (queryIndex != -1) {
            return uri.substring(0, queryIndex);
        }
        return uri;
    }

    /**
     * 处理异常
     */
    private void handleException(@NotNull ChannelHandlerContext ctx, @NotNull Throwable cause) {
        try {
            // 创建错误响应
            HttpResponse errorResponse = responseHandler.createErrorResponse(500, "Internal Server Error",
                    cause.getMessage());

            // 发送错误响应
            String clientId = getClientIdFromContext(ctx);
            responseHandler.sendResponse(clientId, errorResponse);

        } catch (Exception e) {
            log.error("Failed to send error response", e);
            ctx.close();
        }
    }
}
