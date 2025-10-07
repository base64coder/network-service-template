package com.dtc.core.bootstrap.ioc;

import com.dtc.core.custom.*;
import com.dtc.core.mqtt.MqttConnectionManager;
import com.dtc.core.mqtt.MqttMessageHandler;
import com.dtc.core.mqtt.MqttServer;
import com.dtc.core.statistics.StatisticsCollector;
import com.dtc.core.messaging.NetworkMessageQueue;
import com.dtc.core.tcp.*;
import com.dtc.core.websocket.WebSocketConnectionManager;
import com.dtc.core.websocket.WebSocketMessageHandler;
import com.dtc.core.websocket.WebSocketServer;
import com.google.inject.AbstractModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 扩展依赖模块
 * 注册所有扩展所需的依赖类到 Guice 容器中
 * 
 * @author Network Service Template
 */
public class ExtensionDependencyModule extends AbstractModule {

    private static final Logger log = LoggerFactory.getLogger(ExtensionDependencyModule.class);

    @Override
    protected void configure() {
        log.info("Configuring extension dependencies...");

        // 注册统计收集器
        bind(StatisticsCollector.class).asEagerSingleton();

        // 注册网络消息队列
        bind(NetworkMessageQueue.class).asEagerSingleton();

        // 注册 MQTT 扩展依赖
        bind(MqttServer.class).asEagerSingleton();
        bind(MqttMessageHandler.class).asEagerSingleton();
        bind(MqttConnectionManager.class).asEagerSingleton();

        // 注册 TCP 扩展依赖
        bind(TcpServer.class).asEagerSingleton();
        bind(TcpMessageHandler.class).asEagerSingleton();
        bind(TcpConnectionManager.class).asEagerSingleton();
        bind(TcpProtocolHandler.class).to(GrpcTcpProtocolHandler.class).asEagerSingleton();

        // 注册 WebSocket 扩展依赖
        bind(WebSocketServer.class).asEagerSingleton();
        bind(WebSocketMessageHandler.class).asEagerSingleton();
        bind(WebSocketConnectionManager.class).asEagerSingleton();

        // 注册 Custom 扩展依赖
        bind(CustomServer.class).asEagerSingleton();
        bind(CustomMessageHandler.class).asEagerSingleton();
        bind(CustomConnectionManager.class).asEagerSingleton();
        bind(CustomCodecFactory.class).to(DefaultCustomCodec.class).asEagerSingleton();

        log.info("Extension dependencies configured successfully");
    }
}
