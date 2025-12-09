package com.dtc.ioc.core;

/**
     * ä¸ä¸æå·æ°äºä»¶
å½åºç¨ä¸ä¸æå·æ°å®ææ¶åå¸
åé´Spring ContextRefreshedEventçè®¾è®¡
@author Network Service Template
/
public class ContextRefreshedEvent extends ApplicationEvent {
    
    /**
     * æé å½æ°
@param source åºç¨ä¸ä¸æ
/
    public ContextRefreshedEvent(NetworkApplicationContext source) {
        super(source);
    }
    
    /**
     * è·ååºç¨ä¸ä¸æ
@return åºç¨ä¸ä¸æ
/
    public NetworkApplicationContext getApplicationContext() {
        return (NetworkApplicationContext) getSource();
    }
}
