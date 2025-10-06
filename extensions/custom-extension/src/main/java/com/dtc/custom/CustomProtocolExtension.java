package com.dtc.custom;

import com.dtc.api.ExtensionMain;
import com.dtc.api.MessageHandler;
import com.dtc.api.ProtocolExtension;
import com.dtc.core.extensions.NetworkExtension;
import com.dtc.core.extensions.GracefulShutdownExtension;
import com.dtc.core.extensions.RequestStatisticsExtension;
import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import com.dtc.api.parameter.ExtensionStartInput;
import com.dtc.api.parameter.ExtensionStartOutput;
import com.dtc.api.parameter.ExtensionStopInput;
import com.dtc.api.parameter.ExtensionStopOutput;
import com.dtc.core.netty.codec.CodecFactory;
import com.dtc.core.netty.codec.MessageDecoder;
import com.dtc.core.netty.codec.MessageEncoder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 自定义协议扩展示例
 * 展示如何通过扩展实现自定义编解码器
 * 
 * @author Network Service Template
 */
@Singleton
public class CustomProtocolExtension implements ExtensionMain, ProtocolExtension, NetworkExtension,
        GracefulShutdownExtension, RequestStatisticsExtension {

    private static final Logger log = LoggerFactory.getLogger(CustomProtocolExtension.class);

    private final @NotNull CodecFactory codecFactory;
    private volatile boolean started = false;
    private final AtomicLong messageCount = new AtomicLong(0);
    private volatile boolean shutdownPrepared = false;

    // 请求统计
    private final AtomicLong totalProcessedRequests = new AtomicLong(0);
    private final AtomicLong errorRequestCount = new AtomicLong(0);
    private final AtomicLong activeRequestCount = new AtomicLong(0);
    private final AtomicLong totalProcessingTime = new AtomicLong(0);

    // 连接管理
    private final ConcurrentHashMap<String, ChannelHandlerContext> activeConnections = new ConcurrentHashMap<>();

    @Inject
    public CustomProtocolExtension(@NotNull CodecFactory codecFactory) {
        this.codecFactory = codecFactory;
    }

    /**
     * 创建自定义解码器
     */
    @NotNull
    public ChannelHandler createCustomDecoder() {
        return new CustomMessageDecoder();
    }

    /**
     * 创建自定义编码器
     */
    @NotNull
    public ChannelHandler createCustomEncoder() {
        return new CustomMessageEncoder();
    }

    /**
     * 自定义消息解码器
     */
    public static class CustomMessageDecoder extends MessageDecoder {

        @Override
        protected boolean isDecodable(@NotNull ByteBuf in) {
            // 检查是否有足够的数据进行解码
            return in.readableBytes() >= 4; // 至少需要4字节的头部
        }

        @Override
        protected Object doDecode(@NotNull ChannelHandlerContext ctx, @NotNull ByteBuf in) {
            try {
                // 读取消息长度
                int messageLength = in.readInt();

                // 检查消息长度
                if (messageLength <= 0 || messageLength > 64 * 1024) {
                    log.warn("Invalid message length: {}", messageLength);
                    return null;
                }

                // 检查是否有完整的消息
                if (in.readableBytes() < messageLength) {
                    return null; // 需要更多数据
                }

                // 读取消息内容
                byte[] messageData = new byte[messageLength];
                in.readBytes(messageData);

                // 解析自定义协议
                String message = new String(messageData, StandardCharsets.UTF_8);
                log.debug("Decoded custom message: {}", message);

                return message;

            } catch (Exception e) {
                log.error("Failed to decode custom message", e);
                return null;
            }
        }
    }

    /**
     * 自定义消息编码器
     */
    public static class CustomMessageEncoder extends MessageEncoder {

        @Override
        protected void doEncode(@NotNull ChannelHandlerContext ctx, @NotNull Object msg, @NotNull ByteBuf out) {
            try {
                String message;
                if (msg instanceof String) {
                    message = (String) msg;
                } else {
                    message = msg.toString();
                }

                byte[] messageData = message.getBytes(StandardCharsets.UTF_8);

                // 写入消息长度
                out.writeInt(messageData.length);

                // 写入消息内容
                out.writeBytes(messageData);

                log.debug("Encoded custom message: {} bytes", messageData.length);

            } catch (Exception e) {
                log.error("Failed to encode custom message", e);
                throw new RuntimeException("Custom message encoding failed", e);
            }
        }
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
        return "CustomProtocol";
    }

    @Override
    @NotNull
    public String getProtocolVersion() {
        return "1.0.0";
    }

    @Override
    public int getDefaultPort() {
        return 9999;
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
        messageCount.incrementAndGet();
        log.debug("Received custom protocol message: {}", message);

        // 记录请求开始处理
        recordRequestStart();
        long startTime = System.currentTimeMillis();

        try {
            // 处理自定义协议消息
            if (message instanceof String) {
                String msg = (String) message;
                log.info("Processing custom message: {}", msg);
            }

            // 记录请求处理完成
            long processingTime = System.currentTimeMillis() - startTime;
            recordRequestComplete(processingTime);

        } catch (Exception e) {
            // 记录请求处理错误
            recordRequestError();
            log.error("Error processing custom protocol message", e);
        }
    }

    @Override
    public void onException(@NotNull ChannelHandlerContext ctx, @NotNull Throwable cause) {
        log.error("Custom protocol exception occurred", cause);
    }

    @Override
    @Nullable
    public MessageHandler getMessageHandler() {
        return new CustomMessageHandler();
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
    public com.dtc.core.extensions.model.ExtensionMetadata getMetadata() {
        return com.dtc.core.extensions.model.ExtensionMetadata.builder()
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
    private class CustomMessageHandler implements MessageHandler {

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
        return activeRequestCount.get() == 0 && activeConnections.isEmpty();
    }

    @Override
    public int getActiveRequestCount() {
        return (int) activeRequestCount.get();
    }

    @Override
    public boolean waitForRequestsToComplete(long timeoutMs) {
        long startTime = System.currentTimeMillis();

        while (activeRequestCount.get() > 0 && (System.currentTimeMillis() - startTime) < timeoutMs) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }

        return activeRequestCount.get() == 0;
    }

    // ========== RequestStatisticsExtension 实现 ==========

    @Override
    public int getPendingRequestCount() {
        return getActiveRequestCount();
    }

    @Override
    public long getTotalProcessedRequests() {
        return totalProcessedRequests.get();
    }

    @Override
    public long getErrorRequestCount() {
        return errorRequestCount.get();
    }

    @Override
    public double getAverageProcessingTime() {
        long total = totalProcessedRequests.get();
        if (total == 0) {
            return 0.0;
        }
        return (double) totalProcessingTime.get() / total;
    }

    @Override
    public void resetStatistics() {
        totalProcessedRequests.set(0);
        errorRequestCount.set(0);
        activeRequestCount.set(0);
        totalProcessingTime.set(0);
        log.info("Custom Protocol extension statistics reset");
    }

    /**
     * 记录请求开始处理
     */
    public void recordRequestStart() {
        activeRequestCount.incrementAndGet();
    }

    /**
     * 记录请求处理完成
     */
    public void recordRequestComplete(long processingTimeMs) {
        activeRequestCount.decrementAndGet();
        totalProcessedRequests.incrementAndGet();
        totalProcessingTime.addAndGet(processingTimeMs);
    }

    /**
     * 记录请求处理错误
     */
    public void recordRequestError() {
        activeRequestCount.decrementAndGet();
        errorRequestCount.incrementAndGet();
    }

    /**
     * 获取活跃连接数量
     */
    public int getActiveConnectionCount() {
        return activeConnections.size();
    }

    /**
     * 获取所有活跃连接
     */
    public ConcurrentHashMap<String, ChannelHandlerContext> getActiveConnections() {
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
}
