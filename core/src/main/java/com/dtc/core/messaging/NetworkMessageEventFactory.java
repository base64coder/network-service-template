package com.dtc.core.messaging;

import com.lmax.disruptor.EventFactory;

 // * 缃戠粶娑堟伅浜嬩欢宸ュ巶
 // * 鐢ㄤ簬Disruptor鍒涘缓NetworkMessageEvent瀹炰緥
public class NetworkMessageEventFactory implements EventFactory<NetworkMessageEvent> {

    @Override
    public NetworkMessageEvent newInstance() {
        return new NetworkMessageEvent();
    }
}
