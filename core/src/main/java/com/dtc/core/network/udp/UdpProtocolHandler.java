package com.dtc.core.network.udp;

import com.dtc.api.annotations.NotNull;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;

/**
 * UDP 协议处理器
 * 负责处理 UDP 协议相关的消息和连接管理
 * 
 * @author Network Service Template
 */
@Singleton
public class UdpProtocolHandler {

    private static final Logger log = LoggerFactory.getLogger(UdpProtocolHandler.class);

    // 客户端管理：UDP是无连接的，所以这里记录的是最近通信的客户端地址
    private final ConcurrentHashMap<String, InetSocketAddress> activeClients = new ConcurrentHashMap<>();

    public UdpProtocolHandler() {
        log.info("Creating UDP Protocol Handler instance");
    }

    /**
     * 处理客户端消息，UDP是无连接的，所以这里记录的是最近通信的客户端地址
     */
    public void handleMessage(@NotNull ChannelHandlerContext ctx, 
                             @NotNull DatagramPacket packet) {
        InetSocketAddress sender = packet.sender();
        String clientId = generateClientId(sender);
        
        // 记录活动客户端
        activeClients.put(clientId, sender);
        
        log.debug("UDP message from client: {} ({})", clientId, sender);
    }

    /**
     * 处理客户端断开，UDP是无连接的，所以这里清除的是客户端地址记录
     */
    public void handleDisconnect(@NotNull String clientId) {
        activeClients.remove(clientId);
        log.debug("UDP client disconnected: {}", clientId);
    }

    /**
     * 生成客户端ID
     */
    @NotNull
    private String generateClientId(@NotNull InetSocketAddress address) {
        return String.format("udp-%s-%d", address.getAddress().getHostAddress(), address.getPort());
    }

    /**
     * 获取活动客户端数量
     */
    public int getActiveClientCount() {
        return activeClients.size();
    }

    /**
     * 获取所有活动客户端
     */
    @NotNull
    public ConcurrentHashMap<String, InetSocketAddress> getActiveClients() {
        return new ConcurrentHashMap<>(activeClients);
    }

    /**
     * 检查客户端是否存在
     */
    public boolean hasClient(@NotNull String clientId) {
        return activeClients.containsKey(clientId);
    }

    /**
     * 获取客户端地址
     */
    public InetSocketAddress getClientAddress(@NotNull String clientId) {
        return activeClients.get(clientId);
    }
}
