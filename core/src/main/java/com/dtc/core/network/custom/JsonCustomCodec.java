package com.dtc.core.network.custom;

import com.dtc.api.annotations.NotNull;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Singleton;
import java.nio.charset.StandardCharsets;

/**
 * JSON 自定义协议编解码器实现
 * 基于Jackson的JSON自定义协议编解码器
 * 
 * @author Network Service Template
 */
@Singleton
public class JsonCustomCodec extends CustomCodecFactory {

    private static final Logger log = LoggerFactory.getLogger(JsonCustomCodec.class);
    private final ObjectMapper objectMapper;

    public JsonCustomCodec() {
        this.objectMapper = new ObjectMapper();
        log.info("Creating JSON Custom Codec instance");
    }

    @Override
    public void decode(@NotNull ChannelHandlerContext ctx, @NotNull ByteBuf in, @NotNull java.util.List<Object> out)
            throws Exception {
        if (in.readableBytes() < 4) {
            return; // 数据不完整，等待更多数据
        }

        // 读取消息长度（前4字节）
        int messageLength = in.readInt();
        if (in.readableBytes() < messageLength) {
            in.resetReaderIndex(); // 重置读取位置
            return; // 数据不完整，等待更多数据
        }

        // 读取消息数据
        byte[] messageBytes = new byte[messageLength];
        in.readBytes(messageBytes);
        String jsonMessage = new String(messageBytes, StandardCharsets.UTF_8);

        try {
            // 尝试解析 JSON 消息
            Object parsedMessage = objectMapper.readValue(jsonMessage, Object.class);
            log.debug("Decoded JSON message: {}", jsonMessage);
            out.add(parsedMessage);
        } catch (Exception e) {
            log.warn("Failed to parse JSON message: {}", jsonMessage, e);
            // 如果解析失败，则返回原始字符串
            out.add(jsonMessage);
        }
    }

    @Override
    public void encode(@NotNull ChannelHandlerContext ctx, @NotNull Object msg, @NotNull ByteBuf out) throws Exception {
        if (msg == null) {
            return;
        }

        String jsonMessage;
        try {
            // 尝试将对象序列化为JSON
            jsonMessage = objectMapper.writeValueAsString(msg);
        } catch (Exception e) {
            log.warn("Failed to serialize object to JSON: {}", msg, e);
            // 如果序列化失败，则转换为字符串
            jsonMessage = msg.toString();
        }

        byte[] messageBytes = jsonMessage.getBytes(StandardCharsets.UTF_8);
        int messageLength = messageBytes.length;

        // 写入消息长度（4字节）
        out.writeInt(messageLength);
        // 写入消息数据
        out.writeBytes(messageBytes);

        log.debug("Encoded JSON message: {}", jsonMessage);
    }

    @Override
    @NotNull
    public String getCodecName() {
        return "JsonCustomCodec";
    }

    @Override
    @NotNull
    public String getCodecVersion() {
        return "1.0.0";
    }

    @Override
    public boolean supports(@NotNull Class<?> messageType) {
        // 支持所有类型，都可以转换为 JSON 序列化对象
        return true;
    }
}
