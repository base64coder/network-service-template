package com.dtc.core.network.tcp;

import com.dtc.api.annotations.NotNull;
import com.dtc.core.tcp.proto.TcpServiceGrpc;
import com.dtc.core.tcp.proto.TcpServiceProto;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Singleton;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * gRPC TCP 协议处理器
 * 支持 Google Protobuf 协议和 gRPC 框架
 * 
 * @author Network Service Template
 */
@Singleton
public class GrpcTcpProtocolHandler extends TcpProtocolHandler {

    private static final Logger log = LoggerFactory.getLogger(GrpcTcpProtocolHandler.class);

    private Server grpcServer;
    private final ConcurrentHashMap<String, ChannelHandlerContext> grpcConnections = new ConcurrentHashMap<>();

    public GrpcTcpProtocolHandler() {
        log.info("Creating gRPC TCP Protocol Handler instance");
    }

    @Override
    public void handleConnect(@NotNull ChannelHandlerContext ctx, @NotNull String clientId) {
        log.info("gRPC client connected: {} from {}", clientId, ctx.channel().remoteAddress());
        grpcConnections.put(clientId, ctx);

        // 启动 gRPC 服务器，如果尚未启动
        startGrpcServer();
    }

    @Override
    public void handleDisconnect(@NotNull ChannelHandlerContext ctx, @NotNull String clientId) {
        log.info("gRPC client disconnected: {}", clientId);
        grpcConnections.remove(clientId);
    }

    @Override
    public void handleMessage(@NotNull ChannelHandlerContext ctx, @NotNull Object message) {
        log.debug("Handling gRPC message from client: {}", ctx.channel().remoteAddress());

        // 处理 gRPC 消息
        if (message instanceof TcpServiceProto.TcpRequest) {
            handleGrpcRequest(ctx, (TcpServiceProto.TcpRequest) message);
        } else if (message instanceof String) {
            handleGrpcMessage(ctx, (String) message);
        } else {
            log.warn("Received unsupported message type in gRPC handler: {}", message.getClass().getSimpleName());
        }
    }

    @Override
    public void handleException(@NotNull ChannelHandlerContext ctx, @NotNull Throwable cause) {
        log.error("gRPC protocol error from client: {}", ctx.channel().remoteAddress(), cause);
    }

    @Override
    @NotNull
    public String getProtocolName() {
        return "gRPC-TCP";
    }

    @Override
    @NotNull
    public String getProtocolVersion() {
        return "1.0.0";
    }

    @Override
    public boolean supports(@NotNull Class<?> messageType) {
        return TcpServiceProto.TcpRequest.class.isAssignableFrom(messageType) ||
                String.class.isAssignableFrom(messageType);
    }

    /**
     * 启动 gRPC 服务器
     */
    private void startGrpcServer() {
        if (grpcServer == null || grpcServer.isShutdown()) {
            try {
                grpcServer = ServerBuilder.forPort(9090)
                        .addService(new TcpGrpcService())
                        .build()
                        .start();

                log.info("gRPC server started on port 9090");

                // 添加关闭钩子
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    log.info("Shutting down gRPC server...");
                    grpcServer.shutdown();
                }));

            } catch (IOException e) {
                log.error("Failed to start gRPC server", e);
            }
        }
    }

    /**
     * 处理 gRPC 请求
     */
    private void handleGrpcRequest(@NotNull ChannelHandlerContext ctx, @NotNull TcpServiceProto.TcpRequest request) {
        log.debug("Processing gRPC request: {}", request.getMessage());

        // 创建响应
        TcpServiceProto.TcpResponse response = TcpServiceProto.TcpResponse.newBuilder()
                .setMessage("Echo: " + request.getMessage())
                .setTimestamp(System.currentTimeMillis())
                .setStatus("SUCCESS")
                .build();

        // 发送响应
        ctx.writeAndFlush(response);
        log.debug("Sent gRPC response: {}", response.getMessage());
    }

    /**
     * 处理 gRPC 消息
     */
    private void handleGrpcMessage(@NotNull ChannelHandlerContext ctx, @NotNull String message) {
        log.debug("Processing gRPC message: {}", message);

        // 可以通过路由管理器实现 gRPC 消息处理逻辑
        processGrpcMessage(ctx, message);
    }

    /**
     * 处理 gRPC 消息
     */
    private void processGrpcMessage(@NotNull ChannelHandlerContext ctx, @NotNull String message) {
        // 可以通过路由管理器实现 gRPC 消息处理逻辑
        log.debug("Processing gRPC message: {}", message);

        // 简单示例：处理常见命令
        if (message.trim().equals("ping")) {
            sendGrpcResponse(ctx, "pong");
        } else if (message.trim().equals("hello")) {
            sendGrpcResponse(ctx, "Hello from gRPC TCP Server!");
        } else {
            sendGrpcResponse(ctx, "Echo: " + message);
        }
    }

    /**
     * 发送 gRPC 响应
     */
    private void sendGrpcResponse(@NotNull ChannelHandlerContext ctx, @NotNull String response) {
        try {
            ctx.writeAndFlush(response);
            log.debug("Sent gRPC response: {}", response);
        } catch (Exception e) {
            log.error("Failed to send gRPC response", e);
        }
    }

    /**
     * 获取活动 gRPC 连接数量
     */
    public int getActiveGrpcConnectionCount() {
        return grpcConnections.size();
    }

    /**
     * 关闭所有 gRPC 连接
     */
    public void closeAllGrpcConnections() {
        log.info("Closing all gRPC connections...");
        for (String clientId : grpcConnections.keySet()) {
            try {
                ChannelHandlerContext ctx = grpcConnections.get(clientId);
                if (ctx != null && ctx.channel().isActive()) {
                    ctx.close();
                }
            } catch (Exception e) {
                log.warn("Failed to close gRPC connection for client: {}", clientId, e);
            }
        }
        grpcConnections.clear();
    }

    /**
     * 停止 gRPC 服务器
     */
    public void stopGrpcServer() {
        if (grpcServer != null && !grpcServer.isShutdown()) {
            log.info("Stopping gRPC server...");
            grpcServer.shutdown();
        }
    }

    /**
     * gRPC 服务实现
     */
    private static class TcpGrpcService extends TcpServiceGrpc.TcpServiceImplBase {

        @Override
        public void processMessage(TcpServiceProto.TcpRequest request,
                StreamObserver<TcpServiceProto.TcpResponse> responseObserver) {

            log.debug("Processing gRPC request: {}", request.getMessage());

            // 创建响应
            TcpServiceProto.TcpResponse response = TcpServiceProto.TcpResponse.newBuilder()
                    .setMessage("Echo: " + request.getMessage())
                    .setTimestamp(System.currentTimeMillis())
                    .setStatus("SUCCESS")
                    .build();

            // 发送响应
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

        @Override
        public StreamObserver<TcpServiceProto.TcpRequest> processMessageStream(
                StreamObserver<TcpServiceProto.TcpResponse> responseObserver) {

            return new StreamObserver<TcpServiceProto.TcpRequest>() {
                @Override
                public void onNext(TcpServiceProto.TcpRequest request) {
                    log.debug("Processing stream request: {}", request.getMessage());

                    // 创建响应
                    TcpServiceProto.TcpResponse response = TcpServiceProto.TcpResponse.newBuilder()
                            .setMessage("Stream Echo: " + request.getMessage())
                            .setTimestamp(System.currentTimeMillis())
                            .setStatus("SUCCESS")
                            .build();

                    // 发送响应
                    responseObserver.onNext(response);
                }

                @Override
                public void onError(Throwable t) {
                    log.error("Stream error", t);
                    responseObserver.onError(t);
                }

                @Override
                public void onCompleted() {
                    log.debug("Stream completed");
                    responseObserver.onCompleted();
                }
            };
        }
    }
}
