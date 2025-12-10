package com.dtc.framework.beans.binder;

import com.dtc.framework.beans.BeanScope;
import com.dtc.framework.beans.provider.Provider;

/**
 * Binder interface for configuring bindings in modules.
 * Binder interface for module-based dependency injection configuration.
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

