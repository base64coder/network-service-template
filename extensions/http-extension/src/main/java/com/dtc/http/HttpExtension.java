package com.dtc.http;

import com.dtc.api.ExtensionMain;
import com.dtc.api.ProtocolExtension;
import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import com.dtc.core.extensions.NetworkExtension;
import com.dtc.core.http.HttpServer;
import com.dtc.core.http.HttpRequestHandler;
import com.dtc.core.http.HttpResponseHandler;
import com.dtc.core.http.HttpRouteManager;
import com.dtc.core.http.HttpMiddlewareManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * HTTP REST 协议扩展
 * 提供 HTTP 请求处理、路由管理、中间件支持等功能
 * 
 * @author Network Service Template
 */
@Singleton
public class HttpExtension implements ExtensionMain, ProtocolExtension, NetworkExtension {

    private static final Logger log = LoggerFactory.getLogger(HttpExtension.class);

    private final HttpServer httpServer;
    private final HttpRequestHandler requestHandler;
    private final HttpResponseHandler responseHandler;
    private final HttpRouteManager routeManager;
    private final HttpMiddlewareManager middlewareManager;
    
    private final AtomicBoolean started = new AtomicBoolean(false);
    private final AtomicBoolean enabled = new AtomicBoolean(true);

    @Inject
    public HttpExtension(@NotNull HttpServer httpServer,
                        @NotNull HttpRequestHandler requestHandler,
                        @NotNull HttpResponseHandler responseHandler,
                        @NotNull HttpRouteManager routeManager,
                        @NotNull HttpMiddlewareManager middlewareManager) {
        this.httpServer = httpServer;
        this.requestHandler = requestHandler;
        this.responseHandler = responseHandler;
        this.routeManager = routeManager;
        this.middlewareManager = middlewareManager;
    }

    // ========== ExtensionMain 接口实现 ==========

    @Override
    public void extensionStart() {
        if (started.compareAndSet(false, true)) {
            log.info("🚀 Starting HTTP REST extension...");
            
            try {
                // 初始化路由
                initializeRoutes();
                
                // 初始化中间件
                initializeMiddleware();
                
                // 启动 HTTP 服务器
                httpServer.start();
                
                log.info("✅ HTTP REST extension started successfully on port {}", getDefaultPort());
            } catch (Exception e) {
                log.error("❌ Failed to start HTTP REST extension", e);
                started.set(false);
                throw new RuntimeException("Failed to start HTTP REST extension", e);
            }
        }
    }

    @Override
    public void extensionStop() {
        if (started.compareAndSet(true, false)) {
            log.info("🛑 Stopping HTTP REST extension...");
            
            try {
                // 停止 HTTP 服务器
                httpServer.stop();
                
                log.info("✅ HTTP REST extension stopped successfully");
            } catch (Exception e) {
                log.error("❌ Error stopping HTTP REST extension", e);
            }
        }
    }

    @Override
    public boolean isEnabled() {
        return enabled.get();
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled.set(enabled);
        log.info("HTTP REST extension {} {}", enabled ? "enabled" : "disabled");
    }

    // ========== ProtocolExtension 接口实现 ==========

    @Override
    @NotNull
    public String getProtocolName() {
        return "HTTP";
    }

    @Override
    @NotNull
    public String getProtocolVersion() {
        return "1.1";
    }

    @Override
    public int getDefaultPort() {
        return 8080;
    }

    @Override
    public void onConnect(@NotNull String clientId, @NotNull Map<String, Object> connectionInfo) {
        log.info("🔗 HTTP client connected: {}", clientId);
        // HTTP 连接处理逻辑
    }

    @Override
    public void onDisconnect(@NotNull String clientId, @NotNull Map<String, Object> connectionInfo) {
        log.info("🔌 HTTP client disconnected: {}", clientId);
        // HTTP 断开连接处理逻辑
    }

    @Override
    public void onMessage(@NotNull String clientId, @NotNull Object message, @NotNull Map<String, Object> context) {
        log.debug("📨 HTTP message received from client: {}", clientId);
        
        try {
            // 处理 HTTP 消息
            if (message instanceof com.dtc.core.http.HttpRequest) {
                com.dtc.core.http.HttpRequest httpRequest = (com.dtc.core.http.HttpRequest) message;
                com.dtc.core.http.HttpResponse httpResponse = requestHandler.handleRequest(httpRequest);
                
                // 发送响应
                responseHandler.sendResponse(clientId, httpResponse);
            }
        } catch (Exception e) {
            log.error("❌ Error handling HTTP message from client: {}", clientId, e);
        }
    }

    @Override
    public void onException(@NotNull String clientId, @NotNull Throwable exception, @NotNull Map<String, Object> context) {
        log.error("💥 HTTP exception for client: {}", clientId, exception);
        
        try {
            // 创建错误响应
            com.dtc.core.http.HttpResponse errorResponse = responseHandler.createErrorResponse(500, 
                "Internal Server Error", exception.getMessage());
            responseHandler.sendResponse(clientId, errorResponse);
        } catch (Exception e) {
            log.error("❌ Failed to send error response to client: {}", clientId, e);
        }
    }

    @Override
    @Nullable
    public com.dtc.api.MessageHandler getMessageHandler() {
        return new HttpMessageHandler();
    }

    // ========== NetworkExtension 接口实现 ==========

    @Override
    public boolean isStarted() {
        return started.get();
    }

    @Override
    public void start() {
        extensionStart();
    }

    @Override
    public void stop() {
        extensionStop();
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
        middlewareManager.addMiddleware(new com.dtc.core.http.middleware.CorsMiddleware());
        middlewareManager.addMiddleware(new com.dtc.core.http.middleware.LoggingMiddleware());
        middlewareManager.addMiddleware(new com.dtc.core.http.middleware.AuthMiddleware());
        middlewareManager.addMiddleware(new com.dtc.core.http.middleware.RateLimitMiddleware());
        
        log.info("✅ HTTP middleware initialized successfully");
    }

    // ========== 路由处理方法 ==========

    private com.dtc.core.http.HttpResponse handleRoot(com.dtc.core.http.HttpRequest request) {
        return responseHandler.createJsonResponse(200, Map.of(
            "message", "Welcome to HTTP REST API",
            "version", "1.0.0",
            "timestamp", System.currentTimeMillis()
        ));
    }

    private com.dtc.core.http.HttpResponse handleHealth(com.dtc.core.http.HttpRequest request) {
        return responseHandler.createJsonResponse(200, Map.of(
            "status", "healthy",
            "timestamp", System.currentTimeMillis()
        ));
    }

    private com.dtc.core.http.HttpResponse handleStatus(com.dtc.core.http.HttpRequest request) {
        return responseHandler.createJsonResponse(200, Map.of(
            "status", "running",
            "uptime", System.currentTimeMillis(),
            "activeConnections", httpServer.getActiveConnections()
        ));
    }

    private com.dtc.core.http.HttpResponse handleApiInfo(com.dtc.core.http.HttpRequest request) {
        return responseHandler.createJsonResponse(200, Map.of(
            "name", "Network Service Template HTTP API",
            "version", "1.0.0",
            "description", "RESTful API for network service template",
            "endpoints", Map.of(
                "users", "/api/users",
                "orders", "/api/orders",
                "products", "/api/products"
            )
        ));
    }

    // 用户相关路由
    private com.dtc.core.http.HttpResponse handleGetUsers(com.dtc.core.http.HttpRequest request) {
        // 实现获取用户列表逻辑
        return responseHandler.createJsonResponse(200, Map.of(
            "users", java.util.Arrays.asList(
                Map.of("id", 1, "name", "John Doe", "email", "john@example.com"),
                Map.of("id", 2, "name", "Jane Smith", "email", "jane@example.com")
            )
        ));
    }

    private com.dtc.core.http.HttpResponse handleCreateUser(com.dtc.core.http.HttpRequest request) {
        // 实现创建用户逻辑
        return responseHandler.createJsonResponse(201, Map.of(
            "message", "User created successfully",
            "id", System.currentTimeMillis()
        ));
    }

    private com.dtc.core.http.HttpResponse handleGetUser(com.dtc.core.http.HttpRequest request) {
        // 实现获取单个用户逻辑
        String userId = request.getPathParameters().get("id");
        return responseHandler.createJsonResponse(200, Map.of(
            "id", userId,
            "name", "John Doe",
            "email", "john@example.com"
        ));
    }

    private com.dtc.core.http.HttpResponse handleUpdateUser(com.dtc.core.http.HttpRequest request) {
        // 实现更新用户逻辑
        String userId = request.getPathParameters().get("id");
        return responseHandler.createJsonResponse(200, Map.of(
            "message", "User updated successfully",
            "id", userId
        ));
    }

    private com.dtc.core.http.HttpResponse handleDeleteUser(com.dtc.core.http.HttpRequest request) {
        // 实现删除用户逻辑
        String userId = request.getPathParameters().get("id");
        return responseHandler.createJsonResponse(200, Map.of(
            "message", "User deleted successfully",
            "id", userId
        ));
    }

    // 订单相关路由
    private com.dtc.core.http.HttpResponse handleGetOrders(com.dtc.core.http.HttpRequest request) {
        return responseHandler.createJsonResponse(200, Map.of(
            "orders", java.util.Arrays.asList(
                Map.of("id", 1, "userId", 1, "total", 99.99, "status", "pending"),
                Map.of("id", 2, "userId", 2, "total", 149.99, "status", "completed")
            )
        ));
    }

    private com.dtc.core.http.HttpResponse handleCreateOrder(com.dtc.core.http.HttpRequest request) {
        return responseHandler.createJsonResponse(201, Map.of(
            "message", "Order created successfully",
            "id", System.currentTimeMillis()
        ));
    }

    private com.dtc.core.http.HttpResponse handleGetOrder(com.dtc.core.http.HttpRequest request) {
        String orderId = request.getPathParameters().get("id");
        return responseHandler.createJsonResponse(200, Map.of(
            "id", orderId,
            "userId", 1,
            "total", 99.99,
            "status", "pending"
        ));
    }

    // 产品相关路由
    private com.dtc.core.http.HttpResponse handleGetProducts(com.dtc.core.http.HttpRequest request) {
        return responseHandler.createJsonResponse(200, Map.of(
            "products", java.util.Arrays.asList(
                Map.of("id", 1, "name", "Product A", "price", 99.99),
                Map.of("id", 2, "name", "Product B", "price", 149.99)
            )
        ));
    }

    private com.dtc.core.http.HttpResponse handleCreateProduct(com.dtc.core.http.HttpRequest request) {
        return responseHandler.createJsonResponse(201, Map.of(
            "message", "Product created successfully",
            "id", System.currentTimeMillis()
        ));
    }

    private com.dtc.core.http.HttpResponse handleGetProduct(com.dtc.core.http.HttpRequest request) {
        String productId = request.getPathParameters().get("id");
        return responseHandler.createJsonResponse(200, Map.of(
            "id", productId,
            "name", "Product A",
            "price", 99.99,
            "description", "A great product"
        ));
    }

    // ========== 内部类 ==========

    /**
     * HTTP 消息处理器
     */
    private class HttpMessageHandler implements com.dtc.api.MessageHandler {
        @Override
        public void handleMessage(@NotNull Object message) {
            if (message instanceof com.dtc.core.http.HttpRequest) {
                com.dtc.core.http.HttpRequest httpRequest = (com.dtc.core.http.HttpRequest) message;
                try {
                    com.dtc.core.http.HttpResponse response = requestHandler.handleRequest(httpRequest);
                    log.debug("HTTP request processed: {} {}", httpRequest.getMethod(), httpRequest.getPath());
                } catch (Exception e) {
                    log.error("Error processing HTTP request", e);
                }
            }
        }
    }
}
