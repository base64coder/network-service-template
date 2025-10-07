package com.dtc.core.websocket;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;

/**
 * WebSocket 连接管理器
 * 负责管理 WebSocket 客户端连接的生命周期
 * 
 * @author Network Service Template
 */
@Singleton
public class WebSocketConnectionManager {

    private static final Logger log = LoggerFactory.getLogger(WebSocketConnectionManager.class);

    // 活跃连接管理
    private final ConcurrentHashMap<String, ChannelHandlerContext> activeConnections = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> connectionStates = new ConcurrentHashMap<>();

    public WebSocketConnectionManager() {
        log.info("Creating WebSocket Connection Manager instance");
    }

    /**
     * 添加新连接
     */
    public void addConnection(@NotNull String clientId, @NotNull ChannelHandlerContext ctx) {
        activeConnections.put(clientId, ctx);
        connectionStates.put(clientId, "CONNECTED");
        log.info("Added WebSocket connection for client: {} from {}", clientId, ctx.channel().remoteAddress());
    }

    /**
     * 移除连接
     */
    public void removeConnection(@NotNull String clientId) {
        ChannelHandlerContext ctx = activeConnections.remove(clientId);
        connectionStates.remove(clientId);
        if (ctx != null) {
            log.info("Removed WebSocket connection for client: {}", clientId);
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
     * 获取活跃连接数量
     */
    public int getActiveConnectionCount() {
        return activeConnections.size();
    }

    /**
     * 获取所有活跃连接
     */
    @NotNull
    public Set<String> getActiveClientIds() {
        return activeConnections.keySet();
    }

    /**
     * 设置连接状态
     */
    public void setConnectionState(@NotNull String clientId, @NotNull String state) {
        connectionStates.put(clientId, state);
        log.debug("Set connection state for client: {} to {}", clientId, state);
    }

    /**
     * 获取连接状态
     */
    @Nullable
    public String getConnectionState(@NotNull String clientId) {
        return connectionStates.get(clientId);
    }

    /**
     * 关闭所有连接
     */
    public void closeAllConnections() {
        log.info("Closing all WebSocket connections...");
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
        connectionStates.clear();
        log.info("All WebSocket connections closed");
    }

    /**
     * 优雅关闭所有连接
     */
    public void gracefulCloseAllConnections() {
        log.info("Gracefully closing all WebSocket connections...");
        for (String clientId : activeConnections.keySet()) {
            try {
                ChannelHandlerContext ctx = activeConnections.get(clientId);
                if (ctx != null && ctx.channel().isActive()) {
                    // 发送关闭通知
                    sendCloseFrame(ctx, clientId);
                    ctx.close();
                }
            } catch (Exception e) {
                log.warn("Failed to gracefully close connection for client: {}", clientId, e);
            }
        }
        activeConnections.clear();
        connectionStates.clear();
        log.info("All WebSocket connections gracefully closed");
    }

    /**
     * 发送关闭帧
     */
    private void sendCloseFrame(@NotNull ChannelHandlerContext ctx, @NotNull String clientId) {
        try {
            // 发送 WebSocket 关闭帧
            log.debug("Sending close frame to client: {}", clientId);
            // 这里可以实现具体的关闭帧发送逻辑
        } catch (Exception e) {
            log.warn("Failed to send close frame to client: {}", clientId, e);
        }
    }
}
