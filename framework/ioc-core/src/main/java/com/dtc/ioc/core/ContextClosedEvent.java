package com.dtc.ioc.core;

/**
 * 上下文关闭事件
 * 当应用上下文关闭时发布
 * 借鉴Spring ContextClosedEvent的设计
 * 
 * @author Network Service Template
 */
public class ContextClosedEvent extends ApplicationEvent {
    
    /**
     * 构造函数
     * @param source 应用上下文
     */
    public ContextClosedEvent(NetApplicationContext source) {
        super(source);
    }
    
    /**
     * 获取应用上下文
     * @return 应用上下文
     */
    public NetApplicationContext getApplicationContext() {
        return (NetApplicationContext) getSource();
    }
}
