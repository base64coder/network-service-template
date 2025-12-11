package com.dtc.framework.persistent.transaction.support;

import java.util.HashMap;
import java.util.Map;

public abstract class TransactionSynchronizationManager {
    private static final ThreadLocal<Map<Object, Object>> resources = new ThreadLocal<>();

    public static Object getResource(Object key) {
        Map<Object, Object> map = resources.get();
        if (map == null) {
            return null;
        }
        return map.get(key);
    }

    public static void bindResource(Object key, Object value) {
        Map<Object, Object> map = resources.get();
        if (map == null) {
            map = new HashMap<>();
            resources.set(map);
        }
        map.put(key, value);
    }

    public static Object unbindResource(Object key) {
        Map<Object, Object> map = resources.get();
        if (map == null) {
            return null;
        }
        Object value = map.remove(key);
        if (map.isEmpty()) {
            resources.remove();
        }
        return value;
    }
    
    public static boolean hasResource(Object key) {
        Map<Object, Object> map = resources.get();
        return map != null && map.containsKey(key);
    }
}

