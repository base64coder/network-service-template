package com.dtc.core.bootstrap.ioc.lazysingleton;

import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 延迟加载单例作用域实现
 * 
 * @author Network Service Template
 */
public class LazySingletonScopeImpl implements Scope {

    private static final Logger log = LoggerFactory.getLogger(LazySingletonScopeImpl.class);
    private final ConcurrentMap<Key<?>, Object> instances = new ConcurrentHashMap<>();

    @Override
    public <T> Provider<T> scope(Key<T> key, Provider<T> unscoped) {
        return new LazySingletonProvider<>(key, unscoped);
    }

    private class LazySingletonProvider<T> implements Provider<T> {
        private final Key<T> key;
        private final Provider<T> unscoped;

        public LazySingletonProvider(Key<T> key, Provider<T> unscoped) {
            this.key = key;
            this.unscoped = unscoped;
        }

        @Override
        public T get() {
            @SuppressWarnings("unchecked")
            T instance = (T) instances.get(key);
            if (instance == null) {
                synchronized (instances) {
                    @SuppressWarnings("unchecked")
                    T doubleChecked = (T) instances.get(key);
                    if (doubleChecked == null) {
                        log.trace("Creating lazy singleton instance for {}", key);
                        instance = unscoped.get();
                        instances.put(key, instance);
                    } else {
                        instance = doubleChecked;
                    }
                }
            }
            return instance;
        }
    }

    @Override
    public String toString() {
        return "LazySingleton";
    }
}
