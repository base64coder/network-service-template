package com.dtc.core.network.custom;

import com.dtc.api.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;

/**
 * 自定义服务器
 * 用于实现自定义协议的网络服务器，可以基于 NettyServer 进行扩展
 * 
 * @author Network Service Template
 */
@Singleton
public class CustomServer {

    private static final Logger log = LoggerFactory.getLogger(CustomServer.class);

    // 注意：自定义服务器可以基于 NettyServer 进行扩展
    // private volatile boolean started = false; // 待实现
    private int port = 9999;
    private String host = "0.0.0.0";
    private String protocolName = "CustomProtocol";

    public CustomServer() {
        log.info("Creating Custom Server instance");
    }

    // 注意：自定义服务器可以基于 NettyServer 进行扩展
    // start(), stop(), isStarted() 等方法待实现
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
     * 获取服务器主机地址
     */
    @NotNull
    public String getHost() {
        return host;
    }

    /**
     * 设置服务器主机地址
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
