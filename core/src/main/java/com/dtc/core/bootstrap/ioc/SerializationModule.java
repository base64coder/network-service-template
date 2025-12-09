package com.dtc.core.bootstrap.ioc;

import com.dtc.core.serialization.ProtobufSerializer;
import com.google.inject.AbstractModule;

/**
 * 序列化模块
 * 配置序列化相关的依赖注入
 * 
 * @author Network Service Template
 */
public class SerializationModule extends AbstractModule {

    @Override
    protected void configure() {
        // 绑定 Protobuf 序列化器
        bind(ProtobufSerializer.class).asEagerSingleton();
    }
}
