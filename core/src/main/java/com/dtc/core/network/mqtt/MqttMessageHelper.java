package com.dtc.core.network.mqtt;

import com.dtc.api.annotations.NotNull;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;

/**
 * MQTT 消息辅助工具类
 * 提供MQTT消息发送和处理的工具方法
 * 
 * @author Network Service Template
 */
@Singleton
public class MqttMessageHelper {

    private static final Logger log = LoggerFactory.getLogger(MqttMessageHelper.class);

    public MqttMessageHelper() {
        log.info("Creating MQTT Message Helper instance");
    }

    /**
     * 发送 MQTT 消息
     */
    public void sendMessage(@NotNull ChannelHandlerContext ctx, @NotNull Object message) {
        try {
            ctx.writeAndFlush(message);
            log.debug("Sent MQTT message to client: {}", ctx.channel().remoteAddress());
        } catch (Exception e) {
            log.error("Failed to send MQTT message", e);
        }
    }

    /**
     * 广播消息到所有连接的客户端
     */
    public void broadcastMessage(@NotNull Object message) {
        log.debug("Broadcasting MQTT message to all connected clients");
        // 可以通过路由管理器实现消息广播逻辑
    }
}
