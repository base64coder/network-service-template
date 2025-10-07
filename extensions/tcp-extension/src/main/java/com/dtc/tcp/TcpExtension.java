package com.dtc.tcp;

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
import com.dtc.core.tcp.TcpServer;
import com.dtc.core.tcp.TcpMessageHandler;
import com.dtc.core.tcp.TcpConnectionManager;
import com.dtc.core.tcp.TcpProtocolHandler;
import com.dtc.core.statistics.StatisticsAware;
import com.dtc.core.messaging.NetworkMessageEvent;
import com.dtc.core.messaging.NetworkMessageQueue;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TCP协议扩展示例
 * 实现TCP协议的基本功能
 * 
 * @author Network Service Template
 */
@Singleton
public class TcpExtension extends StatisticsAware implements ExtensionMain, ProtocolExtension, NetworkExtension,
        GracefulShutdownExtension {

    private static final Logger log = LoggerFactory.getLogger(TcpExtension.class);

    private final TcpServer tcpServer;
    private final TcpMessageHandler messageHandler;
    private final TcpConnectionManager connectionManager;
    private final TcpProtocolHandler protocolHandler;
    private final NetworkMessageQueue messageQueue;

    private volatile boolean started = false;
    private volatile boolean enabled = true;
    private volatile boolean shutdownPrepared = false;

    // 连接管理
    private final ConcurrentHashMap<String, ChannelHandlerContext> activeConnections = new ConcurrentHashMap<>();

    @Inject
    public TcpExtension(@NotNull TcpServer tcpServer,
            @NotNull TcpMessageHandler messageHandler,
            @NotNull TcpConnectionManager connectionManager,
            @NotNull TcpProtocolHandler protocolHandler,
            @NotNull NetworkMessageQueue messageQueue,
            @NotNull com.dtc.core.statistics.StatisticsCollector statisticsCollector) {
        super(statisticsCollector);
        this.tcpServer = tcpServer;
        this.messageHandler = messageHandler;
        this.connectionManager = connectionManager;
        this.protocolHandler = protocolHandler;
        this.messageQueue = messageQueue;
    }

    @Override
    public void extensionStart(@NotNull ExtensionStartInput input, @NotNull ExtensionStartOutput output) {
        log.info("Starting TCP Extension v{}", input.getExtensionVersion());

        try {
            // 初始化TCP协议处理器
            initializeTcpHandler();

            started = true;
            log.info("TCP Extension started successfully");
        } catch (Exception e) {
            log.error("Failed to start TCP Extension", e);
            output.preventStartup("Failed to initialize TCP handler: " + e.getMessage());
        }
    }

    @Override
    public void extensionStop(@NotNull ExtensionStopInput input, @NotNull ExtensionStopOutput output) {
        log.info("Stopping TCP Extension v{}", input.getExtensionVersion());

        try {
            // 清理TCP协议资源
            cleanupTcpHandler();

            started = false;
            log.info("TCP Extension stopped successfully");
        } catch (Exception e) {
            log.error("Failed to stop TCP Extension", e);
            output.preventStop("Failed to cleanup TCP handler: " + e.getMessage());
        }
    }

    @Override
    @NotNull
    public String getProtocolName() {
        return ServiceConfig.TCP.getServiceName();
    }

    @Override
    @NotNull
    public String getProtocolVersion() {
        return "RFC 793";
    }

    @Override
    public int getDefaultPort() {
        return ServiceConfig.TCP.getDefaultPort();
    }

    @Override
    public void onConnect(@NotNull ChannelHandlerContext ctx, @NotNull String clientId) {
        log.info("TCP client connected: {} from {}", clientId, ctx.channel().remoteAddress());

        // 添加连接到活跃连接管理
        activeConnections.put(clientId, ctx);
        connectionManager.addConnection(clientId, ctx);

        // 使用协议处理器处理连接
        protocolHandler.handleConnect(ctx, clientId);
    }

    @Override
    public void onDisconnect(@NotNull ChannelHandlerContext ctx, @NotNull String clientId) {
        log.info("TCP client disconnected: {}", clientId);

        // 从活跃连接中移除
        activeConnections.remove(clientId);
        connectionManager.removeConnection(clientId);

        // 使用协议处理器处理断开连接
        protocolHandler.handleDisconnect(ctx, clientId);
    }

    @Override
    public void onMessage(@NotNull ChannelHandlerContext ctx, @NotNull Object message) {
        log.debug("📨 TCP message received from client: {}", ctx.channel().remoteAddress());

        try {
            // 处理 TCP 消息 - 使用 Disruptor 异步处理
            if (message != null) {
                // 创建网络消息事件
                NetworkMessageEvent event = createNetworkMessageEvent(ctx, message);

                // 发布到 Disruptor 队列进行异步处理
                boolean published = messageQueue.publish(event);
                if (published) {
                    log.debug("✅ TCP message published to Disruptor queue: {}", event.getEventId());
                } else {
                    log.error("❌ Failed to publish TCP message to Disruptor queue");
                    // 如果发布失败，发送错误响应
                    sendErrorResponse(ctx, "Service temporarily unavailable");
                }
            } else {
                log.warn("⚠️ Received null message in TCP extension");
            }
        } catch (Exception e) {
            log.error("❌ Error handling TCP message from client: {}", ctx.channel().remoteAddress(), e);
            sendErrorResponse(ctx, "Internal server error");
        }
    }

    @Override
    public void onException(@NotNull ChannelHandlerContext ctx, @NotNull Throwable cause) {
        log.error("TCP protocol error from client: {}", ctx.channel().remoteAddress(), cause);

        // 使用协议处理器处理异常
        protocolHandler.handleException(ctx, cause);
    }

    @Override
    @Nullable
    public MessageHandler getMessageHandler() {
        return new TcpMessageHandler();
    }

    /**
     * 处理TCP消息
     */
    private void handleTcpMessage(@NotNull ChannelHandlerContext ctx, @NotNull ByteBuf message) {
        try {
            // 读取消息内容
            byte[] data = new byte[message.readableBytes()];
            message.getBytes(message.readerIndex(), data);

            // 处理消息内容
            String content = new String(data);
            log.debug("Processing TCP message: {}", content);

            // 这里可以实现具体的TCP消息处理逻辑
            // 例如：协议解析、消息路由、数据转换等
            processTcpData(ctx, content);

        } catch (Exception e) {
            log.error("Failed to process TCP message", e);
        }
    }

    /**
     * 处理TCP数据
     */
    private void processTcpData(@NotNull ChannelHandlerContext ctx, @NotNull String data) {
        // 这里可以实现具体的TCP数据处理逻辑
        // 例如：自定义协议解析、消息格式验证等
        log.debug("Processing TCP data: {}", data);

        // 示例：简单的回显处理
        if (data.trim().equals("ping")) {
            sendResponse(ctx, "pong");
        } else if (data.trim().equals("hello")) {
            sendResponse(ctx, "Hello from TCP Extension!");
        } else {
            sendResponse(ctx, "Echo: " + data);
        }
    }

    /**
     * 发送响应
     */
    private void sendResponse(@NotNull ChannelHandlerContext ctx, @NotNull String response) {
        try {
            ByteBuf buffer = ctx.alloc().buffer();
            buffer.writeBytes(response.getBytes());
            ctx.writeAndFlush(buffer);
            log.debug("Sent TCP response: {}", response);
        } catch (Exception e) {
            log.error("Failed to send TCP response", e);
        }
    }

    /**
     * 发送欢迎消息
     */
    private void sendWelcomeMessage(@NotNull ChannelHandlerContext ctx, @NotNull String clientId) {
        String welcome = "Welcome to TCP Extension! Client ID: " + clientId;
        sendResponse(ctx, welcome);
    }

    /**
     * 清理客户端会话
     */
    private void cleanupClientSession(@NotNull String clientId) {
        log.debug("Cleaning up session for client: {}", clientId);
        // 这里可以实现会话清理逻辑
        // 例如：清理缓存、释放资源等
    }

    /**
     * 处理TCP异常
     */
    private void handleTcpException(@NotNull ChannelHandlerContext ctx, @NotNull Throwable cause) {
        // 根据异常类型进行不同处理
        if (cause instanceof java.io.IOException) {
            log.warn("TCP connection lost: {}", cause.getMessage());
        } else {
            log.error("Unexpected TCP error", cause);
        }

        // 可以选择关闭连接或发送错误响应
        // ctx.close();
    }

    /**
     * 初始化TCP处理器
     */
    private void initializeTcpHandler() {
        log.info("Initializing TCP protocol handler...");
        // 初始化TCP协议相关的组件
        // 例如：配置缓冲区大小、设置超时时间等
    }

    /**
     * 清理TCP处理器
     */
    private void cleanupTcpHandler() {
        log.info("Cleaning up TCP protocol handler...");
        // 清理TCP协议相关的资源
        // 例如：关闭连接、释放缓冲区等
    }

    /**
     * TCP消息处理器
     */
    private static class TcpMessageHandler implements MessageHandler {

        @Override
        @Nullable
        public Object handleMessage(@NotNull ChannelHandlerContext ctx, @NotNull Object message) {
            log.debug("Handling TCP message: {}", message.getClass().getSimpleName());

            // 处理接收到的TCP消息
            // 这里可以实现具体的TCP消息处理逻辑
            // 例如：消息验证、协议解析等

            return null; // 继续处理链
        }

        @Override
        @Nullable
        public Object handleOutboundMessage(@NotNull ChannelHandlerContext ctx, @NotNull Object message) {
            log.debug("Handling outbound TCP message: {}", message.getClass().getSimpleName());

            // 处理发送的TCP消息
            // 这里可以实现TCP消息的预处理逻辑
            // 例如：消息格式化、协议封装等

            return message; // 发送消息
        }

        @Override
        public int getPriority() {
            return 70; // TCP消息处理器优先级
        }

        @Override
        public boolean supports(@NotNull Class<?> messageType) {
            // 检查是否支持该消息类型
            return messageType.getName().contains("ByteBuf") ||
                    messageType.getName().contains("tcp") ||
                    messageType.getName().contains("TCP");
        }
    }

    // NetworkExtension 实现
    @Override
    @NotNull
    public String getId() {
        return "tcp-extension";
    }

    @Override
    @NotNull
    public String getName() {
        return "TCP Protocol Extension";
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
        return 70;
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
            log.info("Starting TCP extension...");
            started = true;
            log.info("TCP extension started successfully");
        }
    }

    @Override
    public void stop() throws Exception {
        if (started) {
            log.info("Stopping TCP extension...");
            started = false;
            log.info("TCP extension stopped successfully");
        }
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        log.info("TCP extension {} {}", getId(), enabled ? "enabled" : "disabled");
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
        log.info("Cleaning up TCP extension: {} (disable: {})", getId(), disable);
        if (disable) {
            setEnabled(false);
        }
    }

    // ========== GracefulShutdownExtension 实现 ==========

    @Override
    public void prepareForShutdown() throws Exception {
        log.info("Preparing TCP extension for shutdown...");
        shutdownPrepared = true;

        // 停止接收新的 TCP 连接
        // 这里可以关闭端口、移除路由等
        log.info("TCP extension prepared for shutdown");
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

    // ========== 统计功能已移至StatisticsAware基类 ==========

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
        log.info("Gracefully closing {} active TCP connections", activeConnections.size());

        for (String clientId : activeConnections.keySet()) {
            try {
                ChannelHandlerContext ctx = activeConnections.get(clientId);
                if (ctx != null && ctx.channel().isActive()) {
                    // 发送关闭通知
                    sendShutdownNotification(ctx, clientId);
                    // 关闭连接
                    ctx.close();
                }
            } catch (Exception e) {
                log.warn("Failed to close connection for client: {}", clientId, e);
            }
        }

        activeConnections.clear();
        log.info("All TCP connections closed gracefully");
    }

    /**
     * 发送关闭通知
     */
    private void sendShutdownNotification(@NotNull ChannelHandlerContext ctx, @NotNull String clientId) {
        try {
            String shutdownMsg = "Server is shutting down. Connection will be closed.";
            ByteBuf buffer = ctx.alloc().buffer();
            buffer.writeBytes(shutdownMsg.getBytes());
            ctx.writeAndFlush(buffer);
            log.debug("Sent shutdown notification to client: {}", clientId);
        } catch (Exception e) {
            log.warn("Failed to send shutdown notification to client: {}", clientId, e);
        }
    }

    // ========== 辅助方法 ==========

    /**
     * 创建网络消息事件
     */
    @NotNull
    private NetworkMessageEvent createNetworkMessageEvent(@NotNull ChannelHandlerContext ctx, @NotNull Object message) {
        // 生成客户端ID
        String clientId = "client-" + System.currentTimeMillis();

        // 计算消息大小
        int messageSize = 0;
        if (message instanceof ByteBuf) {
            messageSize = ((ByteBuf) message).readableBytes();
        } else if (message instanceof byte[]) {
            messageSize = ((byte[]) message).length;
        }

        return NetworkMessageEvent.builder()
                .protocolType("tcp")
                .clientId(clientId)
                .message(message)
                .channelContext(ctx)
                .sourceAddress(ctx.channel().remoteAddress().toString())
                .messageSize(messageSize)
                .messageType("TCP_MESSAGE")
                .isRequest(true)
                .priority(4) // TCP消息优先级
                .build();
    }

    /**
     * 发送错误响应
     */
    private void sendErrorResponse(@NotNull ChannelHandlerContext ctx, @NotNull String errorMessage) {
        try {
            // TCP错误响应处理
            String errorMsg = "ERROR: " + errorMessage;
            ByteBuf buffer = ctx.alloc().buffer();
            buffer.writeBytes(errorMsg.getBytes());
            ctx.writeAndFlush(buffer);
        } catch (Exception e) {
            log.error("❌ Failed to send error response to TCP client: {}", ctx.channel().remoteAddress(), e);
        }
    }
}
