package com.dtc.core.network.udp;

import jakarta.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dtc.api.annotations.NotNull;

/**
 * UDP 服务器类
 * 负责管理 UDP 连接和消息处理
 * 
 * @author Network Service Template
 */
@Singleton
public class UdpServer {

    private static final Logger log = LoggerFactory.getLogger(UdpServer.class);

    // 配置信息：UDP服务器启动参数，通过NettyServer统一管理
    private int port = 9997;
    private String host = "0.0.0.0";
    private int maxPacketSize = 65507; // UDP最大数据包大小限制

    public UdpServer() {
        log.info("Creating UDP Server instance");
    }

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
     * 获取最大数据包大小限制
     */
    public int getMaxPacketSize() {
        return maxPacketSize;
    }

    /**
     * 设置最大数据包大小限制
     */
    public void setMaxPacketSize(int maxPacketSize) {
        if (maxPacketSize > 65507) {
            log.warn("UDP max packet size cannot exceed 65507, setting to 65507");
            this.maxPacketSize = 65507;
        } else {
            this.maxPacketSize = maxPacketSize;
        }
    }
}
