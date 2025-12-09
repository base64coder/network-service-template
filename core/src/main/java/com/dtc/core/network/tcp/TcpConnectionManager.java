package com.dtc.core.network.tcp;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;

/**
 * TCP 连接管理器
 * 负责管理 TCP 客户端连接和连接状态管理
 * 
 * @author Network Service Template
 */
@Singleton
public class TcpConnectionManager {

    private static final Logger log = LoggerFactory.getLogger(TcpConnectionManager.class);

    // 活动连接管理
    private final ConcurrentHashMap<String, ChannelHandlerContext> activeConnections = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> connectionTimestamps = new ConcurrentHashMap<>();

    public TcpConnectionManager() {
        log.info("Creating TCP Connection Manager instance");
    }

    /**
     * 添加新连接
     */
    public void addConnection(@NotNull String clientId, @NotNull ChannelHandlerContext ctx) {
        activeConnections.put(clientId, ctx);
        connectionTimestamps.put(clientId, System.currentTimeMillis());
        log.info("Added TCP connection for client: {} from {}", clientId, ctx.channel().remoteAddress());
    }

    /**
     * 移除连接
     */
    public void removeConnection(@NotNull String clientId) {
        ChannelHandlerContext ctx = activeConnections.remove(clientId);
        connectionTimestamps.remove(clientId);
        if (ctx != null) {
            log.info("Removed TCP connection for client: {}", clientId);
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
     * 获取连接时间戳
     */
    @Nullable
    public Long getConnectionTimestamp(@NotNull String clientId) {
        return connectionTimestamps.get(clientId);
    }

    /**
     * 关闭所有连接
     */
    public void closeAllConnections() {
        log.info("Closing all TCP connections...");
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
        connectionTimestamps.clear();
        log.info("All TCP connections closed");
    }

    /**
     * 优雅关闭所有连接
     */
    public void gracefulCloseAllConnections() {
        log.info("Gracefully closing all TCP connections...");
        for (String clientId : activeConnections.keySet()) {
            try {
                ChannelHandlerContext ctx = activeConnections.get(clientId);
                if (ctx != null && ctx.channel().isActive()) {
                    // 发送关闭通知
                    sendShutdownNotification(ctx, clientId);
                    ctx.close();
                }
            } catch (Exception e) {
                log.warn("Failed to gracefully close connection for client: {}", clientId, e);
            }
        }
        activeConnections.clear();
        connectionTimestamps.clear();
        log.info("All TCP connections gracefully closed");
    }

    /**
     * 发送关闭通知
     */
    private void sendShutdownNotification(@NotNull ChannelHandlerContext ctx, @NotNull String clientId) {
        try {
            String shutdownMsg = "Server is shutting down. Connection will be closed.";
            ctx.writeAndFlush(shutdownMsg);
            log.debug("Sent shutdown notification to client: {}", clientId);
        } catch (Exception e) {
            log.warn("Failed to send shutdown notification to client: {}", clientId, e);
        }
    }
}
