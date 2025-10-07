package com.dtc.custom;

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
import com.dtc.core.custom.CustomCodecFactory;
import com.dtc.core.custom.CustomConnectionManager;
import com.dtc.core.custom.CustomServer;
import com.dtc.core.custom.CustomMessageHandler;
import com.dtc.core.statistics.StatisticsAware;
import com.dtc.core.messaging.NetworkMessageEvent;
import com.dtc.core.messaging.NetworkMessageQueue;
import com.dtc.core.extensions.GracefulShutdownExtension;
import com.dtc.core.extensions.NetworkExtension;
import com.dtc.core.extensions.model.ExtensionMetadata;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 自定义协议扩展示例
 * 展示如何通过扩展实现自定义编解码器
 *
 * @author Network Service Template
 */
@Singleton
public class CustomProtocolExtension extends StatisticsAware
        implements ExtensionMain, ProtocolExtension, NetworkExtension,
        GracefulShutdownExtension {

    private static final Logger log = LoggerFactory.getLogger(CustomProtocolExtension.class);

    @SuppressWarnings("unused") // 保留用于依赖注入，但由NettyServer统一管理
    private final CustomServer customServer;
    @SuppressWarnings("unused") // 保留用于依赖注入，但由NettyServer统一管理
    private final CustomMessageHandler customMessageHandler;
    @SuppressWarnings("unused") // 保留用于依赖注入，但由NettyServer统一管理
    private final CustomConnectionManager connectionManager;
    private final CustomCodecFactory customCodec;
    private final NetworkMessageQueue messageQueue;

    private volatile boolean started = false;
    @SuppressWarnings("unused") // 保留用于优雅关闭功能
    private volatile boolean shutdownPrepared = false;

    // 连接管理
    private final ConcurrentHashMap<String, ChannelHandlerContext> activeConnections = new ConcurrentHashMap<>();

    @Inject
    public CustomProtocolExtension(@NotNull CustomServer customServer,
            @NotNull CustomMessageHandler customMessageHandler,
            @NotNull CustomConnectionManager connectionManager,
            @NotNull CustomCodecFactory customCodec,
            @NotNull NetworkMessageQueue messageQueue,
            @NotNull com.dtc.core.statistics.StatisticsCollector statisticsCollector) {
        super(statisticsCollector);
        this.customServer = customServer;
        this.customMessageHandler = customMessageHandler;
        this.connectionManager = connectionManager;
        this.customCodec = customCodec;
        this.messageQueue = messageQueue;
    }

    /**
     * 创建自定义解码器
     */
    @NotNull
    public ChannelHandler createCustomDecoder() {
        // 使用注入的编解码器创建解码器
        return new CustomMessageDecoder(customCodec);
    }

    /**
     * 创建自定义编码器
     */
    @NotNull
    public ChannelHandler createCustomEncoder() {
        // 使用注入的编解码器创建编码器
        return new CustomMessageEncoder(customCodec);
    }

    // ========== ExtensionMain 接口实现 ==========

    @Override
    public void extensionStart(@NotNull ExtensionStartInput input, @NotNull ExtensionStartOutput output) {
        log.info("Starting Custom Protocol Extension v{}", input.getExtensionVersion());

        try {
            // 初始化自定义协议处理器
            initializeCustomHandler();

            started = true;
            log.info("Custom Protocol Extension started successfully");
        } catch (Exception e) {
            log.error("Failed to start Custom Protocol Extension", e);
            output.preventStartup("Failed to initialize custom handler: " + e.getMessage());
        }
    }

    @Override
    public void extensionStop(@NotNull ExtensionStopInput input, @NotNull ExtensionStopOutput output) {
        log.info("Stopping Custom Protocol Extension v{}", input.getExtensionVersion());

        try {
            // 清理自定义协议处理器
            cleanupCustomHandler();

            started = false;
            log.info("Custom Protocol Extension stopped successfully");
        } catch (Exception e) {
            log.error("Failed to stop Custom Protocol Extension", e);
            output.preventStop("Failed to cleanup custom handler: " + e.getMessage());
        }
    }

    // ========== ProtocolExtension 接口实现 ==========

    @Override
    @NotNull
    public String getProtocolName() {
        return ServiceConfig.CUSTOM.getServiceName();
    }

    @Override
    @NotNull
    public String getProtocolVersion() {
        return "1.0.0";
    }

    @Override
    public int getDefaultPort() {
        return ServiceConfig.CUSTOM.getDefaultPort();
    }

    @Override
    public void onConnect(@NotNull ChannelHandlerContext ctx, @NotNull String clientId) {
        log.info("Client {} connected to custom protocol", clientId);

        // 添加连接到活跃连接管理
        activeConnections.put(clientId, ctx);
    }

    @Override
    public void onDisconnect(@NotNull ChannelHandlerContext ctx, @NotNull String clientId) {
        log.info("Client {} disconnected from custom protocol", clientId);

        // 从活跃连接中移除
        activeConnections.remove(clientId);
    }

    @Override
    public void onMessage(@NotNull ChannelHandlerContext ctx, @NotNull Object message) {
        log.debug("📨 Custom protocol message received from client: {}", ctx.channel().remoteAddress());

        try {
            // 处理自定义协议消息 - 使用 Disruptor 异步处理
            if (message != null) {
                // 创建网络消息事件
                NetworkMessageEvent event = createNetworkMessageEvent(ctx, message);

                // 发布到 Disruptor 队列进行异步处理
                boolean published = messageQueue.publish(event);
                if (published) {
                    log.debug("✅ Custom protocol message published to Disruptor queue: {}", event.getEventId());
                } else {
                    log.error("❌ Failed to publish Custom protocol message to Disruptor queue");
                    // 如果发布失败，发送错误响应
                    sendErrorResponse(ctx, "Service temporarily unavailable");
                }
            } else {
                log.warn("⚠️ Received null message in Custom protocol extension");
            }
        } catch (Exception e) {
            log.error("❌ Error handling Custom protocol message from client: {}", ctx.channel().remoteAddress(), e);
            sendErrorResponse(ctx, "Internal server error");
        }
    }

    @Override
    public void onException(@NotNull ChannelHandlerContext ctx, @NotNull Throwable cause) {
        log.error("Custom protocol exception occurred", cause);
    }

    @Override
    @Nullable
    public MessageHandler getMessageHandler() {
        return new CustomProtocolMessageHandler();
    }

    // ========== NetworkExtension 接口实现 ==========

    @Override
    @NotNull
    public String getId() {
        return "custom-extension";
    }

    @Override
    @NotNull
    public String getName() {
        return "Custom Protocol Extension";
    }

    @Override
    @NotNull
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    @NotNull
    public String getAuthor() {
        return "Network Service Template";
    }

    @Override
    public int getPriority() {
        return 100;
    }

    @Override
    public int getStartPriority() {
        return 1000;
    }

    @Override
    @NotNull
    public ExtensionMetadata getMetadata() {
        return ExtensionMetadata.builder()
                .id("custom-extension")
                .name("Custom Protocol Extension")
                .version("1.0.0")
                .author("Network Service Template")
                .description("Custom protocol extension with custom codecs")
                .priority(100)
                .startPriority(1000)
                .mainClass("com.dtc.custom.CustomProtocolExtension")
                .build();
    }

    @Override
    @NotNull
    public java.nio.file.Path getExtensionFolderPath() {
        return java.nio.file.Paths.get("extensions/custom-extension");
    }

    @Override
    @NotNull
    public ClassLoader getExtensionClassloader() {
        return this.getClass().getClassLoader();
    }

    @Override
    public void start() {
        log.info("Starting Custom Protocol Extension");
        started = true;
    }

    @Override
    public void stop() {
        log.info("Stopping Custom Protocol Extension");
        started = false;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public void setEnabled(boolean enabled) {
        // 扩展启用状态由外部控制
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
        log.info("Cleaning up Custom Protocol Extension, disable: {}", disable);
        started = false;
    }

    // ========== 私有方法 ==========

    private void initializeCustomHandler() {
        log.debug("Initializing custom protocol handler");
        // 这里可以添加自定义协议的初始化逻辑
    }

    private void cleanupCustomHandler() {
        log.debug("Cleaning up custom protocol handler");
        // 这里可以添加自定义协议的清理逻辑
    }

    /**
     * 自定义消息处理器
     */
    private class CustomProtocolMessageHandler implements MessageHandler {

        @Override
        @Nullable
        public Object handleMessage(@NotNull ChannelHandlerContext ctx, @NotNull Object message) {
            // 处理接收到的消息
            onMessage(ctx, message);
            return null; // 继续处理链
        }

        @Override
        @Nullable
        public Object handleOutboundMessage(@NotNull ChannelHandlerContext ctx, @NotNull Object message) {
            // 处理发送的消息
            return message; // 直接发送
        }

        @Override
        public int getPriority() {
            return 50; // 中等优先级
        }

        @Override
        public boolean supports(@NotNull Class<?> messageType) {
            return String.class.isAssignableFrom(messageType);
        }
    }

    // ========== GracefulShutdownExtension 实现 ==========

    @Override
    public void prepareForShutdown() throws Exception {
        log.info("Preparing Custom Protocol extension for shutdown...");
        shutdownPrepared = true;

        // 停止接收新的自定义协议连接
        // 这里可以关闭端口、移除路由等
        log.info("Custom Protocol extension prepared for shutdown");
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
        log.info("Gracefully closing {} active Custom Protocol connections", activeConnections.size());

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
                log.warn("Failed to close Custom Protocol connection for client: {}", clientId, e);
            }
        }

        activeConnections.clear();
        log.info("All Custom Protocol connections closed gracefully");
    }

    /**
     * 发送关闭通知
     */
    private void sendShutdownNotification(@NotNull ChannelHandlerContext ctx, @NotNull String clientId) {
        try {
            String shutdownMsg = "Server is shutting down. Connection will be closed.";
            ByteBuf buffer = ctx.alloc().buffer();
            buffer.writeBytes(shutdownMsg.getBytes(StandardCharsets.UTF_8));
            ctx.writeAndFlush(buffer);
            log.debug("Sent shutdown notification to client: {}", clientId);
        } catch (Exception e) {
            log.warn("Failed to send shutdown notification to client: {}", clientId, e);
        }
    }

    // ========== 自定义编解码器 ==========

    /**
     * 自定义消息解码器
     */
    public static class CustomMessageDecoder extends io.netty.handler.codec.MessageToMessageDecoder<ByteBuf> {
        @SuppressWarnings("unused")
        private static final Logger log = LoggerFactory.getLogger(CustomMessageDecoder.class);
        private final CustomCodecFactory customCodec;

        public CustomMessageDecoder(@NotNull CustomCodecFactory customCodec) {
            this.customCodec = customCodec;
        }

        @Override
        protected void decode(ChannelHandlerContext ctx, ByteBuf in, java.util.List<Object> out) throws Exception {
            customCodec.decode(ctx, in, out);
        }
    }

    /**
     * 自定义消息编码器
     */
    public static class CustomMessageEncoder extends io.netty.handler.codec.MessageToByteEncoder<Object> {
        @SuppressWarnings("unused")
        private static final Logger log = LoggerFactory.getLogger(CustomMessageEncoder.class);
        private final CustomCodecFactory customCodec;

        public CustomMessageEncoder(@NotNull CustomCodecFactory customCodec) {
            this.customCodec = customCodec;
        }

        @Override
        protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
            customCodec.encode(ctx, msg, out);
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
        } else if (message instanceof String) {
            messageSize = ((String) message).getBytes(StandardCharsets.UTF_8).length;
        }

        return NetworkMessageEvent.builder()
                .protocolType("custom")
                .clientId(clientId)
                .message(message)
                .channelContext(ctx)
                .sourceAddress(ctx.channel().remoteAddress().toString())
                .messageSize(messageSize)
                .messageType("CUSTOM_MESSAGE")
                .isRequest(true)
                .priority(5) // 自定义协议消息优先级
                .build();
    }

    /**
     * 发送错误响应
     */
    private void sendErrorResponse(@NotNull ChannelHandlerContext ctx, @NotNull String errorMessage) {
        try {
            // 自定义协议错误响应处理
            String errorMsg = "ERROR: " + errorMessage;
            byte[] errorBytes = errorMsg.getBytes(StandardCharsets.UTF_8);

            ByteBuf response = ctx.alloc().buffer(errorBytes.length);
            response.writeBytes(errorBytes);
            ctx.writeAndFlush(response);

        } catch (Exception e) {
            log.error("❌ Failed to send error response to Custom protocol client: {}", ctx.channel().remoteAddress(),
                    e);
        }
    }
}
