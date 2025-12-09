package com.dtc.ioc.core.binder;

import com.dtc.ioc.core.BeanScope;

public interface ScopedBindingBuilder {
    
    /**
     * Binds to a specific scope.
     */
    void in(BeanScope scope);
    
    /**
     * Binds to singleton scope.
     */
    void asEagerSingleton();
}

