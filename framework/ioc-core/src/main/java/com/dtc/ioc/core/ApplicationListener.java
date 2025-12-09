package com.dtc.ioc.core;

import java.util.EventListener;

/**
     * åºç¨çå¬å¨æ¥å£
çå¬åºç¨äºä»¶
åé´Spring ApplicationListenerçè®¾è®¡
@author Network Service Template
/
public interface ApplicationListener<E extends ApplicationEvent> extends EventListener {
    
    /**
     * å¤çåºç¨äºä»¶
@param event åºç¨äºä»¶
/
    void onApplicationEvent(E event);
}
