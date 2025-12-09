package com.dtc.ioc.core.binder;

import com.dtc.ioc.core.BeanScope;
import com.dtc.ioc.core.provider.Provider;

/**
 * Binder interface for configuring bindings in modules.
 * Inspired by Guice Binder.
 */
public interface Binder {
    
    /**
     * Start a binding for a specific type.
     */
    <T> LinkedBindingBuilder<T> bind(Class<T> type);
    
    /**
     * Install another module.
     */
    void install(com.dtc.ioc.core.IoCModule module);
}

