package com.dtc.core.custom;

import com.dtc.api.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;

/**
 * Custom 服务器实现
 * 负责管理自定义协议连接和消息处理
 * 
 * @author Network Service Template
 */
@Singleton
public class CustomServer {

    private static final Logger log = LoggerFactory.getLogger(CustomServer.class);

    // 注意：Custom服务器启动由NettyServer统一管理
    // private volatile boolean started = false; // 已废弃
    private int port = 9999;
    private String host = "0.0.0.0";
    private String protocolName = "CustomProtocol";

    public CustomServer() {
        log.info("Creating Custom Server instance");
    }

    // 注意：Custom服务器启动/停止由NettyServer统一管理
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
     * 获取协议名称
     */
    @NotNull
    public String getProtocolName() {
        return protocolName;
    }

    /**
     * 设置协议名称
     */
    public void setProtocolName(@NotNull String protocolName) {
        this.protocolName = protocolName;
    }
}
