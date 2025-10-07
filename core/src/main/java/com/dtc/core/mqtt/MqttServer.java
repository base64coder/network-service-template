package com.dtc.core.mqtt;

import com.dtc.api.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;

/**
 * MQTT 服务器实现
 * 负责管理 MQTT 协议连接和消息处理
 * 
 * @author Network Service Template
 */
@Singleton
public class MqttServer {

    private static final Logger log = LoggerFactory.getLogger(MqttServer.class);

    // 注意：MQTT服务器启动由NettyServer统一管理
    // private volatile boolean started = false; // 已废弃
    private int port = 1883;
    private String host = "0.0.0.0";

    public MqttServer() {
        log.info("Creating MQTT Server instance");
    }

    // 注意：MQTT服务器启动/停止由NettyServer统一管理
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
}
