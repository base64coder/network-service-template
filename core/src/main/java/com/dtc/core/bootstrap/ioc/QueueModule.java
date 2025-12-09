package com.dtc.core.bootstrap.ioc;

import com.dtc.core.messaging.NetworkMessageQueue;
import com.dtc.core.messaging.NetworkMessageConsumer;
import com.dtc.core.messaging.NetworkMessageEventFactory;
import com.google.inject.AbstractModule;

/**
 * 队列模块
 * 配置队列相关的依赖注入
 * 
 * @author Network Service Template
 */
public class QueueModule extends AbstractModule {

    @Override
    protected void configure() {
        // 绑定网络消息事件工厂、消费者和队列
        bind(NetworkMessageEventFactory.class).asEagerSingleton();
        bind(NetworkMessageConsumer.class).asEagerSingleton();
        bind(NetworkMessageQueue.class).asEagerSingleton();
    }
}
