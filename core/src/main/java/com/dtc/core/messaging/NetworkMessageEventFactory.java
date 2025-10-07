package com.dtc.core.messaging;

import com.lmax.disruptor.EventFactory;

/**
 * 网络消息事件工厂
 * 用于Disruptor创建NetworkMessageEvent实例
 * 
 * @author Network Service Template
 */
public class NetworkMessageEventFactory implements EventFactory<NetworkMessageEvent> {

    @Override
    public NetworkMessageEvent newInstance() {
        return new NetworkMessageEvent();
    }
}
