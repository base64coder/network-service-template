package com.dtc.core.netty;

import com.dtc.api.ProtocolExtension;
import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import com.dtc.core.extensions.ExtensionManager;
import com.dtc.core.extensions.NetworkExtension;
import com.dtc.core.messaging.NetworkMessageHandler;
import com.dtc.core.netty.codec.CodecFactory;
import com.dtc.core.netty.PipelineConfigurer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Netty服务器 基于Netty的网络服务器实现 根据发现的扩展动态启动相关的端口和支持的协议
 * 
 * @author Network Service Template
 */
@Singleton
public class NettyServer {

    private static final Logger log = LoggerFactory.getLogger(NettyServer.class);
    private static final int MAX_FRAME_LENGTH = 1024 * 1024; // 1MB

    private final @NotNull CodecFactory codecFactory;
    private final @NotNull NetworkMessageHandler messageHandler;
    private final @NotNull ExtensionManager extensionManager;
    private final @NotNull PipelineConfigurer pipelineConfigurer;
    private final AtomicBoolean started = new AtomicBoolean(false);
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private final Map<Integer, Channel> serverChannels = new ConcurrentHashMap<>();
    private final Map<Integer, ProtocolExtension> portExtensions = new ConcurrentHashMap<>();

    @Inject
    public NettyServer(@NotNull CodecFactory codecFactory, @NotNull NetworkMessageHandler messageHandler,
            @NotNull ExtensionManager extensionManager, @NotNull PipelineConfigurer pipelineConfigurer) {
        this.codecFactory = codecFactory;
        this.messageHandler = messageHandler;
        this.extensionManager = extensionManager;
        this.pipelineConfigurer = pipelineConfigurer;
    }

    /**
     * 启动服务器 根据发现的扩展动态启动相关的端口和支持的协议
     * 
     * @throws Exception 启动异常
     */
    public void start() throws Exception {
        if (started.compareAndSet(false, true)) {
            log.info("🚀 Starting Netty server with dynamic protocol support...");

            // 创建事件循环组
            bossGroup = new NioEventLoopGroup(1);
            workerGroup = new NioEventLoopGroup();

            try {
                // 获取所有已注册的扩展
                Map<String, NetworkExtension> extensions = extensionManager.getAllExtensions();
                log.info("📦 Found {} registered extensions", extensions.size());

                // 扫描协议扩展
                List<ProtocolExtension> protocolExtensions = scanProtocolExtensions(extensions);
                log.info("🔌 Found {} protocol extensions", protocolExtensions.size());

                if (protocolExtensions.isEmpty()) {
                    log.warn("⚠️ No protocol extensions found, starting default server on port 9090");
                    startDefaultServer();
                } else {
                    // 为每个协议扩展启动对应的服务器
                    for (ProtocolExtension extension : protocolExtensions) {
                        startProtocolServer(extension);
                    }
                }

                log.info("✅ Netty server started successfully with {} active ports", serverChannels.size());

            } catch (Exception e) {
                log.error("❌ Failed to start Netty server", e);
                started.set(false);
                throw e;
            }
        }
    }

    /**
     * 停止服务器
     * 
     * @throws Exception 停止异常
     */
    public void stop() throws Exception {
        if (started.compareAndSet(true, false)) {
            log.info("🛑 Stopping Netty server...");

            try {
                // 关闭所有服务器通道
                for (Map.Entry<Integer, Channel> entry : serverChannels.entrySet()) {
                    int port = entry.getKey();
                    Channel channel = entry.getValue();
                    log.info("Closing server on port {}", port);
                    channel.close().sync();
                }
                serverChannels.clear();
                portExtensions.clear();

                // 关闭事件循环组
                if (workerGroup != null) {
                    workerGroup.shutdownGracefully();
                }
                if (bossGroup != null) {
                    bossGroup.shutdownGracefully();
                }

                log.info("✅ Netty server stopped successfully");
            } catch (Exception e) {
                log.error("❌ Error stopping Netty server", e);
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
     * 获取活跃的端口数量
     * 
     * @return 活跃端口数量
     */
    public int getActivePortCount() {
        return serverChannels.size();
    }

    /**
     * 获取活跃的端口列表
     * 
     * @return 活跃端口列表
     */
    @NotNull
    public List<Integer> getActivePorts() {
        return List.copyOf(serverChannels.keySet());
    }

    // ========== 私有方法 ==========

    /**
     * 扫描协议扩展
     * 
     * @param extensions 所有扩展
     * @return 协议扩展列表
     */
    @NotNull
    private List<ProtocolExtension> scanProtocolExtensions(
            @NotNull Map<String, NetworkExtension> extensions) {
        List<ProtocolExtension> protocolExtensions = new ArrayList<>();

        for (NetworkExtension extension : extensions.values()) {
            if (extension.isEnabled() && extension.isStarted()) {
                // 检查扩展是否实现了 ProtocolExtension 接口
                if (extension instanceof ProtocolExtension) {
                    protocolExtensions.add((ProtocolExtension) extension);
                    log.info("🔌 Found protocol extension: {} on port {}",
                            ((ProtocolExtension) extension).getProtocolName(),
                            ((ProtocolExtension) extension).getDefaultPort());
                }
            }
        }

        return protocolExtensions;
    }

    /**
     * 启动默认服务器（当没有协议扩展时）
     * 
     * @throws Exception 启动异常
     */
    private void startDefaultServer() throws Exception {
        log.info("🌐 Starting default server on port 9090");
        startServerOnPort(9090, null);
    }

    /**
     * 为协议扩展启动服务器
     * 
     * @param extension 协议扩展
     * @throws Exception 启动异常
     */
    private void startProtocolServer(@NotNull ProtocolExtension extension) throws Exception {
        int port = extension.getDefaultPort();
        String protocolName = extension.getProtocolName();

        log.info("🚀 Starting {} server on port {}", protocolName, port);

        // 检查端口是否已被占用
        if (serverChannels.containsKey(port)) {
            log.warn("⚠️ Port {} is already in use, skipping {}", port, protocolName);
            return;
        }

        startServerOnPort(port, extension);
    }

    /**
     * 在指定端口启动服务器
     * 
     * @param port      端口号
     * @param extension 协议扩展（可为null）
     * @throws Exception 启动异常
     */
    private void startServerOnPort(int port, @Nullable ProtocolExtension extension) throws Exception {
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 128).childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(@NotNull SocketChannel ch) {
                        ChannelPipeline pipeline = ch.pipeline();

                        // 添加日志处理器
                        pipeline.addLast("logging", new LoggingHandler(LogLevel.INFO));

                        // 配置Pipeline
                        pipelineConfigurer.configurePipeline(pipeline, extension);

                        // 添加业务处理器
                        pipeline.addLast("handler", new NettyServerHandler(messageHandler, extension));
                    }
                });

        // 绑定端口并启动服务器
        ChannelFuture future = bootstrap.bind(port).sync();
        Channel channel = future.channel();

        // 保存服务器通道和协议扩展
        serverChannels.put(port, channel);
        if (extension != null) {
            portExtensions.put(port, extension);
        }

        log.info("✅ Server started successfully on port {} with protocol {}", port,
                extension != null ? extension.getProtocolName() : "default");
    }
}
