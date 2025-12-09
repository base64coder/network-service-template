package com.dtc.core.network.udp;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dtc.api.annotations.NotNull;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;

/**
 * UDP 消息辅助工具类
 * 提供UDP消息发送和处理的工具方法
 * 
 * @author Network Service Template
 */
@Singleton
public class UdpMessageHelper {

    private static final Logger log = LoggerFactory.getLogger(UdpMessageHelper.class);

    public UdpMessageHelper() {
        log.info("Creating UDP Message Helper instance");
    }

    /**
     * 发送响应
     */
    public void sendResponse(@NotNull ChannelHandlerContext ctx, 
                            @NotNull InetSocketAddress recipient, 
                            @NotNull String response) {
        try {
            ByteBuf buffer = ctx.alloc().buffer();
            buffer.writeBytes(response.getBytes(StandardCharsets.UTF_8));
            DatagramPacket packet = new DatagramPacket(buffer, recipient);
            ctx.writeAndFlush(packet);
            log.debug("Sent UDP response to {}: {}", recipient, response);
        } catch (Exception e) {
            log.error("Failed to send UDP response", e);
        }
    }

    /**
     * 发送二进制数据
     */
    public void sendBinaryData(@NotNull ChannelHandlerContext ctx, 
                              @NotNull InetSocketAddress recipient, 
                              @NotNull byte[] data) {
        try {
            ByteBuf buffer = ctx.alloc().buffer();
            buffer.writeBytes(data);
            DatagramPacket packet = new DatagramPacket(buffer, recipient);
            ctx.writeAndFlush(packet);
            log.debug("Sent UDP binary data to {}: {} bytes", recipient, data.length);
        } catch (Exception e) {
            log.error("Failed to send UDP binary data", e);
        }
    }

    /**
     * 广播消息到所有客户端地址
     */
    public void broadcastMessage(@NotNull ChannelHandlerContext ctx, 
                                 @NotNull InetSocketAddress broadcastAddress, 
                                 @NotNull String message) {
        try {
            ByteBuf buffer = ctx.alloc().buffer();
            buffer.writeBytes(message.getBytes(StandardCharsets.UTF_8));
            DatagramPacket packet = new DatagramPacket(buffer, broadcastAddress);
            ctx.writeAndFlush(packet);
            log.debug("Broadcasted UDP message to {}: {}", broadcastAddress, message);
        } catch (Exception e) {
            log.error("Failed to broadcast UDP message", e);
        }
    }
}
