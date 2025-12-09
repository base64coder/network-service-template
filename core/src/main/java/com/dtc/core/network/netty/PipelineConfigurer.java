package com.dtc.core.network.netty;

import com.dtc.api.ProtocolExtension;
import com.dtc.api.annotations.NotNull;
import com.dtc.api.annotations.Nullable;
import com.dtc.core.network.netty.codec.CodecFactory;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Pipelineé°å¶çé£? * ç¼ç¶ç«´ç» ï¼æéå­îéå¿îé¨å±ipelineé°å¶ç
 * 
 * @author Network Service Template
 */
@Singleton
public class PipelineConfigurer {

    private static final Logger log = LoggerFactory.getLogger(PipelineConfigurer.class);
    private static final int MAX_FRAME_LENGTH = 1024 * 1024; // 1MB

    private final CodecFactory codecFactory;
    private final Map<String, BiConsumer<ChannelPipeline, ProtocolExtension>> pipelineConfigurers;

    @Inject
    public PipelineConfigurer(@NotNull CodecFactory codecFactory) {
        this.codecFactory = codecFactory;
        this.pipelineConfigurers = initializePipelineConfigurers();
    }

    /**
     * éæ¿îéæ ipelineé°å¶çé£ã¦æ§§ç
     */
    @NotNull
    private Map<String, BiConsumer<ChannelPipeline, ProtocolExtension>> initializePipelineConfigurers() {
        return Map.of(
                "http", this::configureHttpPipeline,
                "websocket", this::configureWebSocketPipeline,
                "mqtt", this::configureMqttPipeline,
                "tcp", this::configureTcpPipeline,
                "customprotocol", this::configureCustomPipeline);
    }

    /**
     * é°å¶çPipeline
     */
    public void configurePipeline(@NotNull ChannelPipeline pipeline, @Nullable ProtocolExtension extension) {
        if (extension == null) {
            configureDefaultPipeline(pipeline, null);
            return;
        }

        String protocolName = extension.getProtocolName().toLowerCase();
        BiConsumer<ChannelPipeline, ProtocolExtension> configurer = pipelineConfigurers.get(protocolName);

        if (configurer != null) {
            configurer.accept(pipeline, extension);
            log.debug("é?Configured {} pipeline for extension: {}", protocolName, extension.getProtocolName());
        } else {
            log.warn("é¿çç¬ Unknown protocol: {}, using default pipeline", protocolName);
            configureDefaultPipeline(pipeline, extension);
        }
    }

    /**
     * é°å¶çHTTP Pipeline
     */
    private void configureHttpPipeline(@NotNull ChannelPipeline pipeline, @NotNull ProtocolExtension extension) {
        int timeoutSeconds = 300;
        pipeline.addLast("httpRequestDecoder", new HttpRequestDecoder());
        pipeline.addLast("httpResponseEncoder", new HttpResponseEncoder());
        pipeline.addLast("httpObjectAggregator", new HttpObjectAggregator(65536));
        pipeline.addLast(new ChunkedWriteHandler());
        pipeline.addLast(new ReadTimeoutHandler(timeoutSeconds));
        pipeline.addLast(new WriteTimeoutHandler(timeoutSeconds));
    }

    /**
     * é°å¶çWebSocket Pipeline
     */
    private void configureWebSocketPipeline(@NotNull ChannelPipeline pipeline, @NotNull ProtocolExtension extension) {
        pipeline.addLast("httpCodec", new io.netty.handler.codec.http.HttpServerCodec());
        pipeline.addLast("aggregator", new HttpObjectAggregator(65536));
        pipeline.addLast("websocketHandler", new WebSocketServerProtocolHandler("/"));
    }

    /**
     * é°å¶çMQTT Pipeline
     */
    private void configureMqttPipeline(@NotNull ChannelPipeline pipeline, @NotNull ProtocolExtension extension) {
        addFrameCodecs(pipeline);
        addProtocolCodecs(pipeline, "mqtt");
    }

    /**
     * é°å¶çTCP Pipeline
     */
    private void configureTcpPipeline(@NotNull ChannelPipeline pipeline, @NotNull ProtocolExtension extension) {
        addFrameCodecs(pipeline);
        addProtocolCodecs(pipeline, "tcp");
    }

    /**
     * é°å¶çCustom Pipeline
     */
    private void configureCustomPipeline(@NotNull ChannelPipeline pipeline, @NotNull ProtocolExtension extension) {
        addFrameCodecs(pipeline);
        addProtocolCodecs(pipeline, "custom");
    }

    /**
     * é°å¶çæ¦æ¨¿î»Pipeline
     */
    private void configureDefaultPipeline(@NotNull ChannelPipeline pipeline, @Nullable ProtocolExtension extension) {
        addFrameCodecs(pipeline);
        if (extension != null) {
            addProtocolCodecs(pipeline, extension.getProtocolName().toLowerCase());
        } else {
            addProtocolCodecs(pipeline, "simple");
        }
    }

    /**
     * å¨£è¯²å§ç¯Ñç´ªçï½çé£
     */
    private void addFrameCodecs(@NotNull ChannelPipeline pipeline) {
        pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(MAX_FRAME_LENGTH, 0, 4, 0, 4));
        pipeline.addLast("frameEncoder", new LengthFieldPrepender(4));
    }

    /**
     * å¨£è¯²å§éå¿îç¼æ ¬Ðé®ä½¸æ«
     */
    private void addProtocolCodecs(@NotNull ChannelPipeline pipeline, @NotNull String protocolName) {
        try {
            CodecFactory.CodecPair codecPair = codecFactory.createCodecPair(protocolName);
            pipeline.addLast(protocolName + "Decoder", codecPair.getDecoder());
            pipeline.addLast(protocolName + "Encoder", codecPair.getEncoder());
        } catch (Exception e) {
            log.warn("Failed to create {} codec, using simple codec", protocolName);
            try {
                CodecFactory.CodecPair codecPair = codecFactory.createCodecPair("simple");
                pipeline.addLast("decoder", codecPair.getDecoder());
                pipeline.addLast("encoder", codecPair.getEncoder());
            } catch (Exception ex) {
                log.error("Failed to create simple codec", ex);
            }
        }
    }
}
