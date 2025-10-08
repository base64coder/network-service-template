package com.dtc.http;

import com.dtc.api.ExtensionMain;
import com.dtc.api.MessageHandler;
import com.dtc.api.ProtocolExtension;
import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import com.dtc.api.ServiceConfig;
import com.dtc.api.parameter.ExtensionStartInput;
import com.dtc.api.parameter.ExtensionStartOutput;
import com.dtc.api.parameter.ExtensionStopInput;
import com.dtc.api.parameter.ExtensionStopOutput;
import com.dtc.core.extensions.NetworkExtension;
import com.dtc.core.extensions.model.ExtensionMetadata;
import com.dtc.core.extensions.GracefulShutdownExtension;
import com.dtc.core.http.HttpRequestEx;
import com.dtc.core.http.HttpResponseEx;
import com.dtc.core.http.*;
import com.dtc.core.statistics.StatisticsAware;
import com.dtc.core.http.middleware.AuthMiddleware;
import com.dtc.core.http.middleware.CorsMiddleware;
import com.dtc.core.http.middleware.LoggingMiddleware;
import com.dtc.core.http.middleware.RateLimitMiddleware;
import com.dtc.core.messaging.NetworkMessageEvent;
import com.dtc.core.messaging.NetworkMessageQueue;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * HTTP REST 协议扩展
 * 提供 HTTP 请求处理、路由管理、中间件支持等功能
 *
 * @author Network Service Template
 */
@Singleton
public class HttpExtension extends StatisticsAware implements ExtensionMain, ProtocolExtension, NetworkExtension,
        GracefulShutdownExtension {

    private static final Logger log = LoggerFactory.getLogger(HttpExtension.class);

    @SuppressWarnings("unused") // 保留用于依赖注入，但由NettyServer统一管理
    private final HttpServer httpServer;
    private final HttpRequestHandler requestHandler;
    private final HttpResponseHandler responseHandler;
    private final HttpRouteManager routeManager;
    private final HttpMiddlewareManager middlewareManager;
    private final NetworkMessageQueue messageQueue;

    private volatile boolean started = false;
    private volatile boolean enabled = true;
    private volatile boolean shutdownPrepared = false;
    private volatile boolean stopped = false;

    // NetworkExtension 需要的字段
    private final String id = "http-extension";
    private final String name = "HTTP REST Protocol Extension";
    private final String version = "1.0.0";
    private final String author = "Network Service Template";
    private final int priority = 60;
    private final int startPriority = 1000;
    private final ExtensionMetadata metadata;
    private final Path extensionFolderPath;

    @Inject
    public HttpExtension(@NotNull HttpServer httpServer,
            @NotNull HttpRequestHandler requestHandler,
            @NotNull HttpResponseHandler responseHandler,
            @NotNull HttpRouteManager routeManager,
            @NotNull HttpMiddlewareManager middlewareManager,
            @NotNull NetworkMessageQueue messageQueue,
            @NotNull com.dtc.core.statistics.StatisticsCollector statisticsCollector) {
        super(statisticsCollector);
        this.httpServer = httpServer;
        this.requestHandler = requestHandler;
        this.responseHandler = responseHandler;
        this.routeManager = routeManager;
        this.middlewareManager = middlewareManager;
        this.messageQueue = messageQueue;

        // 初始化 NetworkExtension 字段
        this.metadata = ExtensionMetadata.builder()
                .id(id)
                .name(name)
                .version(version)
                .author(author)
                .priority(priority)
                .startPriority(startPriority)
                .description("HTTP REST protocol extension for handling HTTP requests and responses")
                .mainClass("com.dtc.http.HttpExtension")
                .build();
        this.extensionFolderPath = Path.of("extensions/http-extension");
    }

    // ========== ExtensionMain 接口实现 ==========

    @Override
    public void extensionStart(@NotNull ExtensionStartInput input, @NotNull ExtensionStartOutput output) {
        if (!started) {
            started = true;
            log.info("🚀 Starting HTTP REST extension...");

            try {
                // 初始化路由
                initializeRoutes();

                // 初始化中间件
                initializeMiddleware();

                log.info("✅ HTTP REST extension started successfully (server managed by NettyServer)");
            } catch (Exception e) {
                log.error("❌ Failed to start HTTP REST extension", e);
                started = false;
                output.preventStartup("Failed to start HTTP REST extension: " + e.getMessage());
                throw new RuntimeException("Failed to start HTTP REST extension", e);
            }
        }
    }

    @Override
    public void extensionStop(@NotNull ExtensionStopInput input, @NotNull ExtensionStopOutput output) {
        if (started) {
            started = false;
            log.info("🛑 Stopping HTTP REST extension...");

            try {
                stopped = true;

                log.info("✅ HTTP REST extension stopped successfully (server managed by NettyServer)");
            } catch (Exception e) {
                log.error("❌ Error stopping HTTP REST extension", e);
                throw new RuntimeException("Failed to stop HTTP REST extension", e);
            }
        }
    }

    // ========== ProtocolExtension 接口实现 ==========

    @Override
    @NotNull
    public String getProtocolName() {
        return ServiceConfig.HTTP.getServiceName();
    }

    @Override
    @NotNull
    public String getProtocolVersion() {
        return "1.1";
    }

    @Override
    public int getDefaultPort() {
        return ServiceConfig.HTTP.getDefaultPort();
    }

    @Override
    public void onConnect(@NotNull ChannelHandlerContext ctx, @NotNull String clientId) {
        log.info("🔗 HTTP client connected: {}", clientId);
        // HTTP 连接处理逻辑
    }

    @Override
    public void onDisconnect(@NotNull ChannelHandlerContext ctx, @NotNull String clientId) {
        log.info("🔌 HTTP client disconnected: {}", clientId);
        // HTTP 断开连接处理逻辑
    }

    @Override
    public void onMessage(@NotNull ChannelHandlerContext ctx, @NotNull Object message) {
        String clientAddress = ctx.channel() != null ? ctx.channel().remoteAddress().toString() : "unknown";
        log.debug("📨 HTTP message received from client: {}", clientAddress);

        try {
            // 处理 HTTP 消息 - 使用 Disruptor 异步处理
            if (message instanceof FullHttpRequest) {
                FullHttpRequest nettyRequest = (FullHttpRequest) message;

                // 创建网络消息事件
                NetworkMessageEvent event = createNetworkMessageEvent(ctx, nettyRequest);

                // 发布到 Disruptor 队列进行异步处理
                boolean published = messageQueue.publish(event);
                if (published) {
                    log.debug("✅ HTTP message published to Disruptor queue: {}", event.getEventId());
                } else {
                    log.error("❌ Failed to publish HTTP message to Disruptor queue");
                    // 如果发布失败，发送错误响应
                    sendErrorResponse(ctx, "Service temporarily unavailable");
                }
            } else {
                log.warn("⚠️ Received unexpected message type in HTTP extension: {}",
                        message.getClass().getSimpleName());
            }
        } catch (Exception e) {
            log.error("❌ Error handling HTTP message from client: {}", clientAddress, e);
            sendErrorResponse(ctx, "Internal server error");
        }
    }

    @Override
    public void onException(@NotNull ChannelHandlerContext ctx, @NotNull Throwable cause) {
        String clientAddress = ctx.channel() != null ? ctx.channel().remoteAddress().toString() : "unknown";
        log.error("💥 HTTP exception for client: {}", clientAddress, cause);

        try {
            // 创建错误响应
            HttpResponseEx errorResponse = responseHandler.createErrorResponse(500,
                    "Internal Server Error", cause.getMessage());
            ctx.writeAndFlush(errorResponse);
        } catch (Exception e) {
            log.error("❌ Failed to send error response to client: {}", clientAddress, e);
        }
    }

    @Override
    @Nullable
    public MessageHandler getMessageHandler() {
        return new HttpMessageHandler();
    }

    // ========== NetworkExtension 接口实现 ==========

    @Override
    @NotNull
    public String getId() {
        return id;
    }

    @Override
    @NotNull
    public String getName() {
        return name;
    }

    @Override
    @NotNull
    public String getVersion() {
        return version;
    }

    @Override
    @Nullable
    public String getAuthor() {
        return author;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public int getStartPriority() {
        return startPriority;
    }

    @Override
    @NotNull
    public ExtensionMetadata getMetadata() {
        return metadata;
    }

    @Override
    @NotNull
    public Path getExtensionFolderPath() {
        return extensionFolderPath;
    }

    @Override
    @Nullable
    public ClassLoader getExtensionClassloader() {
        return this.getClass().getClassLoader();
    }

    @Override
    public void start() throws Exception {
        log.info("🔍 HttpExtension.start() method called - thread: {}", Thread.currentThread().getName());
        log.info("🔍 Current started state: {}", started);

        if (!started) {
            started = true;
            log.info("🚀 Starting HTTP REST extension...");

            try {
                // 初始化路由
                initializeRoutes();

                // 初始化中间件
                initializeMiddleware();
                log.info("✅ HTTP REST extension initialized successfully (server managed by NettyServer)");
            } catch (Exception e) {
                log.error("❌ Failed to initialize HTTP REST extension", e);
                started = false;
                throw e;
            }
        }
    }

    @Override
    public void stop() throws Exception {
        if (started) {
            started = false;
            log.info("🛑 Stopping HTTP REST extension...");

            try {
                stopped = true;

                log.info("✅ HTTP REST extension stopped successfully (server managed by NettyServer)");
            } catch (Exception e) {
                log.error("❌ Error stopping HTTP REST extension", e);
                throw e;
            }
        }
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        log.info("HTTP REST extension {} {}", enabled ? "enabled" : "disabled");
    }

    @Override
    public boolean isStarted() {
        return started;
    }

    @Override
    public boolean isStopped() {
        return stopped;
    }

    @Override
    public void cleanup(boolean disable) {
        log.info("🧹 Cleaning up HTTP REST extension (disable: {})", disable);

        try {
            if (started) {
                stop();
            }

            if (disable) {
                setEnabled(false);
            }

            // 清理资源
            routeManager.clearRoutes();
            middlewareManager.clearMiddlewares();

            log.info("✅ HTTP REST extension cleanup completed");
        } catch (Exception e) {
            log.error("❌ Error during HTTP REST extension cleanup", e);
        }
    }

    // ========== 私有方法 ==========

    /**
     * 初始化路由
     */
    private void initializeRoutes() {
        log.info("🔧 Initializing HTTP routes...");

        // 注册基础路由
        routeManager.registerRoute("GET", "/", this::handleRoot);
        routeManager.registerRoute("GET", "/health", this::handleHealth);
        routeManager.registerRoute("GET", "/status", this::handleStatus);
        routeManager.registerRoute("GET", "/api/info", this::handleApiInfo);

        // 注册 API 路由
        routeManager.registerRoute("GET", "/api/users", this::handleGetUsers);
        routeManager.registerRoute("POST", "/api/users", this::handleCreateUser);
        routeManager.registerRoute("GET", "/api/users/{id}", this::handleGetUser);
        routeManager.registerRoute("PUT", "/api/users/{id}", this::handleUpdateUser);
        routeManager.registerRoute("DELETE", "/api/users/{id}", this::handleDeleteUser);

        // 注册订单路由
        routeManager.registerRoute("GET", "/api/orders", this::handleGetOrders);
        routeManager.registerRoute("POST", "/api/orders", this::handleCreateOrder);
        routeManager.registerRoute("GET", "/api/orders/{id}", this::handleGetOrder);

        // 注册产品路由
        routeManager.registerRoute("GET", "/api/products", this::handleGetProducts);
        routeManager.registerRoute("POST", "/api/products", this::handleCreateProduct);
        routeManager.registerRoute("GET", "/api/products/{id}", this::handleGetProduct);

        log.info("✅ HTTP routes initialized successfully");
    }

    /**
     * 初始化中间件
     */
    private void initializeMiddleware() {
        log.info("🔧 Initializing HTTP middleware...");

        // 注册中间件
        middlewareManager.addMiddleware(new CorsMiddleware());
        middlewareManager.addMiddleware(new LoggingMiddleware());
        middlewareManager.addMiddleware(new AuthMiddleware());
        middlewareManager.addMiddleware(new RateLimitMiddleware());

        log.info("✅ HTTP middleware initialized successfully");
    }

    // ========== 路由处理方法 ==========

    private HttpResponseEx handleRoot(HttpRequestEx request) {
        return responseHandler.createJsonResponse(200, Map.of(
                "message", "Welcome to HTTP REST API",
                "version", "1.0.0",
                "timestamp", System.currentTimeMillis()));
    }

    private HttpResponseEx handleHealth(HttpRequestEx request) {
        return responseHandler.createJsonResponse(200, Map.of(
                "status", "healthy",
                "timestamp", System.currentTimeMillis()));
    }

    private HttpResponseEx handleStatus(HttpRequestEx request) {
        return responseHandler.createJsonResponse(200, Map.of(
                "status", "running",
                "uptime", System.currentTimeMillis(),
                "activeConnections", statisticsCollector.getActiveConnections(),
                "totalRequests", statisticsCollector.getTotalRequests()));
    }

    private HttpResponseEx handleApiInfo(HttpRequestEx request) {
        return responseHandler.createJsonResponse(200, Map.of(
                "name", "Network Service Template HTTP API",
                "version", "1.0.0",
                "description", "RESTful API for network service template",
                "endpoints", Map.of(
                        "users", "/api/users",
                        "orders", "/api/orders",
                        "products", "/api/products")));
    }

    // 用户相关路由
    private HttpResponseEx handleGetUsers(HttpRequestEx request) {
        // 实现获取用户列表逻辑
        return responseHandler.createJsonResponse(200, Map.of(
                "users", java.util.Arrays.asList(
                        Map.of("id", 1, "name", "John Doe", "email", "john@example.com"),
                        Map.of("id", 2, "name", "Jane Smith", "email", "jane@example.com"))));
    }

    private HttpResponseEx handleCreateUser(HttpRequestEx request) {
        // 实现创建用户逻辑
        return responseHandler.createJsonResponse(201, Map.of(
                "message", "User created successfully",
                "id", System.currentTimeMillis()));
    }

    private HttpResponseEx handleGetUser(HttpRequestEx request) {
        // 实现获取单个用户逻辑
        String userId = request.getPathParameters().get("id");
        return responseHandler.createJsonResponse(200, Map.of(
                "id", userId,
                "name", "John Doe",
                "email", "john@example.com"));
    }

    private HttpResponseEx handleUpdateUser(HttpRequestEx request) {
        // 实现更新用户逻辑
        String userId = request.getPathParameters().get("id");
        return responseHandler.createJsonResponse(200, Map.of(
                "message", "User updated successfully",
                "id", userId));
    }

    private HttpResponseEx handleDeleteUser(HttpRequestEx request) {
        // 实现删除用户逻辑
        String userId = request.getPathParameters().get("id");
        return responseHandler.createJsonResponse(200, Map.of(
                "message", "User deleted successfully",
                "id", userId));
    }

    // 订单相关路由
    private HttpResponseEx handleGetOrders(HttpRequestEx request) {
        return responseHandler.createJsonResponse(200, Map.of(
                "orders", java.util.Arrays.asList(
                        Map.of("id", 1, "userId", 1, "total", 99.99, "status", "pending"),
                        Map.of("id", 2, "userId", 2, "total", 149.99, "status", "completed"))));
    }

    private HttpResponseEx handleCreateOrder(HttpRequestEx request) {
        return responseHandler.createJsonResponse(201, Map.of(
                "message", "Order created successfully",
                "id", System.currentTimeMillis()));
    }

    private HttpResponseEx handleGetOrder(HttpRequestEx request) {
        String orderId = request.getPathParameters().get("id");
        return responseHandler.createJsonResponse(200, Map.of(
                "id", orderId,
                "userId", 1,
                "total", 99.99,
                "status", "pending"));
    }

    // 产品相关路由
    private HttpResponseEx handleGetProducts(HttpRequestEx request) {
        return responseHandler.createJsonResponse(200, Map.of(
                "products", java.util.Arrays.asList(
                        Map.of("id", 1, "name", "Product A", "price", 99.99),
                        Map.of("id", 2, "name", "Product B", "price", 149.99))));
    }

    private HttpResponseEx handleCreateProduct(HttpRequestEx request) {
        return responseHandler.createJsonResponse(201, Map.of(
                "message", "Product created successfully",
                "id", System.currentTimeMillis()));
    }

    private HttpResponseEx handleGetProduct(HttpRequestEx request) {
        String productId = request.getPathParameters().get("id");
        return responseHandler.createJsonResponse(200, Map.of(
                "id", productId,
                "name", "Product A",
                "price", 99.99,
                "description", "A great product"));
    }

    // ========== 内部类 ==========

    /**
     * HTTP 消息处理器
     */
    public class HttpMessageHandler implements MessageHandler {
        @Override
        @Nullable
        public Object handleMessage(@NotNull ChannelHandlerContext ctx, @NotNull Object message) {
            if (message instanceof HttpRequestEx) {
                HttpRequestEx httpRequest = (HttpRequestEx) message;
                try {
                    HttpResponseEx response = requestHandler.handleRequest(httpRequest);
                    log.debug("HTTP request processed: {} {}", httpRequest.getMethod(), httpRequest.getPath());
                    return response;
                } catch (Exception e) {
                    log.error("Error processing HTTP request", e);
                    return responseHandler.createErrorResponse(500, "Internal Server Error", e.getMessage());
                }
            }
            return null;
        }

        @Override
        @Nullable
        public Object handleOutboundMessage(@NotNull ChannelHandlerContext ctx, @NotNull Object message) {
            if (message instanceof HttpResponseEx) {
                HttpResponseEx httpResponse = (HttpResponseEx) message;
                log.debug("HTTP response being sent: {}", httpResponse.getStatusCode());
                return httpResponse;
            }
            return null;
        }

        @Override
        public boolean supports(@NotNull Class<?> messageType) {
            return HttpRequestEx.class.isAssignableFrom(messageType) ||
                    HttpResponseEx.class.isAssignableFrom(messageType);
        }
    }

    // ========== 辅助方法 ==========

    /**
     * 创建网络消息事件
     */
    @NotNull
    private NetworkMessageEvent createNetworkMessageEvent(@NotNull ChannelHandlerContext ctx,
            @NotNull FullHttpRequest nettyRequest) {
        // 生成客户端ID
        String clientId = "client-" + System.currentTimeMillis();

        // 计算消息大小
        int messageSize = nettyRequest.content() != null ? nettyRequest.content().readableBytes() : 0;

        String sourceAddress = ctx.channel() != null && ctx.channel().remoteAddress() != null 
                ? ctx.channel().remoteAddress().toString() 
                : "unknown";
        
        return NetworkMessageEvent.builder()
                .protocolType("http")
                .clientId(clientId)
                .message(nettyRequest)
                .channelContext(ctx)
                .sourceAddress(sourceAddress)
                .messageSize(messageSize)
                .messageType("HTTP_REQUEST")
                .isRequest(true)
                .priority(1) // HTTP请求优先级
                .build();
    }

    /**
     * 发送错误响应
     */
    private void sendErrorResponse(@NotNull ChannelHandlerContext ctx, @NotNull String errorMessage) {
        try {
            HttpResponseEx errorResponse = responseHandler.createErrorResponse(500, "Internal Server Error",
                    errorMessage);
            ctx.writeAndFlush(errorResponse);
        } catch (Exception e) {
            log.error("❌ Failed to send error response to client: {}", ctx.channel().remoteAddress(), e);
        }
    }

    /**
     * 将 Netty FullHttpRequest 转换为 HttpRequestEx
     */
    @NotNull
    private HttpRequestEx convertToHttpRequestEx(@NotNull io.netty.handler.codec.http.FullHttpRequest nettyRequest) {
        // 提取请求信息
        String method = nettyRequest.method().name();
        String uri = nettyRequest.uri();
        String path = extractPathFromUri(uri);

        // 提取头部信息
        Map<String, String> headers = new HashMap<>();
        for (Map.Entry<String, String> entry : nettyRequest.headers()) {
            headers.put(entry.getKey().toLowerCase(), entry.getValue());
        }

        // 提取查询参数
        Map<String, String> queryParams = extractQueryParameters(uri);

        // 获取请求体
        String body = null;
        if (nettyRequest.content() != null && nettyRequest.content().readableBytes() > 0) {
            byte[] bodyBytes = new byte[nettyRequest.content().readableBytes()];
            nettyRequest.content().getBytes(0, bodyBytes);
            body = new String(bodyBytes, java.nio.charset.StandardCharsets.UTF_8);
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
    private Map<String, String> extractQueryParameters(@NotNull String uri) {
        Map<String, String> queryParams = new HashMap<>();
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

    // ========== GracefulShutdownExtension 实现 ==========

    @Override
    public void prepareForShutdown() throws Exception {
        log.info("Preparing HTTP extension for shutdown...");
        shutdownPrepared = true;

        // 停止接收新的 HTTP 请求
        // 这里可以移除路由、关闭端口等
        log.info("HTTP extension prepared for shutdown");
    }

    @Override
    public boolean canShutdownSafely() {
        return statisticsCollector.getActiveRequestCount() == 0;
    }

    @Override
    public long getActiveRequestCount() {
        return super.getActiveRequestCount();
    }

    @Override
    public boolean waitForRequestsToComplete(long timeoutMs) {
        long startTime = System.currentTimeMillis();

        while (statisticsCollector.getActiveRequestCount() > 0
                && (System.currentTimeMillis() - startTime) < timeoutMs) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }

        return statisticsCollector.getActiveRequestCount() == 0;
    }

    // ========== 统计功能已移至StatisticsAware基类 ==========
}
