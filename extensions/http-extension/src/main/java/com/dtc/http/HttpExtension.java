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
import com.dtc.core.network.http.HttpRequestEx;
import com.dtc.core.network.http.HttpResponseEx;
import com.dtc.core.network.http.HttpServer;
import com.dtc.core.network.http.HttpRequestHandler;
import com.dtc.core.network.http.HttpResponseHandler;
import com.dtc.core.network.http.HttpRouteManager;
import com.dtc.core.network.http.HttpMiddlewareManager;
import com.dtc.core.statistics.StatisticsAware;
import com.dtc.core.network.http.middleware.AuthMiddleware;
import com.dtc.core.network.http.middleware.CorsMiddleware;
import com.dtc.core.network.http.middleware.LoggingMiddleware;
import com.dtc.core.network.http.middleware.RateLimitMiddleware;
import com.dtc.core.messaging.NetworkMessageEvent;
import com.dtc.core.messaging.NetworkMessageQueue;
import com.dtc.core.web.WebControllerScanner;
import com.dtc.core.web.IoCBeanProvider;
import com.google.inject.Injector;
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
    private final @Nullable Injector injector;

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
                         @NotNull com.dtc.core.statistics.StatisticsCollector statisticsCollector,
                         @Nullable Injector injector) {
        super(statisticsCollector);
        this.httpServer = httpServer;
        this.requestHandler = requestHandler;
        this.responseHandler = responseHandler;
        this.routeManager = routeManager;
        this.middlewareManager = middlewareManager;
        this.messageQueue = messageQueue;
        this.injector = injector;

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
            log.info("Starting HTTP REST extension...");

            try {
                // 初始化中间件
                initializeMiddleware();

                // 扫描并注册注解驱动的控制器（替代硬编码路由）
                scanAndRegisterControllers();

                log.info("HTTP REST extension started successfully (server managed by NettyServer)");
            } catch (Exception e) {
                log.error("Failed to start HTTP REST extension", e);
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
            log.info("Stopping HTTP REST extension...");

            try {
                stopped = true;

                log.info("HTTP REST extension stopped successfully (server managed by NettyServer)");
            } catch (Exception e) {
                log.error("Error stopping HTTP REST extension", e);
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
        log.info("HTTP client connected: {}", clientId);
        // HTTP 连接处理逻辑
    }

    @Override
    public void onDisconnect(@NotNull ChannelHandlerContext ctx, @NotNull String clientId) {
        log.info("HTTP client disconnected: {}", clientId);
        // HTTP 断开连接处理逻辑
    }

    @Override
    public void onMessage(@NotNull ChannelHandlerContext ctx, @NotNull Object message) {
        String clientAddress = ctx.channel() != null ? ctx.channel().remoteAddress().toString() : "unknown";
        log.debug("HTTP message received from client: {}", clientAddress);

        try {
            // 处理 HTTP 消息 - 使用 Disruptor 异步处理
            if (message instanceof FullHttpRequest) {
                FullHttpRequest nettyRequest = (FullHttpRequest) message;

                // 创建网络消息事件
                NetworkMessageEvent event = createNetworkMessageEvent(ctx, nettyRequest);

                // 发布到 Disruptor 队列进行异步处理
                boolean published = messageQueue.publish(event);
                if (published) {
                    log.debug("HTTP message published to Disruptor queue: {}", event.getEventId());
                } else {
                    log.error("Failed to publish HTTP message to Disruptor queue");
                    // 如果发布失败，发送错误响应
                    sendErrorResponse(ctx, "Service temporarily unavailable");
                }
            } else {
                log.warn("Received unexpected message type in HTTP extension: {}",
                        message.getClass().getSimpleName());
            }
        } catch (Exception e) {
            log.error("Error handling HTTP message from client: {}", clientAddress, e);
            sendErrorResponse(ctx, "Internal server error");
        }
    }

    @Override
    public void onException(@NotNull ChannelHandlerContext ctx, @NotNull Throwable cause) {
        String clientAddress = ctx.channel() != null ? ctx.channel().remoteAddress().toString() : "unknown";
        log.error("HTTP exception for client: {}", clientAddress, cause);

        try {
            // 创建错误响应
            HttpResponseEx errorResponse = responseHandler.createErrorResponse(500,
                    "Internal Server Error", cause.getMessage());
            ctx.writeAndFlush(errorResponse);
        } catch (Exception e) {
            log.error("Failed to send error response to client: {}", clientAddress, e);
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
        log.info("HttpExtension.start() method called - thread: {}", Thread.currentThread().getName());
        log.info("Current started state: {}", started);

        if (!started) {
            started = true;
            log.info("Starting HTTP REST extension...");

            try {
                // 初始化中间件
                initializeMiddleware();

                // 扫描并注册注解驱动的控制器
                scanAndRegisterControllers();
                
                log.info("HTTP REST extension initialized successfully (server managed by NettyServer)");
            } catch (Exception e) {
                log.error("Failed to initialize HTTP REST extension", e);
                started = false;
                throw e;
            }
        }
    }

    @Override
    public void stop() throws Exception {
        if (started) {
            started = false;
            log.info("Stopping HTTP REST extension...");

            try {
                stopped = true;

                log.info("HTTP REST extension stopped successfully (server managed by NettyServer)");
            } catch (Exception e) {
                log.error("Error stopping HTTP REST extension", e);
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
        log.info("Cleaning up HTTP REST extension (disable: {})", disable);

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

            log.info("HTTP REST extension cleanup completed");
        } catch (Exception e) {
            log.error("Error during HTTP REST extension cleanup", e);
        }
    }

    // ========== 私有方法 ==========

    /**
     * 初始化中间件
     */
    private void initializeMiddleware() {
        log.info("Initializing HTTP middleware...");

        // 注册中间件
        middlewareManager.addMiddleware(new CorsMiddleware());
        middlewareManager.addMiddleware(new LoggingMiddleware());
        middlewareManager.addMiddleware(new AuthMiddleware());
        middlewareManager.addMiddleware(new RateLimitMiddleware());

        log.info("HTTP middleware initialized successfully");
    }

    /**
     * 扫描并注册注解驱动的控制器
     */
    private void scanAndRegisterControllers() {
        if (injector != null) {
            try {
                log.info("Scanning for @RestController annotated controllers...");

                // 获取参数解析器
                com.dtc.core.web.argument.HandlerMethodArgumentResolverComposite argumentResolver =
                        injector.getInstance(com.dtc.core.web.argument.HandlerMethodArgumentResolverComposite.class);

                // 创建Bean提供者并设置扫描包
                // 默认扫描com包，可以通过配置指定
                IoCBeanProvider beanProvider = new IoCBeanProvider(injector, "com");

                // 创建控制器扫描器
                WebControllerScanner scanner = new WebControllerScanner(
                        routeManager,
                        beanProvider,
                        argumentResolver
                );

                // 扫描并注册控制器
                scanner.scanAndRegister("com");

                log.info("Controller scanning completed");
            } catch (Exception e) {
                log.warn("Failed to scan controllers, continuing with manual routes only: {}", e.getMessage());
                log.debug("Controller scanning error details", e);
            }
        } else {
            log.info("Injector not available, skipping annotation-driven controller scanning");
            log.info("Tip: To enable controller scanning, ensure Injector is available via dependency injection");
        }
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
            log.error("Failed to send error response to client: {}", ctx.channel().remoteAddress(), e);
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
        return super.getActiveRequestCount() == 0;
    }

    @Override
    public long getActiveRequestCount() {
        return super.getActiveRequestCount();
    }

    @Override
    public boolean waitForRequestsToComplete(long timeoutMs) {
        long startTime = System.currentTimeMillis();

        while (super.getActiveRequestCount() > 0
                && (System.currentTimeMillis() - startTime) < timeoutMs) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }

        return super.getActiveRequestCount() == 0;
    }

    // ========== 统计功能已移至StatisticsAware基类 ==========
}
