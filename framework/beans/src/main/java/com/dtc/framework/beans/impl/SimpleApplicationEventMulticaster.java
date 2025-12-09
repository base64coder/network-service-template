package com.dtc.framework.beans.impl;

import com.dtc.api.annotations.NotNull;
import com.dtc.framework.beans.ApplicationEvent;
import com.dtc.framework.beans.ApplicationEventMulticaster;
import com.dtc.framework.beans.ApplicationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
     * ç®ååºç¨äºä»¶å¤æ­å¨å®ç°
åé´Spring SimpleApplicationEventMulticasterçè®¾è®¡
@author Network Service Template
/
public class SimpleApplicationEventMulticaster implements ApplicationEventMulticaster {
    
    private static final Logger log = LoggerFactory.getLogger(SimpleApplicationEventMulticaster.class);
    
    // åºç¨çå¬å¨åè¡¨
    private final List<ApplicationListener<?>> applicationListeners = new CopyOnWriteArrayList<>();
    
    @Override
    public void addApplicationListener(@NotNull ApplicationListener<?> listener) {
        if (listener != null) {
            applicationListeners.add(listener);
            log.debug("ð§ Application listener added: {}", listener.getClass().getSimpleName());
        }
    }
    
    @Override
    public void removeApplicationListener(@NotNull ApplicationListener<?> listener) {
        if (listener != null) {
            applicationListeners.remove(listener);
            log.debug("ð§ Application listener removed: {}", listener.getClass().getSimpleName());
        }
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public void multicastEvent(@NotNull ApplicationEvent event) {
        log.debug("ð¢ Multicasting event: {}", event.getClass().getSimpleName());
        
        for (ApplicationListener listener : applicationListeners) {
            try {
                if (supportsEvent(listener, event)) {
                    listener.onApplicationEvent(event);
                }
            } catch (Exception e) {
                log.error("â Error in application listener: {}", listener.getClass().getSimpleName(), e);
            }
        }
    }
    
    /**
     * æ£æ¥çå¬å¨æ¯å¦æ¯æè¯¥äºä»¶
/
    @SuppressWarnings("unchecked")
    private boolean supportsEvent(ApplicationListener listener, ApplicationEvent event) {
        try {
            // éè¿åå°æ£æ¥çå¬å¨æ¯å¦æ¯æè¯¥äºä»¶ç±»å
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
            
            // å¦ææ²¡ææ³åä¿¡æ¯ï¼é»è®¤æ¯æææäºä»¶
            return true;
            
        } catch (Exception e) {
            log.debug("Error checking event support for listener: {}", listener.getClass().getSimpleName(), e);
            return true;
        }
    }
    
    @Override
    public void removeAllListeners() {
        applicationListeners.clear();
        log.debug("ðï¸ Removed all application listeners");
    }
}
