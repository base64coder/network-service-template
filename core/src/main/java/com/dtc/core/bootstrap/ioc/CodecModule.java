package com.dtc.core.bootstrap.ioc;

import com.dtc.core.network.netty.codec.CodecFactory;
import com.dtc.core.network.netty.codec.ProtobufDecoder;
import com.dtc.core.network.netty.codec.ProtobufEncoder;
import com.dtc.core.network.netty.codec.SimpleMessageDecoder;
import com.dtc.core.network.netty.codec.SimpleMessageEncoder;
import com.google.inject.AbstractModule;

/**
 * 编解码器模块
 * 配置所有编解码器相关的依赖注入
 * 
 * @author Network Service Template
 */
public class CodecModule extends AbstractModule {

    @Override
    protected void configure() {
        // 绑定编解码器工厂
        bind(CodecFactory.class).asEagerSingleton();

        // 绑定 Protobuf 编解码器
        bind(ProtobufDecoder.class).asEagerSingleton();
        bind(ProtobufEncoder.class).asEagerSingleton();
        bind(SimpleMessageDecoder.class).asEagerSingleton();
        bind(SimpleMessageEncoder.class).asEagerSingleton();
    }
}
