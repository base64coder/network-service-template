package com.dtc.core.bootstrap.ioc;

import com.dtc.core.messaging.MessageHandlerRegistry;
import com.dtc.core.messaging.handler.MessageHandlerInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dtc.core.network.custom.CustomCodecFactory;
import com.dtc.core.network.custom.CustomConnectionManager;
import com.dtc.core.network.custom.CustomMessageHelper;
import com.dtc.core.network.custom.CustomServer;
import com.dtc.core.network.custom.DefaultCustomCodec;
import com.dtc.core.messaging.NetworkMessageQueue;
import com.dtc.core.network.mqtt.MqttConnectionManager;
import com.dtc.core.network.mqtt.MqttMessageHelper;
import com.dtc.core.network.mqtt.MqttServer;
import com.dtc.core.statistics.StatisticsCollector;
import com.dtc.core.network.tcp.GrpcTcpProtocolHandler;
import com.dtc.core.network.tcp.TcpConnectionManager;
import com.dtc.core.network.tcp.TcpMessageHelper;
import com.dtc.core.network.tcp.TcpProtocolHandler;
import com.dtc.core.network.tcp.TcpServer;
import com.dtc.core.network.udp.UdpMessageHelper;
import com.dtc.core.network.udp.UdpProtocolHandler;
import com.dtc.core.network.udp.UdpServer;
import com.dtc.core.web.argument.HandlerMethodArgumentResolverComposite;
import com.dtc.core.web.argument.PathVariableMethodArgumentResolver;
import com.dtc.core.web.argument.RequestBodyMethodArgumentResolver;
import com.dtc.core.web.argument.RequestParamMethodArgumentResolver;
import com.dtc.core.network.websocket.WebSocketConnectionManager;
import com.dtc.core.network.websocket.WebSocketMessageHelper;
import com.dtc.core.network.websocket.WebSocketServer;
import com.google.inject.AbstractModule;

/**
 * 扩展依赖模块
 * 注册所有扩展所需的依赖类到 Guice 容器
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
        bind(MqttMessageHelper.class).asEagerSingleton();
        bind(MqttConnectionManager.class).asEagerSingleton();

        // 注册 TCP 扩展依赖
        bind(TcpServer.class).asEagerSingleton();
        bind(TcpMessageHelper.class).asEagerSingleton();
        bind(TcpConnectionManager.class).asEagerSingleton();
        bind(TcpProtocolHandler.class).to(GrpcTcpProtocolHandler.class).asEagerSingleton();

        // 注册 WebSocket 扩展依赖
        bind(WebSocketServer.class).asEagerSingleton();
        bind(WebSocketMessageHelper.class).asEagerSingleton();
        bind(WebSocketConnectionManager.class).asEagerSingleton();

        // 注册 Custom 扩展依赖
        bind(CustomServer.class).asEagerSingleton();
        bind(CustomMessageHelper.class).asEagerSingleton();
        bind(CustomConnectionManager.class).asEagerSingleton();
        bind(CustomCodecFactory.class).to(DefaultCustomCodec.class).asEagerSingleton();

        // 注册 UDP 扩展依赖
        bind(UdpServer.class).asEagerSingleton();
        bind(UdpMessageHelper.class).asEagerSingleton();
        bind(UdpProtocolHandler.class).asEagerSingleton();

        // 注册 Web 框架依赖
        bind(HandlerMethodArgumentResolverComposite.class).asEagerSingleton();
        bind(PathVariableMethodArgumentResolver.class).asEagerSingleton();
        bind(RequestParamMethodArgumentResolver.class).asEagerSingleton();
        bind(RequestBodyMethodArgumentResolver.class).asEagerSingleton();
        
        // 注册消息处理器注册表和初始化器
        bind(MessageHandlerRegistry.class).asEagerSingleton();
        bind(MessageHandlerInitializer.class).asEagerSingleton();

        log.info("Extension dependencies configured successfully");
    }
}
