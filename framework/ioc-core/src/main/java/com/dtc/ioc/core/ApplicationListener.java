package com.dtc.ioc.core;

import java.util.EventListener;

/**
 * 应用监听器接口
 * 监听应用事件
 * 借鉴Spring ApplicationListener的设计
 * 
 * @author Network Service Template
 */
public interface ApplicationListener<E extends ApplicationEvent> extends EventListener {
    
    /**
     * 处理应用事件
     * 
     * @param event 应用事件
     */
    void onApplicationEvent(E event);
}
