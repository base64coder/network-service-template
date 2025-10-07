package com.dtc.core.messaging;

import com.dtc.api.annotations.NotNull;
import com.dtc.core.queue.DisruptorQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * 网络消息队列
 * 扩展DisruptorQueue，专门处理NetworkMessageEvent
 * 
 * @author Network Service Template
 */
@Singleton
public class NetworkMessageQueue extends DisruptorQueue<NetworkMessageEvent> {

    private static final Logger log = LoggerFactory.getLogger(NetworkMessageQueue.class);

    private final NetworkMessageConsumer messageConsumer;

    @Inject
    public NetworkMessageQueue(@NotNull NetworkMessageConsumer messageConsumer) {
        super(1024 * 1024); // 1M buffer
        this.messageConsumer = messageConsumer;
        initializeQueue();
    }

    /**
     * 初始化队列
     */
    private void initializeQueue() {
        try {
            // 添加消息消费者
            addConsumer(messageConsumer);
            log.info("✅ Network message queue initialized successfully");
        } catch (Exception e) {
            log.error("❌ Failed to initialize network message queue", e);
            throw new RuntimeException("Failed to initialize network message queue", e);
        }
    }

    /**
     * 启动消息队列
     */
    public void start() {
        try {
            super.start();
            log.info("🚀 Network message queue started successfully");
        } catch (Exception e) {
            log.error("❌ Failed to start network message queue", e);
            throw new RuntimeException("Failed to start network message queue", e);
        }
    }

    /**
     * 停止消息队列
     */
    public void stop() {
        try {
            super.shutdown();
            log.info("🛑 Network message queue stopped successfully");
        } catch (Exception e) {
            log.error("❌ Failed to stop network message queue", e);
        }
    }

    /**
     * 发布网络消息事件
     */
    public boolean publish(@NotNull NetworkMessageEvent event) {
        try {
            boolean success = super.publish(event);
            if (success) {
                log.debug("📤 Published network message: {}", event.getEventId());
            } else {
                log.warn("⚠️ Failed to publish network message: {}", event.getEventId());
            }
            return success;
        } catch (Exception e) {
            log.error("❌ Failed to publish network message: {}", event.getEventId(), e);
            return false;
        }
    }

    /**
     * 检查队列是否已启动
     */
    public boolean isStarted() {
        return getStatus().isStarted();
    }

    /**
     * 获取支持的协议类型
     */
    @NotNull
    public java.util.Set<String> getSupportedProtocols() {
        return messageConsumer.getSupportedProtocols();
    }

    /**
     * 检查是否支持指定协议
     */
    public boolean supportsProtocol(@NotNull String protocolType) {
        return messageConsumer.supportsProtocol(protocolType);
    }
}
