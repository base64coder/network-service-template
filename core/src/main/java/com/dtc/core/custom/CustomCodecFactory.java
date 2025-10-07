package com.dtc.core.custom;

import com.dtc.api.annotations.NotNull;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 自定义协议编解码器工厂接口
 * 提供自定义协议的编码和解码功能
 * 
 * @author Network Service Template
 */
public abstract class CustomCodecFactory {

    private static final Logger log = LoggerFactory.getLogger(CustomCodecFactory.class);

    public CustomCodecFactory() {
        log.info("Creating Custom Codec instance");
    }

    /**
     * 解码消息
     * 
     * @param ctx 通道上下文
     * @param in  输入缓冲区
     * @param out 输出消息列表
     * @throws Exception 解码异常
     */
    public abstract void decode(@NotNull ChannelHandlerContext ctx, @NotNull ByteBuf in,
            @NotNull java.util.List<Object> out) throws Exception;

    /**
     * 编码消息
     * 
     * @param ctx 通道上下文
     * @param msg 要编码的消息
     * @param out 输出缓冲区
     * @throws Exception 编码异常
     */
    public abstract void encode(@NotNull ChannelHandlerContext ctx, @NotNull Object msg, @NotNull ByteBuf out)
            throws Exception;

    /**
     * 获取编解码器名称
     */
    @NotNull
    public abstract String getCodecName();

    /**
     * 获取编解码器版本
     */
    @NotNull
    public abstract String getCodecVersion();

    /**
     * 检查是否支持该消息类型
     */
    public abstract boolean supports(@NotNull Class<?> messageType);
}
