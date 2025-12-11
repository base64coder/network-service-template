package com.dtc.framework.context.event;

import java.util.ArrayList;
import java.util.List;

public class SimpleEventMulticaster {
    private final List<ApplicationListener<?>> listeners = new ArrayList<>();

    public void addListener(ApplicationListener<?> listener) {
        listeners.add(listener);
    }

    public void removeListener(ApplicationListener<?> listener) {
        listeners.remove(listener);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void multicastEvent(ApplicationEvent event) {
        for (ApplicationListener listener : listeners) {
            try {
                // 简单实现：不进行类型检查，直接广播
                listener.onApplicationEvent(event);
            } catch (ClassCastException e) {
                // Ignore mismatch
            }
        }
    }
    
    public void removeAllListeners() {
        listeners.clear();
    }
}

