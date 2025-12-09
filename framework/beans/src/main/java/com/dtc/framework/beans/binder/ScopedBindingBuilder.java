package com.dtc.framework.beans.binder;

import com.dtc.framework.beans.BeanScope;

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

