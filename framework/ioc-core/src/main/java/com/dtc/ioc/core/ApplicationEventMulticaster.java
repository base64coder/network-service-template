package com.dtc.ioc.core;

import com.dtc.api.annotations.NotNull;

/**
 * 应用事件多播器接口
 * 广播应用事件
 * 借鉴Spring ApplicationEventMulticaster的设计
 * 
 * @author Network Service Template
 */
public interface ApplicationEventMulticaster {
    
    /**
     * 添加应用监听器
     * 
     * @param listener 应用监听器
     */
    void addApplicationListener(@NotNull ApplicationListener<?> listener);
    
    /**
     * 移除应用监听器
     * 
     * @param listener 应用监听器
     */
    void removeApplicationListener(@NotNull ApplicationListener<?> listener);
    
    /**
     * 广播应用事件
     * 
     * @param event 应用事件
     */
    void multicastEvent(@NotNull ApplicationEvent event);
    
    /**
     * 移除所有监听器
     */
    void removeAllListeners();
}
