package com.dtc.ioc.core;

/**
 * 上下文刷新事件
 * 当应用上下文刷新完成时发布
 * 借鉴Spring ContextRefreshedEvent的设计
 * 
 * @author Network Service Template
 */
public class ContextRefreshedEvent extends ApplicationEvent {
    
    /**
     * 构造函数
     * 
     * @param source 应用上下文
     */
    public ContextRefreshedEvent(NetworkApplicationContext source) {
        super(source);
    }
    
    /**
     * 获取应用上下文
     * 
     * @return 应用上下文
     */
    public NetworkApplicationContext getApplicationContext() {
        return (NetworkApplicationContext) getSource();
    }
}
