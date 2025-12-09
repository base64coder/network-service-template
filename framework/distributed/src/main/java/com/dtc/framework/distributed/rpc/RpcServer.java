package com.dtc.framework.distributed.rpc;

import com.dtc.core.network.netty.NettyBootstrap;
import com.dtc.framework.distributed.rpc.proto.RpcRequest;
import com.dtc.framework.distributed.rpc.proto.RpcResponse;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;

/**
 * RPC 服务器
 * 独立监听一个端口，用于处理内部 RPC 请求
 */
@Singleton
public class RpcServer {
    
    private static final Logger log = LoggerFactory.getLogger(RpcServer.class);
    
    private final RpcHandler rpcHandler;
    private Channel channel;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    
    @Inject
    public RpcServer(RpcProviderRegistry providerRegistry) {
        this.rpcHandler = new RpcHandler(providerRegistry);
    }
    
    public void start(int port) {
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();
        
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
             .channel(NioServerSocketChannel.class)
             .childHandler(new ChannelInitializer<SocketChannel>() {
                 @Override
                 public void initChannel(SocketChannel ch) {
                     ch.pipeline().addLast(new ProtobufVarint32FrameDecoder());
                     ch.pipeline().addLast(new ProtobufDecoder(RpcRequest.getDefaultInstance()));
                     ch.pipeline().addLast(new ProtobufVarint32LengthFieldPrepender());
                     ch.pipeline().addLast(new ProtobufEncoder());
                     ch.pipeline().addLast(new RpcServerHandler(rpcHandler));
                 }
             })
             .option(ChannelOption.SO_BACKLOG, 128)
             .childOption(ChannelOption.SO_KEEPALIVE, true);
             
            ChannelFuture f = b.bind(port).sync();
            this.channel = f.channel();
            log.info("RPC Server started on port {}", port);
            
        } catch (InterruptedException e) {
            log.error("Interrupted while starting RPC server", e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("Failed to start RPC server", e);
        }
    }
    
    public void stop() {
        if (channel != null) {
            channel.close();
        }
        if (bossGroup != null) bossGroup.shutdownGracefully();
        if (workerGroup != null) workerGroup.shutdownGracefully();
    }
    
    private static class RpcServerHandler extends SimpleChannelInboundHandler<RpcRequest> {
        private final RpcHandler handler;
        
        public RpcServerHandler(RpcHandler handler) {
            this.handler = handler;
        }
        
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, RpcRequest request) {
            // Async execution in future optimization
            RpcResponse response = handler.handle(request);
            ctx.writeAndFlush(response);
        }
        
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            log.error("RPC handler exception", cause);
            ctx.close();
        }
    }
}

