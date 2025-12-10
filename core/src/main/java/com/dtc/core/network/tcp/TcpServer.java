package com.dtc.core.network.tcp;

import com.dtc.api.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;

/**
 * TCP 服务器
 * 用于实现 TCP 协议的网络服务器，可以基于 NettyServer 进行扩展
 * 
 * @author Network Service Template
 */
@Singleton
public class TcpServer {

    private static final Logger log = LoggerFactory.getLogger(TcpServer.class);

    // 注意：TCP服务器可以基于 NettyServer 进行扩展
    // private volatile boolean started = false; // 待实现
    private int port = 9999;
    private String host = "0.0.0.0";

    public TcpServer() {
        log.info("Creating TCP Server instance");
    }

    // 注意：TCP服务器可以基于 NettyServer 进行扩展
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
}
