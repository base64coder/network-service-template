package com.dtc.ioc.core.impl;

import com.dtc.api.annotations.NotNull;
import com.dtc.ioc.core.ApplicationEvent;
import com.dtc.ioc.core.ApplicationEventMulticaster;
import com.dtc.ioc.core.ApplicationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 简单应用事件多播器实现
 * 借鉴Spring SimpleApplicationEventMulticaster的设计
 * 
 * @author Network Service Template
 */
public class SimpleApplicationEventMulticaster implements ApplicationEventMulticaster {
    
    private static final Logger log = LoggerFactory.getLogger(SimpleApplicationEventMulticaster.class);
    
    // 应用监听器列表
    private final List<ApplicationListener<?>> applicationListeners = new CopyOnWriteArrayList<>();
    
    @Override
    public void addApplicationListener(@NotNull ApplicationListener<?> listener) {
        if (listener != null) {
            applicationListeners.add(listener);
            log.debug("🔧 Application listener added: {}", listener.getClass().getSimpleName());
        }
    }
    
    @Override
    public void removeApplicationListener(@NotNull ApplicationListener<?> listener) {
        if (listener != null) {
            applicationListeners.remove(listener);
            log.debug("🔧 Application listener removed: {}", listener.getClass().getSimpleName());
        }
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public void multicastEvent(@NotNull ApplicationEvent event) {
        log.debug("📢 Multicasting event: {}", event.getClass().getSimpleName());
        
        for (ApplicationListener listener : applicationListeners) {
            try {
                if (supportsEvent(listener, event)) {
                    listener.onApplicationEvent(event);
                }
            } catch (Exception e) {
                log.error("❌ Error in application listener: {}", listener.getClass().getSimpleName(), e);
            }
        }
    }
    
    /**
     * 检查监听器是否支持该事件
     */
    @SuppressWarnings("unchecked")
    private boolean supportsEvent(ApplicationListener listener, ApplicationEvent event) {
        try {
            // 通过反射检查监听器是否支持该事件类型
            Class<?> listenerType = listener.getClass();
            java.lang.reflect.Type[] genericInterfaces = listenerType.getGenericInterfaces();
            
            for (java.lang.reflect.Type genericInterface : genericInterfaces) {
                if (genericInterface instanceof java.lang.reflect.ParameterizedType) {
                    java.lang.reflect.ParameterizedType parameterizedType = (java.lang.reflect.ParameterizedType) genericInterface;
                    java.lang.reflect.Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                    
                    if (actualTypeArguments.length > 0) {
                        Class<?> eventType = (Class<?>) actualTypeArguments[0];
                        if (eventType.isAssignableFrom(event.getClass())) {
                            return true;
                        }
                    }
                }
            }
            
            // 如果没有泛型信息，默认支持所有事件
            return true;
            
        } catch (Exception e) {
            log.debug("Error checking event support for listener: {}", listener.getClass().getSimpleName(), e);
            return true;
        }
    }
    
    @Override
    public void removeAllListeners() {
        applicationListeners.clear();
        log.debug("🗑️ Removed all application listeners");
    }
}
