package com.dtc.core.netty.codec;

import com.dtc.api.annotations.NotNull;
import com.dtc.core.serialization.ProtobufSerializer;
import io.netty.channel.ChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * 编解码器工厂 根据协议类型创建相应的编解码器
 * 
 * @author Network Service Template
 */
@Singleton
public class CodecFactory {

    private static final Logger log = LoggerFactory.getLogger(CodecFactory.class);

    private final @NotNull ProtobufSerializer serializer;

    @Inject
    public CodecFactory(@NotNull ProtobufSerializer serializer) {
        this.serializer = serializer;
    }

    /**
     * 创建解码器
     * 
     * @param protocolType 协议类型
     * @return 解码器
     */
    @NotNull
    public ChannelHandler createDecoder(@NotNull String protocolType) {
        switch (protocolType.toLowerCase()) {
        case "protobuf":
            return new ProtobufDecoder(serializer);
        case "tcp":
        case "simple":
        default:
            return new SimpleMessageDecoder();
        }
    }

    /**
     * 创建编码器
     * 
     * @param protocolType 协议类型
     * @return 编码器
     */
    @NotNull
    public ChannelHandler createEncoder(@NotNull String protocolType) {
        switch (protocolType.toLowerCase()) {
        case "protobuf":
            return new ProtobufEncoder(serializer);
        case "tcp":
        case "simple":
        default:
            return new SimpleMessageEncoder();
        }
    }

    /**
     * 创建编解码器对
     * 
     * @param protocolType 协议类型
     * @return 编解码器对
     */
    @NotNull
    public CodecPair createCodecPair(@NotNull String protocolType) {
        return new CodecPair(createDecoder(protocolType), createEncoder(protocolType));
    }

    /**
     * 编解码器对
     */
    public static class CodecPair {
        private final @NotNull ChannelHandler decoder;
        private final @NotNull ChannelHandler encoder;

        public CodecPair(@NotNull ChannelHandler decoder, @NotNull ChannelHandler encoder) {
            this.decoder = decoder;
            this.encoder = encoder;
        }

        @NotNull
        public ChannelHandler getDecoder() {
            return decoder;
        }

        @NotNull
        public ChannelHandler getEncoder() {
            return encoder;
        }
    }
}
