package com.dtc.ioc.core;

import java.util.EventObject;

/**
     * åºç¨äºä»¶åºç±»
åé´Spring ApplicationEventçè®¾è®¡
@author Network Service Template
/
public abstract class ApplicationEvent extends EventObject {
    
    private final long timestamp;
    
    /**
     * æé å½æ°
@param source äºä»¶æº
/
    public ApplicationEvent(Object source) {
        super(source);
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * è·åæ¶é´æ³
@return æ¶é´æ³
/
    public final long getTimestamp() {
        return this.timestamp;
    }
}
