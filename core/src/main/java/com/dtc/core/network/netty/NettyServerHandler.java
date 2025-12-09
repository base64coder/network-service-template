package com.dtc.core.network.netty;

import com.dtc.api.ProtocolExtension;
import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import com.dtc.core.messaging.NetworkMessageHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Netty 服务器处理器
 * 处理客户端连接和消息
 * 
 * @author Network Service Template
 */
@Singleton
public class NettyServerHandler extends ChannelInboundHandlerAdapter {

    private static final Logger log = LoggerFactory.getLogger(NettyServerHandler.class);

    private final @NotNull NetworkMessageHandler messageHandler;
    private final @Nullable ProtocolExtension protocolExtension;
    private final AtomicLong connectionCount = new AtomicLong(0);

    @Inject
    public NettyServerHandler(@NotNull NetworkMessageHandler messageHandler) {
        this.messageHandler = messageHandler;
        this.protocolExtension = null;
    }

    public NettyServerHandler(@NotNull NetworkMessageHandler messageHandler,
            @Nullable ProtocolExtension protocolExtension) {
        this.messageHandler = messageHandler;
        this.protocolExtension = protocolExtension;
    }

    @Override
    public void channelActive(@NotNull ChannelHandlerContext ctx) {
        long connectionId = connectionCount.incrementAndGet();
        String clientId = "client-" + connectionId;

        log.info("🔍 Client connected: {} (ID: {})", ctx.channel().remoteAddress(), clientId);

        // 如果有协议扩展，调用连接处理方法
        if (protocolExtension != null) {
            try {
                protocolExtension.onConnect(ctx, clientId);
                log.debug("Protocol extension {} handled connection for {}", protocolExtension.getProtocolName(),
                        clientId);
            } catch (Exception e) {
                log.error("Protocol extension failed to handle connection for {}", clientId, e);
            }
        }
    }

    @Override
    public void channelInactive(@NotNull ChannelHandlerContext ctx) {
        long connectionId = connectionCount.get();
        String clientId = "client-" + connectionId;

        log.info("🔍 Client disconnected: {} (ID: {})", ctx.channel().remoteAddress(), clientId);

        // 如果有协议扩展，调用断开连接处理方法
        if (protocolExtension != null) {
            try {
                protocolExtension.onDisconnect(ctx, clientId);
                log.debug("Protocol extension {} handled disconnection for {}", protocolExtension.getProtocolName(),
                        clientId);
            } catch (Exception e) {
                log.error("Protocol extension failed to handle disconnection for {}", clientId, e);
            }
        }
    }

    @Override
    public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object msg) {
        try {
            log.debug("📨 Received message from {}: {} bytes", ctx.channel().remoteAddress(),
                    msg instanceof byte[] ? ((byte[]) msg).length : "unknown");

            // 如果有协议扩展，让协议扩展处理消息
            if (protocolExtension != null) {
                try {
                    protocolExtension.onMessage(ctx, msg);
                    log.debug("Protocol extension {} handled message", protocolExtension.getProtocolName());
                } catch (Exception e) {
                    log.error("Protocol extension failed to handle message", e);
                }
            }

            // 否则使用消息处理器处理消息
            if (msg instanceof byte[]) {
                boolean success = messageHandler.handleRawData((byte[]) msg);
                if (success) {
                    log.debug("✅ Message processed successfully");
                } else {
                    log.warn("⚠️  Failed to process message");
                }
            } else {
                // 处理非字节数组消息
                log.debug("📨 Processing non-byte message: {}", msg.getClass().getSimpleName());
                // 可以通过路由管理器实现非字节数组消息的处理逻辑，例如调用handleMessage
                // 注意：handleMessage需要Message类型的参数
                log.debug("✅ Non-byte message logged successfully");
            }

        } catch (Exception e) {
            log.error("❌ Error processing message from {}", ctx.channel().remoteAddress(), e);

            // 如果有协议扩展，调用异常处理方法
            if (protocolExtension != null) {
                try {
                    protocolExtension.onException(ctx, e);
                } catch (Exception ex) {
                    log.error("Protocol extension failed to handle exception", ex);
                }
            }
        }
    }

    @Override
    public void exceptionCaught(@NotNull ChannelHandlerContext ctx, @NotNull Throwable cause) {
        log.error("❌ Exception in channel from {}", ctx.channel().remoteAddress(), cause);

        // 如果有协议扩展，调用异常处理方法
        if (protocolExtension != null) {
            try {
                protocolExtension.onException(ctx, cause);
            } catch (Exception e) {
                log.error("Protocol extension failed to handle exception", e);
            }
        }

        ctx.close();
    }
}
