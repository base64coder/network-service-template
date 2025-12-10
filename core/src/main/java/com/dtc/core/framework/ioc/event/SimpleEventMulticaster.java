package com.dtc.core.framework.ioc.event;

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
            // 简单实现：不进行类型检查，直接广播。实际需要判断泛型。
            try {
                // 暂时全部广播，待完善泛型匹配
                listener.onApplicationEvent(event);
            } catch (ClassCastException e) {
                // Ignore mismatch
            }
        }
    }
}

