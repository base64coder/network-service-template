package com.dtc.ws;

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
import com.dtc.core.network.websocket.WebSocketServer;
import com.dtc.core.network.websocket.WebSocketMessageHelper;
import com.dtc.core.network.websocket.WebSocketConnectionManager;
import com.dtc.core.statistics.StatisticsAware;
import com.dtc.core.messaging.NetworkMessageEvent;
import com.dtc.core.messaging.NetworkMessageQueue;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 协议扩展
 * 实现 WebSocket 协议扩展的功能和逻辑
 * 
 * @author Network Service Template
 */
@Singleton
public class WebSocketExtension extends StatisticsAware implements ExtensionMain, ProtocolExtension, NetworkExtension,
        GracefulShutdownExtension {

    private static final Logger log = LoggerFactory.getLogger(WebSocketExtension.class);

    private final WebSocketServer webSocketServer;
    private final WebSocketMessageHelper messageHelper;
    private final WebSocketConnectionManager connectionManager;
    private final NetworkMessageQueue messageQueue;

    private volatile boolean started = false;
    private volatile boolean enabled = true;
    private volatile boolean shutdownPrepared = false;

    // 活跃连接
    private final ConcurrentHashMap<String, ChannelHandlerContext> activeConnections = new ConcurrentHashMap<>();

    @Inject
    public WebSocketExtension(@NotNull WebSocketServer webSocketServer,
            @NotNull WebSocketMessageHelper messageHelper,
            @NotNull WebSocketConnectionManager connectionManager,
            @NotNull NetworkMessageQueue messageQueue,
            @NotNull com.dtc.core.statistics.StatisticsCollector statisticsCollector) {
        super(statisticsCollector);
        this.webSocketServer = webSocketServer;
        this.messageHelper = messageHelper;
        this.connectionManager = connectionManager;
        this.messageQueue = messageQueue;
    }

    @Override
    public void extensionStart(@NotNull ExtensionStartInput input, @NotNull ExtensionStartOutput output) {
        log.info("Starting WebSocket Extension v{}", input.getExtensionVersion());

        try {
            // 初始化 WebSocket 协议处理器
            initializeWebSocketHandler();

            started = true;
            log.info("WebSocket Extension started successfully");
        } catch (Exception e) {
            log.error("Failed to start WebSocket Extension", e);
            output.preventStartup("Failed to initialize WebSocket handler: " + e.getMessage());
        }
    }

    @Override
    public void extensionStop(@NotNull ExtensionStopInput input, @NotNull ExtensionStopOutput output) {
        log.info("Stopping WebSocket Extension v{}", input.getExtensionVersion());

        try {
            // 清理 WebSocket 协议扩展资源
            cleanupWebSocketHandler();

            started = false;
            log.info("WebSocket Extension stopped successfully");
        } catch (Exception e) {
            log.error("Failed to stop WebSocket Extension", e);
            output.preventStop("Failed to cleanup WebSocket handler: " + e.getMessage());
        }
    }

    @Override
    @NotNull
    public String getProtocolName() {
        return ServiceConfig.WEBSOCKET.getServiceName();
    }

    @Override
    @NotNull
    public String getProtocolVersion() {
        return "RFC 6455";
    }

    @Override
    public int getDefaultPort() {
        return ServiceConfig.WEBSOCKET.getDefaultPort();
    }

    @Override
    public void onConnect(@NotNull ChannelHandlerContext ctx, @NotNull String clientId) {
        log.info("WebSocket client connected: {} from {}", clientId, ctx.channel().remoteAddress());

        // 保存活跃连接到连接映射
        activeConnections.put(clientId, ctx);

        // 处理 WebSocket 连接
        // 这里可以根据需要添加 WebSocket 握手后的连接处理逻辑
    }

    @Override
    public void onDisconnect(@NotNull ChannelHandlerContext ctx, @NotNull String clientId) {
        log.info("WebSocket client disconnected: {}", clientId);

        // 移除活跃连接
        activeConnections.remove(clientId);

        // 处理 WebSocket 断开连接
        // 这里可以根据需要添加 WebSocket 关闭处理逻辑
    }

    @Override
    public void onMessage(@NotNull ChannelHandlerContext ctx, @NotNull Object message) {
        log.debug("WebSocket message received from client: {}", ctx.channel().remoteAddress());

        try {
            // 处理 WebSocket 消息 - 通过 Disruptor 异步处理
            if (message instanceof WebSocketFrame) {
                WebSocketFrame webSocketFrame = (WebSocketFrame) message;

                // 创建网络消息事件
                NetworkMessageEvent event = createNetworkMessageEvent(ctx, webSocketFrame);

                // 发布到 Disruptor 队列进行异步处理
                boolean published = messageQueue.publish(event);
                if (published) {
                    log.debug("WebSocket message published to Disruptor queue: {}", event.getEventId());
                } else {
                    log.error("Failed to publish WebSocket message to Disruptor queue");
                    // 如果发布失败，发送错误响应
                    sendErrorResponse(ctx, "Service temporarily unavailable");
                }
            } else {
                log.warn("Received unexpected message type in WebSocket extension: {}",
                        message.getClass().getSimpleName());
            }
        } catch (Exception e) {
            log.error("Error handling WebSocket message from client: {}", ctx.channel().remoteAddress(), e);
            sendErrorResponse(ctx, "Internal server error");
        }
    }

    @Override
    public void onException(@NotNull ChannelHandlerContext ctx, @NotNull Throwable cause) {
        log.error("WebSocket protocol error from client: {}", ctx.channel().remoteAddress(), cause);

        // 处理 WebSocket 协议错误
        // 这里可以根据需要添加错误处理逻辑，如关闭连接等
    }

    @Override
    @Nullable
    public MessageHandler getMessageHandler() {
        return new WebSocketMessageHandler();
    }

    /**
     * 处理 WebSocket 帧
     */
    private void handleWebSocketFrame(@NotNull ChannelHandlerContext ctx, @NotNull WebSocketFrame frame) {
        if (frame instanceof TextWebSocketFrame) {
            TextWebSocketFrame textFrame = (TextWebSocketFrame) frame;
            log.debug("Received text frame: {}", textFrame.text());

            // 处理文本消息
            handleTextMessage(ctx, textFrame.text());
        } else {
            log.debug("Received binary frame: {} bytes", frame.content().readableBytes());

            // 处理二进制消息
            handleBinaryMessage(ctx, frame.content());
        }
    }

    /**
     * 处理文本消息
     */
    private void handleTextMessage(@NotNull ChannelHandlerContext ctx, @NotNull String text) {
        // 这里可以根据需要添加文本消息处理逻辑
        // 例如解析 JSON 格式的消息等
        log.debug("Processing text message: {}", text);
    }

    /**
     * 处理二进制消息
     */
    private void handleBinaryMessage(@NotNull ChannelHandlerContext ctx, @NotNull io.netty.buffer.ByteBuf content) {
        // 这里可以根据需要添加二进制消息处理逻辑
        // 例如协议格式的数据解析等
        log.debug("Processing binary message: {} bytes", content.readableBytes());
    }

    /**
     * 初始化 WebSocket 协议处理器
     */
    private void initializeWebSocketHandler() {
        log.info("Initializing WebSocket protocol handler...");
        // 初始化 WebSocket 协议扩展相关资源
    }

    /**
     * 清理 WebSocket 协议处理器
     */
    private void cleanupWebSocketHandler() {
        log.info("Cleaning up WebSocket protocol handler...");
        // 清理 WebSocket 协议扩展相关资源
    }

    /**
     * WebSocket 消息处理器
     */
    private static class WebSocketMessageHandler implements MessageHandler {

        @Override
        @Nullable
        public Object handleMessage(@NotNull ChannelHandlerContext ctx, @NotNull Object message) {
            log.debug("Handling WebSocket message: {}", message.getClass().getSimpleName());

            // 处理接收到的 WebSocket 消息
            // 这里可以根据需要添加自定义消息处理逻辑

            return null; // 不返回响应
        }

        @Override
        @Nullable
        public Object handleOutboundMessage(@NotNull ChannelHandlerContext ctx, @NotNull Object message) {
            log.debug("Handling outbound WebSocket message: {}", message.getClass().getSimpleName());

            // 处理发送的 WebSocket 消息
            // 这里可以根据需要添加发送消息处理逻辑

            return message; // 发送消息
        }

        @Override
        public int getPriority() {
            return 60; // WebSocket 消息处理器优先级
        }

        @Override
        public boolean supports(@NotNull Class<?> messageType) {
            // 检查是否支持该类型的消息
            return messageType.getName().contains("WebSocket") ||
                    messageType.getName().contains("websocket");
        }
    }

    // NetworkExtension 接口实现
    @Override
    @NotNull
    public String getId() {
        return "websocket-extension";
    }

    @Override
    @NotNull
    public String getName() {
        return "WebSocket Protocol Extension";
    }

    @Override
    @NotNull
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    @Nullable
    public String getAuthor() {
        return "Network Service Template";
    }

    @Override
    public int getPriority() {
        return 60;
    }

    @Override
    public int getStartPriority() {
        return 1000;
    }

    @Override
    @NotNull
    public ExtensionMetadata getMetadata() {
        return ExtensionMetadata.builder()
                .id(getId())
                .name(getName())
                .version(getVersion())
                .author(getAuthor())
                .priority(getPriority())
                .startPriority(getStartPriority())
                .build();
    }

    @Override
    @NotNull
    public Path getExtensionFolderPath() {
        return Paths.get("extensions", getId());
    }

    @Override
    @Nullable
    public ClassLoader getExtensionClassloader() {
        return this.getClass().getClassLoader();
    }

    @Override
    public void start() throws Exception {
        if (!started) {
            log.info("Starting WebSocket extension...");
            started = true;
            log.info("WebSocket extension started successfully");
        }
    }

    @Override
    public void stop() throws Exception {
        if (started) {
            log.info("Stopping WebSocket extension...");
            started = false;
            log.info("WebSocket extension stopped successfully");
        }
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        log.info("WebSocket extension {} {}", getId(), enabled ? "enabled" : "disabled");
    }

    @Override
    public boolean isStarted() {
        return started;
    }

    @Override
    public boolean isStopped() {
        return !started;
    }

    @Override
    public void cleanup(boolean disable) {
        log.info("Cleaning up WebSocket extension: {} (disable: {})", getId(), disable);
        if (disable) {
            setEnabled(false);
        }
    }

    // ========== GracefulShutdownExtension 接口实现 ==========

    @Override
    public void prepareForShutdown() throws Exception {
        log.info("Preparing WebSocket extension for shutdown...");
        shutdownPrepared = true;

        // 停止接收新的 WebSocket 连接
        // 这里可以关闭连接、移除路由等操作
        log.info("WebSocket extension prepared for shutdown");
    }

    @Override
    public boolean canShutdownSafely() {
        return getActiveRequestCount() == 0 && activeConnections.isEmpty();
    }

    @Override
    public long getActiveRequestCount() {
        return super.getActiveRequestCount();
    }

    @Override
    public boolean waitForRequestsToComplete(long timeoutMs) {
        long startTime = System.currentTimeMillis();

        while (getActiveRequestCount() > 0 && (System.currentTimeMillis() - startTime) < timeoutMs) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }

        return getActiveRequestCount() == 0;
    }

    // ========== 连接管理方法（继承自StatisticsAware基类） ==========

    /**
     * 获取活跃连接数量
     */
    public int getActiveConnectionCount() {
        return activeConnections.size();
    }

    /**
     * 获取所有活跃连接
     */
    public ConcurrentHashMap<String, ChannelHandlerContext> getActiveConnectionsMap() {
        return new ConcurrentHashMap<>(activeConnections);
    }

    /**
     * 优雅关闭所有连接
     */
    public void gracefulCloseAllConnections() {
        log.info("Gracefully closing {} active WebSocket connections", activeConnections.size());

        for (String clientId : activeConnections.keySet()) {
            try {
                ChannelHandlerContext ctx = activeConnections.get(clientId);
                if (ctx != null && ctx.channel().isActive()) {
                    // 发送 WebSocket 关闭帧
                    sendWebSocketCloseFrame(ctx, clientId);
                    // 关闭连接
                    ctx.close();
                }
            } catch (Exception e) {
                log.warn("Failed to close WebSocket connection for client: {}", clientId, e);
            }
        }

        activeConnections.clear();
        log.info("All WebSocket connections closed gracefully");
    }

    /**
     * 发送 WebSocket 关闭帧
     */
    private void sendWebSocketCloseFrame(@NotNull ChannelHandlerContext ctx, @NotNull String clientId) {
        try {
            // 发送关闭帧
            TextWebSocketFrame closeFrame = new TextWebSocketFrame(
                    "Server is shutting down. Connection will be closed.");
            ctx.writeAndFlush(closeFrame);
            log.debug("Sent WebSocket close frame to client: {}", clientId);
        } catch (Exception e) {
            log.warn("Failed to send WebSocket close frame to client: {}", clientId, e);
        }
    }

    // ========== 辅助方法 ==========

    /**
     * 创建网络消息事件
     */
    @NotNull
    private NetworkMessageEvent createNetworkMessageEvent(@NotNull ChannelHandlerContext ctx,
            @NotNull WebSocketFrame webSocketFrame) {
        // 生成客户端ID
        String clientId = "client-" + System.currentTimeMillis();

        // 计算消息大小
        int messageSize = webSocketFrame.content() != null ? webSocketFrame.content().readableBytes() : 0;

        return NetworkMessageEvent.builder()
                .protocolType("websocket")
                .clientId(clientId)
                .message(webSocketFrame)
                .channelContext(ctx)
                .sourceAddress(ctx.channel().remoteAddress().toString())
                .messageSize(messageSize)
                .messageType("WEBSOCKET_FRAME")
                .isRequest(true)
                .priority(2) // WebSocket 消息优先级
                .build();
    }

    /**
     * 发送错误响应
     */
    private void sendErrorResponse(@NotNull ChannelHandlerContext ctx, @NotNull String errorMessage) {
        try {
            TextWebSocketFrame errorFrame = new TextWebSocketFrame("ERROR: " + errorMessage);
            ctx.writeAndFlush(errorFrame);
        } catch (Exception e) {
            log.error("Failed to send error response to WebSocket client: {}", ctx.channel().remoteAddress(), e);
        }
    }
}
