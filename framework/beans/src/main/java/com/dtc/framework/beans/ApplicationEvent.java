package com.dtc.framework.beans;

import java.util.EventObject;

/**
 * 应用事件基类
 * 借鉴 Spring ApplicationEvent 的设计
 * 
 * @author Network Service Template
 */
public abstract class ApplicationEvent extends EventObject {
    
    private final long timestamp;
    
    /**
     * 构造函数
     * 
     * @param source 事件源
     */
    public ApplicationEvent(Object source) {
        super(source);
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * 获取时间戳
     * 
     * @return 时间戳
     */
    public final long getTimestamp() {
        return this.timestamp;
    }
}
