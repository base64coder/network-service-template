package com.dtc.udp;

import com.dtc.api.ExtensionMain;
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
import com.dtc.core.network.udp.UdpMessageHandler;
import com.dtc.core.network.udp.UdpProtocolHandler;
import com.dtc.core.statistics.StatisticsAware;
import com.dtc.core.messaging.NetworkMessageEvent;
import com.dtc.core.messaging.NetworkMessageQueue;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;

/**
 * UDP 协议扩展
 * 实现 UDP 协议扩展的功能和逻辑，适用于实时通信、IoT 和游戏服务器等场景
 * 
 * @author Network Service Template
 */
@Singleton
public class UdpExtension extends StatisticsAware implements ExtensionMain, ProtocolExtension, NetworkExtension,
        GracefulShutdownExtension {

    private static final Logger log = LoggerFactory.getLogger(UdpExtension.class);

    private final UdpMessageHandler messageHandler;
    private final UdpProtocolHandler protocolHandler;
    private final NetworkMessageQueue messageQueue;

    private volatile boolean started = false;
    private volatile boolean enabled = true;

    // 活跃客户端映射（UDP 是无连接协议，这里用于跟踪活跃客户端）
    private final ConcurrentHashMap<String, InetSocketAddress> activeClients = new ConcurrentHashMap<>();

    // NetworkExtension 需要的字段
    private static final String ID = "udp-extension";
    private static final String NAME = "UDP Protocol Extension";
    private static final String VERSION = "1.0.0";
    private static final String AUTHOR = "Network Service Template";
    private static final int PRIORITY = 65;
    private static final int START_PRIORITY = 700;
    private static final String UNKNOWN = "unknown";
    private final ExtensionMetadata metadata;
    private final Path extensionFolderPath;

    @Inject
    public UdpExtension(@NotNull UdpMessageHandler messageHandler,
            @NotNull UdpProtocolHandler protocolHandler,
            @NotNull NetworkMessageQueue messageQueue,
            @NotNull com.dtc.core.statistics.StatisticsCollector statisticsCollector) {
        super(statisticsCollector);
        this.messageHandler = messageHandler;
        this.protocolHandler = protocolHandler;
        this.messageQueue = messageQueue;

        // 初始化 NetworkExtension 字段
        this.metadata = ExtensionMetadata.builder()
                .id(ID)
                .name(NAME)
                .version(VERSION)
                .author(AUTHOR)
                .priority(PRIORITY)
                .startPriority(START_PRIORITY)
                .description("UDP protocol extension for real-time communication, IoT, and game servers")
                .mainClass("com.dtc.udp.UdpExtension")
                .build();
        this.extensionFolderPath = Paths.get("extensions/udp-extension");
    }

    @Override
    public void extensionStart(@NotNull ExtensionStartInput input, @NotNull ExtensionStartOutput output) {
        log.info("Starting UDP Extension v{}", input.getExtensionVersion());

        try {
            // 初始化 UDP 协议处理器
            initializeUdpHandler();

            started = true;
            log.info("UDP Extension started successfully on port {}", ServiceConfig.UDP.getDefaultPort());
        } catch (Exception e) {
            log.error("Failed to start UDP Extension", e);
            output.preventStartup("Failed to initialize UDP handler: " + e.getMessage());
        }
    }

    @Override
    public void extensionStop(@NotNull ExtensionStopInput input, @NotNull ExtensionStopOutput output) {
        log.info("Stopping UDP Extension v{}", input.getExtensionVersion());

        try {
            // 清理 UDP 协议扩展资源
            cleanupUdpHandler();

            started = false;
            log.info("UDP Extension stopped successfully");
        } catch (Exception e) {
            log.error("Failed to stop UDP Extension", e);
            output.preventStop("Failed to cleanup UDP handler: " + e.getMessage());
        }
    }

    @Override
    @NotNull
    public String getProtocolName() {
        return ServiceConfig.UDP.getServiceName();
    }

    @Override
    @NotNull
    public String getProtocolVersion() {
        return "RFC 768";
    }

    @Override
    public int getDefaultPort() {
        return ServiceConfig.UDP.getDefaultPort();
    }

    @Override
    public void onConnect(@NotNull ChannelHandlerContext ctx, @NotNull String clientId) {
        // UDP 是无连接协议，这里用于跟踪首次接触的客户端
        log.debug("UDP client first contact: {} from {}", clientId, 
            ctx.channel() != null ? ctx.channel().remoteAddress() : UNKNOWN);
        
        // 记录客户端地址（如果可以从 context 获取）
        if (ctx.channel() != null && ctx.channel().remoteAddress() instanceof InetSocketAddress address) {
            activeClients.put(clientId, address);
            // UDP 是无连接协议，onConnect 可能不会被调用，实际客户端地址在 onMessage 中通过 DatagramPacket 获取
        }
    }

    @Override
    public void onDisconnect(@NotNull ChannelHandlerContext ctx, @NotNull String clientId) {
        // UDP 是无连接协议，这里用于清理客户端地址
        log.debug("UDP client inactive: {}", clientId);
        
        // 移除客户端地址
        activeClients.remove(clientId);
        protocolHandler.handleDisconnect(clientId);
    }

    @Override
    public void onMessage(@NotNull ChannelHandlerContext ctx, @NotNull Object message) {
        log.debug("UDP message received from: {}", 
            message instanceof DatagramPacket packet ? 
                packet.sender() : UNKNOWN);

        try {
            // 处理 UDP 消息 - 通过 Disruptor 异步处理
            if (message instanceof DatagramPacket packet) {
                // 记录客户端地址
                InetSocketAddress sender = packet.sender();
                String clientId = generateClientId(sender);
                activeClients.put(clientId, sender);
                
                // 通过协议处理器处理消息
                protocolHandler.handleMessage(ctx, packet);
                
                // 创建网络消息事件
                NetworkMessageEvent event = createNetworkMessageEvent(ctx, packet);

                // 发布到 Disruptor 队列进行异步处理
                boolean published = messageQueue.publish(event);
                if (published) {
                    log.debug("UDP message published to Disruptor queue: {}", event.getEventId());
                } else {
                    log.error("Failed to publish UDP message to Disruptor queue");
                    // 如果发布失败，发送错误响应
                    sendErrorResponse(ctx, packet.sender(), "Service temporarily unavailable");
                }
            } else {
                log.warn("Received unexpected message type in UDP extension: {}",
                    message.getClass().getSimpleName());
            }
        } catch (Exception e) {
            log.error("Error handling UDP message", e);
            if (message instanceof DatagramPacket packet) {
                sendErrorResponse(ctx, packet.sender(), "Internal server error");
            }
        }
    }
    
    /**
     * 生成客户端ID
     */
    @NotNull
    private String generateClientId(@NotNull InetSocketAddress address) {
        return String.format("udp-%s-%d", address.getAddress().getHostAddress(), address.getPort());
    }

    @Override
    public void onException(@NotNull ChannelHandlerContext ctx, @NotNull Throwable cause) {
        log.error("UDP protocol error from: {}", 
            ctx.channel() != null ? ctx.channel().remoteAddress() : UNKNOWN, cause);
        
        // UDP 是无连接协议，异常处理可能不需要关闭连接，这里可以根据需要添加错误处理逻辑
        // 例如记录错误日志或发送错误响应等
    }

    @Override
    @Nullable
    public com.dtc.api.MessageHandler getMessageHandler() {
        // UDP 消息处理器通过 onMessage 方法处理，这里可以返回 null
        return null;
    }

    // ========== NetworkExtension 接口实现 ==========

    @Override
    @NotNull
    public String getId() {
        return ID;
    }

    @Override
    @NotNull
    public String getName() {
        return NAME;
    }

    @Override
    @NotNull
    public String getVersion() {
        return VERSION;
    }

    @Override
    @Nullable
    public String getAuthor() {
        return AUTHOR;
    }

    @Override
    public int getPriority() {
        return PRIORITY;
    }

    @Override
    public int getStartPriority() {
        return START_PRIORITY;
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
        return UdpExtension.class.getClassLoader();
    }

    @Override
    public void start() throws Exception {
        if (!started) {
            started = true;
            log.info("Starting UDP extension...");

            try {
                // 初始化 UDP 协议处理器
                initializeUdpHandler();

                log.info("UDP extension started successfully on port {}", ServiceConfig.UDP.getDefaultPort());
            } catch (Exception e) {
                log.error("Failed to start UDP extension", e);
                started = false;
                throw e;
            }
        }
    }

    @Override
    public void stop() throws Exception {
        if (started) {
            started = false;
            log.info("Stopping UDP extension...");

            try {
                // 清理 UDP 协议扩展资源
                cleanupUdpHandler();

                log.info("UDP extension stopped successfully");
            } catch (Exception e) {
                log.error("Error stopping UDP extension", e);
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
        log.info("Cleaning up UDP extension (disable: {})", disable);

        try {
            if (started) {
                stop();
            }

            if (disable) {
                setEnabled(false);
            }

            // 清理资源
            activeClients.clear();

            log.info("UDP extension cleanup completed");
        } catch (Exception e) {
            log.error("Error during UDP extension cleanup", e);
        }
    }

    // ========== GracefulShutdownExtension 接口实现 ==========

    @Override
    public void prepareForShutdown() throws Exception {
        log.info("Preparing UDP extension for shutdown...");
        
        // 停止接收新的 UDP 消息
        // UDP 是无连接协议，这里可以设置标志来拒绝新消息
        log.info("UDP extension prepared for shutdown");
    }

    @Override
    public boolean canShutdownSafely() {
        // UDP 是无连接协议，这里检查是否有活跃的客户端
        return activeClients.isEmpty();
    }

    @Override
    public long getActiveRequestCount() {
        // UDP 是无连接协议，这里返回活跃客户端数量作为参考
        return activeClients.size();
    }

    @Override
    public boolean waitForRequestsToComplete(long timeoutMs) {
        long startTime = System.currentTimeMillis();
        
        // UDP 是无连接协议，等待活跃客户端清理
        while (!activeClients.isEmpty() && (System.currentTimeMillis() - startTime) < timeoutMs) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Interrupted while waiting for UDP requests to complete");
                return false;
            }
        }
        
        return activeClients.isEmpty();
    }

    // ========== 辅助方法 ==========

    /**
     * 初始化 UDP 协议处理器
     */
    private void initializeUdpHandler() {
        log.debug("Initializing UDP protocol handler");
        // UDP 是无连接协议，这里可以设置缓冲区大小等
        // 这里可以根据需要添加初始化逻辑
    }

    /**
     * 清理 UDP 协议处理器
     */
    private void cleanupUdpHandler() {
        log.debug("Cleaning up UDP protocol handler");
        activeClients.clear();
    }

    /**
     * 创建网络消息事件
     */
    @NotNull
    private NetworkMessageEvent createNetworkMessageEvent(@NotNull ChannelHandlerContext ctx, 
                                                          @NotNull DatagramPacket packet) {
        String eventId = java.util.UUID.randomUUID().toString();
        InetSocketAddress sender = packet.sender();
        String clientId = sender != null ? generateClientId(sender) : null;
        
        return NetworkMessageEvent.builder()
                .eventId(eventId)
                .protocolType("udp")
                .clientId(clientId)
                .message(packet)
                .channelContext(ctx)
                .sourceAddress(sender != null ? sender.toString() : UNKNOWN)
                .messageSize(packet.content().readableBytes())
                .messageType("DatagramPacket")
                .isRequest(true)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 发送错误响应
     */
    private void sendErrorResponse(@NotNull ChannelHandlerContext ctx, 
                                  @NotNull java.net.InetSocketAddress recipient, 
                                  @NotNull String errorMessage) {
        try {
            messageHandler.sendResponse(ctx, recipient, "ERROR: " + errorMessage);
        } catch (Exception e) {
            log.error("Failed to send error response", e);
        }
    }
}

