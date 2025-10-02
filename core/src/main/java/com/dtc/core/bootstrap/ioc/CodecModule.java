package com.dtc.core.bootstrap.ioc;

import com.dtc.core.netty.codec.CodecFactory;
import com.dtc.core.netty.codec.ProtobufDecoder;
import com.dtc.core.netty.codec.ProtobufEncoder;
import com.dtc.core.netty.codec.SimpleMessageDecoder;
import com.dtc.core.netty.codec.SimpleMessageEncoder;
import com.google.inject.AbstractModule;

/**
 * 编解码器模块 绑定编解码器相关的服务
 * 
 * @author Network Service Template
 */
public class CodecModule extends AbstractModule {

    @Override
    protected void configure() {
        // 绑定编解码器工厂
        bind(CodecFactory.class).asEagerSingleton();

        // 绑定具体的编解码器
        bind(ProtobufDecoder.class).asEagerSingleton();
        bind(ProtobufEncoder.class).asEagerSingleton();
        bind(SimpleMessageDecoder.class).asEagerSingleton();
        bind(SimpleMessageEncoder.class).asEagerSingleton();
    }
}
