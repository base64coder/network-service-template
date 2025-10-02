package com.dtc.core.bootstrap.ioc;

import com.dtc.core.messaging.MessageProcessor;
import com.google.inject.AbstractModule;

/**
 * 队列模块 绑定队列相关的服务
 * 
 * @author Network Service Template
 */
public class QueueModule extends AbstractModule {

    @Override
    protected void configure() {
        // 绑定消息处理器
        bind(MessageProcessor.class).asEagerSingleton();
    }
}
