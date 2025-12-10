package com.dtc.core.network.mqtt;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Singleton;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;

/**
 * MQTT 连接管理器
 * 负责管理 MQTT 客户端连接和连接状态管理
 * 
 * @author Network Service Template
 */
@Singleton
public class MqttConnectionManager {

    private static final Logger log = LoggerFactory.getLogger(MqttConnectionManager.class);

    // 活动连接管理
    private final ConcurrentHashMap<String, ChannelHandlerContext> activeConnections = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> clientSessions = new ConcurrentHashMap<>();

    public MqttConnectionManager() {
        log.info("Creating MQTT Connection Manager instance");
    }

    /**
     * 添加新连接
     */
    public void addConnection(@NotNull String clientId, @NotNull ChannelHandlerContext ctx) {
        activeConnections.put(clientId, ctx);
        log.info("Added MQTT connection for client: {} from {}", clientId, ctx.channel().remoteAddress());
    }

    /**
     * 移除连接
     */
    public void removeConnection(@NotNull String clientId) {
        ChannelHandlerContext ctx = activeConnections.remove(clientId);
        clientSessions.remove(clientId);
        if (ctx != null) {
            log.info("Removed MQTT connection for client: {}", clientId);
        }
    }

    /**
     * 获取客户端连接
     */
    @Nullable
    public ChannelHandlerContext getConnection(@NotNull String clientId) {
        return activeConnections.get(clientId);
    }

    /**
     * 检查客户端是否已连接
     */
    public boolean isConnected(@NotNull String clientId) {
        return activeConnections.containsKey(clientId);
    }

    /**
     * 获取活动连接数量
     */
    public int getActiveConnectionCount() {
        return activeConnections.size();
    }

    /**
     * 获取所有活动客户端ID
     */
    @NotNull
    public Set<String> getActiveClientIds() {
        return activeConnections.keySet();
    }

    /**
     * 设置客户端会话
     */
    public void setClientSession(@NotNull String clientId, @NotNull String sessionData) {
        clientSessions.put(clientId, sessionData);
        log.debug("Set session for client: {}", clientId);
    }

    /**
     * 获取客户端会话
     */
    @Nullable
    public String getClientSession(@NotNull String clientId) {
        return clientSessions.get(clientId);
    }

    /**
     * 关闭所有连接
     */
    public void closeAllConnections() {
        log.info("Closing all MQTT connections...");
        for (String clientId : activeConnections.keySet()) {
            try {
                ChannelHandlerContext ctx = activeConnections.get(clientId);
                if (ctx != null && ctx.channel().isActive()) {
                    ctx.close();
                }
            } catch (Exception e) {
                log.warn("Failed to close connection for client: {}", clientId, e);
            }
        }
        activeConnections.clear();
        clientSessions.clear();
        log.info("All MQTT connections closed");
    }

    /**
     * 优雅关闭所有连接
     */
    public void gracefulCloseAllConnections() {
        log.info("Gracefully closing all MQTT connections...");
        for (String clientId : activeConnections.keySet()) {
            try {
                ChannelHandlerContext ctx = activeConnections.get(clientId);
                if (ctx != null && ctx.channel().isActive()) {
                    // 发送断开连接消息
                    sendDisconnectMessage(ctx, clientId);
                    ctx.close();
                }
            } catch (Exception e) {
                log.warn("Failed to gracefully close connection for client: {}", clientId, e);
            }
        }
        activeConnections.clear();
        clientSessions.clear();
        log.info("All MQTT connections gracefully closed");
    }

    /**
     * 发送断开连接消息
     */
    private void sendDisconnectMessage(@NotNull ChannelHandlerContext ctx, @NotNull String clientId) {
        try {
            // 发送 MQTT DISCONNECT 消息
            log.debug("Sending disconnect message to client: {}", clientId);
            // 可以通过路由管理器实现断开连接消息的发送逻辑
        } catch (Exception e) {
            log.warn("Failed to send disconnect message to client: {}", clientId, e);
        }
    }
}
