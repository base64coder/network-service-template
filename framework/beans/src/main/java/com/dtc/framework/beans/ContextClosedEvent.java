package com.dtc.framework.beans;

/**
     * ä¸ä¸æå³é­äºä»¶
å½åºç¨ä¸ä¸æå³é­æ¶åå¸
åé´Spring ContextClosedEventçè®¾è®¡
@author Network Service Template
/
public class ContextClosedEvent extends ApplicationEvent {
    
    /**
     * æé å½æ°
@param source åºç¨ä¸ä¸æ
/
    public ContextClosedEvent(NetworkApplicationContext source) {
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
