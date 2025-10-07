package com.dtc.core.websocket;

import com.dtc.api.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;

/**
 * WebSocket 服务器实现
 * 负责管理 WebSocket 协议连接和消息处理
 * 
 * @author Network Service Template
 */
@Singleton
public class WebSocketServer {

    private static final Logger log = LoggerFactory.getLogger(WebSocketServer.class);

    // 注意：WebSocket服务器启动由NettyServer统一管理
    // private volatile boolean started = false; // 已废弃
    private int port = 8081;
    private String host = "0.0.0.0";
    private String path = "/websocket";

    public WebSocketServer() {
        log.info("Creating WebSocket Server instance");
    }

    // 注意：WebSocket服务器启动/停止由NettyServer统一管理
    // start(), stop(), isStarted() 方法已移除

    /**
     * 获取服务器端口
     */
    public int getPort() {
        return port;
    }

    /**
     * 设置服务器端口
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * 获取服务器主机
     */
    @NotNull
    public String getHost() {
        return host;
    }

    /**
     * 设置服务器主机
     */
    public void setHost(@NotNull String host) {
        this.host = host;
    }

    /**
     * 获取 WebSocket 路径
     */
    @NotNull
    public String getPath() {
        return path;
    }

    /**
     * 设置 WebSocket 路径
     */
    public void setPath(@NotNull String path) {
        this.path = path;
    }
}
