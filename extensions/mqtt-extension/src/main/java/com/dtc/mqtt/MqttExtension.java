package com.dtc.mqtt;

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
import com.dtc.core.mqtt.MqttServer;
import com.dtc.core.mqtt.MqttMessageHandler;
import com.dtc.core.mqtt.MqttConnectionManager;
import com.dtc.core.statistics.StatisticsAware;
import com.dtc.core.messaging.NetworkMessageEvent;
import com.dtc.core.messaging.NetworkMessageQueue;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * MQTT协议扩展示例
 * 实现MQTT协议的基本功能
 * 
 * @author Network Service Template
 */
@Singleton
public class MqttExtension extends StatisticsAware implements ExtensionMain, ProtocolExtension, NetworkExtension,
        GracefulShutdownExtension {

    private static final Logger log = LoggerFactory.getLogger(MqttExtension.class);

    private final MqttServer mqttServer;
    private final MqttMessageHandler messageHandler;
    private final MqttConnectionManager connectionManager;
    private final NetworkMessageQueue messageQueue;

    private volatile boolean started = false;
    private volatile boolean enabled = true;
    private volatile boolean shutdownPrepared = false;

    // 请求统计

    @Inject
    public MqttExtension(@NotNull MqttServer mqttServer,
            @NotNull MqttMessageHandler messageHandler,
            @NotNull MqttConnectionManager connectionManager,
            @NotNull NetworkMessageQueue messageQueue,
            @NotNull com.dtc.core.statistics.StatisticsCollector statisticsCollector) {
        super(statisticsCollector);
        this.mqttServer = mqttServer;
        this.messageHandler = messageHandler;
        this.connectionManager = connectionManager;
        this.messageQueue = messageQueue;
    }

    @Override
    public void extensionStart(@NotNull ExtensionStartInput input, @NotNull ExtensionStartOutput output) {
        log.info("Starting MQTT Extension v{}", input.getExtensionVersion());

        try {
            // 初始化MQTT协议处理器
            initializeMqttHandler();

            started = true;
            log.info("MQTT Extension started successfully");
        } catch (Exception e) {
            log.error("Failed to start MQTT Extension", e);
            output.preventStartup("Failed to initialize MQTT handler: " + e.getMessage());
        }
    }

    @Override
    public void extensionStop(@NotNull ExtensionStopInput input, @NotNull ExtensionStopOutput output) {
        log.info("Stopping MQTT Extension v{}", input.getExtensionVersion());

        try {
            // 清理MQTT协议资源
            cleanupMqttHandler();

            started = false;
            log.info("MQTT Extension stopped successfully");
        } catch (Exception e) {
            log.error("Failed to stop MQTT Extension", e);
            output.preventStop("Failed to cleanup MQTT handler: " + e.getMessage());
        }
    }

    @Override
    @NotNull
    public String getProtocolName() {
        return ServiceConfig.MQTT.getServiceName();
    }

    @Override
    @NotNull
    public String getProtocolVersion() {
        return "3.1.1";
    }

    @Override
    public int getDefaultPort() {
        return ServiceConfig.MQTT.getDefaultPort();
    }

    @Override
    public void onConnect(@NotNull ChannelHandlerContext ctx, @NotNull String clientId) {
        log.info("MQTT client connected: {} from {}", clientId, ctx.channel().remoteAddress());

        // 处理MQTT连接
        // 这里可以实现MQTT CONNECT消息的处理逻辑
    }

    @Override
    public void onDisconnect(@NotNull ChannelHandlerContext ctx, @NotNull String clientId) {
        log.info("MQTT client disconnected: {}", clientId);

        // 处理MQTT断开连接
        // 这里可以实现MQTT DISCONNECT消息的处理逻辑
    }

    @Override
    public void onMessage(@NotNull ChannelHandlerContext ctx, @NotNull Object message) {
        log.debug("📨 MQTT message received from client: {}", ctx.channel().remoteAddress());

        try {
            // 处理 MQTT 消息 - 使用 Disruptor 异步处理
            if (message != null) {
                // 创建网络消息事件
                NetworkMessageEvent event = createNetworkMessageEvent(ctx, message);

                // 发布到 Disruptor 队列进行异步处理
                boolean published = messageQueue.publish(event);
                if (published) {
                    log.debug("✅ MQTT message published to Disruptor queue: {}", event.getEventId());
                } else {
                    log.error("❌ Failed to publish MQTT message to Disruptor queue");
                    // 如果发布失败，发送错误响应
                    sendErrorResponse(ctx, "Service temporarily unavailable");
                }
            } else {
                log.warn("⚠️ Received null message in MQTT extension");
            }
        } catch (Exception e) {
            log.error("❌ Error handling MQTT message from client: {}", ctx.channel().remoteAddress(), e);
            sendErrorResponse(ctx, "Internal server error");
        }
    }

    @Override
    public void onException(@NotNull ChannelHandlerContext ctx, @NotNull Throwable cause) {
        log.error("MQTT protocol error from client: {}", ctx.channel().remoteAddress(), cause);

        // 处理MQTT协议异常
        // 这里可以实现异常处理和连接关闭逻辑
    }

    @Override
    @Nullable
    public MessageHandler getMessageHandler() {
        return new MqttMessageHandler();
    }

    /**
     * 初始化MQTT处理器
     */
    private void initializeMqttHandler() {
        log.info("Initializing MQTT protocol handler...");
        // 初始化MQTT协议相关的组件
    }

    /**
     * 清理MQTT处理器
     */
    private void cleanupMqttHandler() {
        log.info("Cleaning up MQTT protocol handler...");
        // 清理MQTT协议相关的资源
    }

    /**
     * MQTT消息处理器
     */
    private static class MqttMessageHandler implements MessageHandler {

        @Override
        @Nullable
        public Object handleMessage(@NotNull ChannelHandlerContext ctx, @NotNull Object message) {
            log.debug("Handling MQTT message: {}", message.getClass().getSimpleName());

            // 处理接收到的MQTT消息
            // 这里可以实现具体的MQTT消息处理逻辑

            return null; // 继续处理链
        }

        @Override
        @Nullable
        public Object handleOutboundMessage(@NotNull ChannelHandlerContext ctx, @NotNull Object message) {
            log.debug("Handling outbound MQTT message: {}", message.getClass().getSimpleName());

            // 处理发送的MQTT消息
            // 这里可以实现MQTT消息的预处理逻辑

            return message; // 发送消息
        }

        @Override
        public int getPriority() {
            return 50; // MQTT消息处理器优先级
        }

        @Override
        public boolean supports(@NotNull Class<?> messageType) {
            // 检查是否支持该消息类型
            return messageType.getName().contains("Mqtt") ||
                    messageType.getName().contains("MQTT");
        }
    }

    // NetworkExtension 实现
    @Override
    @NotNull
    public String getId() {
        return "mqtt-extension";
    }

    @Override
    @NotNull
    public String getName() {
        return "MQTT Protocol Extension";
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
        return 50;
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
            log.info("Starting MQTT extension...");
            started = true;
            log.info("MQTT extension started successfully");
        }
    }

    @Override
    public void stop() throws Exception {
        if (started) {
            log.info("Stopping MQTT extension...");
            started = false;
            log.info("MQTT extension stopped successfully");
        }
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        log.info("MQTT extension {} {}", getId(), enabled ? "enabled" : "disabled");
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
        log.info("Cleaning up MQTT extension: {} (disable: {})", getId(), disable);
        if (disable) {
            setEnabled(false);
        }
    }

    // ========== GracefulShutdownExtension 实现 ==========

    @Override
    public void prepareForShutdown() throws Exception {
        log.info("Preparing MQTT extension for shutdown...");
        shutdownPrepared = true;

        // 停止接收新的 MQTT 连接
        // 这里可以关闭端口、移除路由等
        log.info("MQTT extension prepared for shutdown");
    }

    @Override
    public boolean canShutdownSafely() {
        return getActiveRequestCount() == 0;
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
        if (message instanceof io.netty.buffer.ByteBuf) {
            messageSize = ((io.netty.buffer.ByteBuf) message).readableBytes();
        } else if (message instanceof byte[]) {
            messageSize = ((byte[]) message).length;
        }

        return NetworkMessageEvent.builder()
                .protocolType("mqtt")
                .clientId(clientId)
                .message(message)
                .channelContext(ctx)
                .sourceAddress(ctx.channel().remoteAddress().toString())
                .messageSize(messageSize)
                .messageType("MQTT_MESSAGE")
                .isRequest(true)
                .priority(3) // MQTT消息优先级
                .build();
    }

    /**
     * 发送错误响应
     */
    private void sendErrorResponse(@NotNull ChannelHandlerContext ctx, @NotNull String errorMessage) {
        try {
            // MQTT错误响应处理
            log.error("MQTT error response: {}", errorMessage);
            // 这里可以实现具体的MQTT错误响应逻辑
        } catch (Exception e) {
            log.error("❌ Failed to send error response to MQTT client: {}", ctx.channel().remoteAddress(), e);
        }
    }

    // ========== 统计功能已移至StatisticsAware基类 ==========
}
