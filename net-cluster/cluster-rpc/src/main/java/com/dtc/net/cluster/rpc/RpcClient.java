package com.dtc.net.cluster.rpc;

import com.dtc.net.cluster.rpc.proto.RpcRequest;
import com.dtc.net.cluster.rpc.proto.RpcResponse;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * RPC 客户端
 * 负责发送请求到远程服务
 */
@Singleton
public class RpcClient {
    
    private static final Logger log = LoggerFactory.getLogger(RpcClient.class);
    
    private final EventLoopGroup group = new NioEventLoopGroup();
    private final Map<String, Channel> channelCache = new ConcurrentHashMap<>();
    private final Map<String, CompletableFuture<RpcResponse>> pendingRequests = new ConcurrentHashMap<>();
    
    public CompletableFuture<RpcResponse> send(String host, int port, RpcRequest request) {
        String address = host + ":" + port;
        Channel channel = getChannel(address, host, port);
        
        if (channel == null || !channel.isActive()) {
            return CompletableFuture.failedFuture(new RuntimeException("Failed to connect to " + address));
        }
        
        CompletableFuture<RpcResponse> future = new CompletableFuture<>();
        pendingRequests.put(request.getRequestId(), future);
        
        channel.writeAndFlush(request).addListener((ChannelFutureListener) f -> {
            if (!f.isSuccess()) {
                pendingRequests.remove(request.getRequestId());
                future.completeExceptionally(f.cause());
            }
        });
        
        // Timeout handling
        CompletableFuture.delayedExecutor(request.getTimeout(), TimeUnit.MILLISECONDS).execute(() -> {
            if (pendingRequests.remove(request.getRequestId()) != null) {
                future.completeExceptionally(new java.util.concurrent.TimeoutException("RPC request timed out"));
            }
        });
        
        return future;
    }
    
    private Channel getChannel(String key, String host, int port) {
        if (channelCache.containsKey(key)) {
            Channel ch = channelCache.get(key);
            if (ch.isActive()) {
                return ch;
            } else {
                channelCache.remove(key);
            }
        }
        
        synchronized (this) {
            if (channelCache.containsKey(key)) {
                return channelCache.get(key);
            }
            
            try {
                Bootstrap b = new Bootstrap();
                b.group(group)
                 .channel(NioSocketChannel.class)
                 .handler(new ChannelInitializer<SocketChannel>() {
                     @Override
                     public void initChannel(SocketChannel ch) {
                         ch.pipeline().addLast(new ProtobufVarint32FrameDecoder());
                         ch.pipeline().addLast(new ProtobufDecoder(RpcResponse.getDefaultInstance()));
                         ch.pipeline().addLast(new ProtobufVarint32LengthFieldPrepender());
                         ch.pipeline().addLast(new ProtobufEncoder());
                         ch.pipeline().addLast(new RpcClientHandler());
                     }
                 })
                 .option(ChannelOption.TCP_NODELAY, true);
                 
                ChannelFuture f = b.connect(host, port).sync();
                Channel ch = f.channel();
                channelCache.put(key, ch);
                return ch;
                
            } catch (Exception e) {
                log.error("Failed to connect to " + key, e);
                return null;
            }
        }
    }
    
    private class RpcClientHandler extends SimpleChannelInboundHandler<RpcResponse> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, RpcResponse response) {
            CompletableFuture<RpcResponse> future = pendingRequests.remove(response.getRequestId());
            if (future != null) {
                future.complete(response);
            }
        }
        
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            log.error("RPC client exception", cause);
            ctx.close();
        }
    }
    
    public void stop() {
        group.shutdownGracefully();
    }
}

