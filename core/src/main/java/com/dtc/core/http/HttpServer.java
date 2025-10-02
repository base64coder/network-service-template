package com.dtc.core.http;

import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import com.dtc.core.http.handler.HttpServerHandler;
import com.dtc.core.http.handler.HttpRequestDecoder;
import com.dtc.core.http.handler.HttpResponseEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * HTTP 服务器 基于 Netty 的 HTTP 服务器实现，支持 REST API
 * 
 * @author Network Service Template
 */
@Singleton
public class HttpServer {

    private static final Logger log = LoggerFactory.getLogger(HttpServer.class);
    private static final int MAX_CONTENT_LENGTH = 1024 * 1024; // 1MB
    private static final int READ_TIMEOUT = 30; // 30 seconds
    private static final int WRITE_TIMEOUT = 30; // 30 seconds

    private final HttpRequestHandler requestHandler;
    private final HttpResponseHandler responseHandler;
    private final HttpRouteManager routeManager;
    private final HttpMiddlewareManager middlewareManager;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;
    private final AtomicBoolean started = new AtomicBoolean(false);
    private final AtomicInteger activeConnections = new AtomicInteger(0);
    private final ConcurrentHashMap<String, Channel> clientChannels = new ConcurrentHashMap<>();

    @Inject
    public HttpServer(@NotNull HttpRequestHandler requestHandler, @NotNull HttpResponseHandler responseHandler,
            @NotNull HttpRouteManager routeManager, @NotNull HttpMiddlewareManager middlewareManager) {
        this.requestHandler = requestHandler;
        this.responseHandler = responseHandler;
        this.routeManager = routeManager;
        this.middlewareManager = middlewareManager;
    }

    /**
     * 启动 HTTP 服务器
     * 
     * @param port 端口号
     * @throws Exception 启动异常
     */
    public void start(int port) throws Exception {
        if (started.compareAndSet(false, true)) {
            log.info("🚀 Starting HTTP server on port {}", port);

            // 创建事件循环组
            bossGroup = new NioEventLoopGroup(1);
            workerGroup = new NioEventLoopGroup();

            try {
                ServerBootstrap bootstrap = new ServerBootstrap();
                bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                        .option(ChannelOption.SO_BACKLOG, 128).childOption(ChannelOption.SO_KEEPALIVE, true)
                        .childOption(ChannelOption.TCP_NODELAY, true)
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(@NotNull SocketChannel ch) {
                                ChannelPipeline pipeline = ch.pipeline();

                                // 添加日志处理器
                                pipeline.addLast("logging", new LoggingHandler(LogLevel.INFO));

                                // 添加超时处理器
                                pipeline.addLast("idleStateHandler",
                                        new IdleStateHandler(READ_TIMEOUT, WRITE_TIMEOUT, 0));

                                // 添加 HTTP 编解码器
                                pipeline.addLast("httpServerCodec", new HttpServerCodec());

                                // 添加 HTTP 对象聚合器
                                pipeline.addLast("httpObjectAggregator", new HttpObjectAggregator(MAX_CONTENT_LENGTH));

                                // 添加自定义处理器
                                pipeline.addLast("httpRequestDecoder", new HttpRequestDecoder());
                                pipeline.addLast("httpResponseEncoder", new HttpResponseEncoder());
                                pipeline.addLast("httpServerHandler", new HttpServerHandler(requestHandler,
                                        responseHandler, routeManager, middlewareManager, HttpServer.this));
                            }
                        });

                // 绑定端口并启动服务器
                ChannelFuture future = bootstrap.bind(port).sync();
                serverChannel = future.channel();

                log.info("✅ HTTP server started successfully on port {}", port);

            } catch (Exception e) {
                log.error("❌ Failed to start HTTP server", e);
                started.set(false);
                throw e;
            }
        }
    }

    /**
     * 启动 HTTP 服务器（使用默认端口）
     * 
     * @throws Exception 启动异常
     */
    public void start() throws Exception {
        start(8080);
    }

    /**
     * 停止 HTTP 服务器
     * 
     * @throws Exception 停止异常
     */
    public void stop() throws Exception {
        if (started.compareAndSet(true, false)) {
            log.info("🛑 Stopping HTTP server...");

            try {
                // 关闭所有客户端连接
                for (Channel channel : clientChannels.values()) {
                    if (channel.isActive()) {
                        channel.close();
                    }
                }
                clientChannels.clear();

                // 关闭服务器通道
                if (serverChannel != null && serverChannel.isActive()) {
                    serverChannel.close().sync();
                }

                // 关闭事件循环组
                if (workerGroup != null) {
                    workerGroup.shutdownGracefully();
                }
                if (bossGroup != null) {
                    bossGroup.shutdownGracefully();
                }

                log.info("✅ HTTP server stopped successfully");
            } catch (Exception e) {
                log.error("❌ Error stopping HTTP server", e);
                throw e;
            }
        }
    }

    /**
     * 是否已启动
     * 
     * @return 是否已启动
     */
    public boolean isStarted() {
        return started.get();
    }

    /**
     * 获取活跃连接数
     * 
     * @return 活跃连接数
     */
    public int getActiveConnections() {
        return activeConnections.get();
    }

    /**
     * 添加客户端连接
     * 
     * @param clientId 客户端 ID
     * @param channel  通道
     */
    public void addClientConnection(@NotNull String clientId, @NotNull Channel channel) {
        clientChannels.put(clientId, channel);
        activeConnections.incrementAndGet();
        log.debug("Client connected: {} (total: {})", clientId, activeConnections.get());
    }

    /**
     * 移除客户端连接
     * 
     * @param clientId 客户端 ID
     */
    public void removeClientConnection(@NotNull String clientId) {
        Channel channel = clientChannels.remove(clientId);
        if (channel != null) {
            activeConnections.decrementAndGet();
            log.debug("Client disconnected: {} (total: {})", clientId, activeConnections.get());
        }
    }

    /**
     * 获取客户端通道
     * 
     * @param clientId 客户端 ID
     * @return 客户端通道
     */
    @Nullable
    public Channel getClientChannel(@NotNull String clientId) {
        return clientChannels.get(clientId);
    }

    /**
     * 发送响应给客户端
     * 
     * @param clientId 客户端 ID
     * @param response HTTP 响应
     */
    public void sendResponse(@NotNull String clientId, @NotNull HttpResponse response) {
        Channel channel = getClientChannel(clientId);
        if (channel != null && channel.isActive()) {
            channel.writeAndFlush(response);
        } else {
            log.warn("Client channel not found or inactive: {}", clientId);
        }
    }

    /**
     * 广播响应给所有客户端
     * 
     * @param response HTTP 响应
     */
    public void broadcastResponse(@NotNull HttpResponse response) {
        for (String clientId : clientChannels.keySet()) {
            sendResponse(clientId, response);
        }
    }

    /**
     * 获取服务器统计信息
     * 
     * @return 统计信息
     */
    @NotNull
    public HttpServerStats getStats() {
        return new HttpServerStats(isStarted(), getActiveConnections(), clientChannels.size(),
                System.currentTimeMillis());
    }

    /**
     * HTTP 服务器统计信息
     */
    public static class HttpServerStats {
        private final boolean started;
        private final int activeConnections;
        private final int totalClients;
        private final long timestamp;

        public HttpServerStats(boolean started, int activeConnections, int totalClients, long timestamp) {
            this.started = started;
            this.activeConnections = activeConnections;
            this.totalClients = totalClients;
            this.timestamp = timestamp;
        }

        public boolean isStarted() {
            return started;
        }

        public int getActiveConnections() {
            return activeConnections;
        }

        public int getTotalClients() {
            return totalClients;
        }

        public long getTimestamp() {
            return timestamp;
        }

        @Override
        public String toString() {
            return String.format("HttpServerStats{started=%s, activeConnections=%d, totalClients=%d, timestamp=%d}",
                    started, activeConnections, totalClients, timestamp);
        }
    }
}
