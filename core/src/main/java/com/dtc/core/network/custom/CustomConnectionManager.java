package com.dtc.core.network.custom;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Singleton;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;

/**
 * Custom 连接管理器
 * 负责管理自定义协议客户端连接的创建和销毁
 * 
 * @author Network Service Template
 */
@Singleton
public class CustomConnectionManager {

    private static final Logger log = LoggerFactory.getLogger(CustomConnectionManager.class);

    // 活动连接存储
    private final ConcurrentHashMap<String, ChannelHandlerContext> activeConnections = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> connectionProtocols = new ConcurrentHashMap<>();

    public CustomConnectionManager() {
        log.info("Creating Custom Connection Manager instance");
    }

    /**
     * 添加新连接
     */
    public void addConnection(@NotNull String clientId, @NotNull ChannelHandlerContext ctx) {
        activeConnections.put(clientId, ctx);
        connectionProtocols.put(clientId, "CustomProtocol");
        log.info("Added Custom connection for client: {} from {}", clientId, ctx.channel().remoteAddress());
    }

    /**
     * 移除连接
     */
    public void removeConnection(@NotNull String clientId) {
        ChannelHandlerContext ctx = activeConnections.remove(clientId);
        connectionProtocols.remove(clientId);
        if (ctx != null) {
            log.info("Removed Custom connection for client: {}", clientId);
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
     * 获取所有活动连接
     */
    @NotNull
    public Set<String> getActiveClientIds() {
        return activeConnections.keySet();
    }

    /**
     * 设置连接协议
     */
    public void setConnectionProtocol(@NotNull String clientId, @NotNull String protocol) {
        connectionProtocols.put(clientId, protocol);
        log.debug("Set protocol for client: {} to {}", clientId, protocol);
    }

    /**
     * 获取连接协议
     */
    @Nullable
    public String getConnectionProtocol(@NotNull String clientId) {
        return connectionProtocols.get(clientId);
    }

    /**
     * 关闭所有连接
     */
    public void closeAllConnections() {
        log.info("Closing all Custom connections...");
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
        connectionProtocols.clear();
        log.info("All Custom connections closed");
    }

    /**
     * 优雅关闭所有连接
     */
    public void gracefulCloseAllConnections() {
        log.info("Gracefully closing all Custom connections...");
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
        connectionProtocols.clear();
        log.info("All Custom connections gracefully closed");
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
}
