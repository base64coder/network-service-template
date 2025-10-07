package com.dtc.core.bootstrap.ioc;

import com.dtc.core.messaging.NetworkMessageQueue;
import com.dtc.core.messaging.NetworkMessageConsumer;
import com.dtc.core.messaging.NetworkMessageEventFactory;
import com.google.inject.AbstractModule;

/**
 * 队列模块 绑定队列相关的服务
 * 
 * @author Network Service Template
 */
public class QueueModule extends AbstractModule {

    @Override
    protected void configure() {
        // 绑定网络消息队列相关组件
        bind(NetworkMessageEventFactory.class).asEagerSingleton();
        bind(NetworkMessageConsumer.class).asEagerSingleton();
        bind(NetworkMessageQueue.class).asEagerSingleton();
    }
}
