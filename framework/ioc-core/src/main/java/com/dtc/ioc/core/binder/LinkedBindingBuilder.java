package com.dtc.ioc.core.binder;

import com.dtc.ioc.core.BeanScope;
import com.dtc.ioc.core.provider.Provider;

public interface LinkedBindingBuilder<T> extends ScopedBindingBuilder {
    
    /**
     * Binds to a specific implementation class.
     */
    ScopedBindingBuilder to(Class<? extends T> implementation);
    
    /**
     * Binds to a specific instance.
     */
    void toInstance(T instance);
    
    /**
     * Binds to a provider.
     */
    ScopedBindingBuilder toProvider(Provider<? extends T> provider);
    
    /**
     * Binds to a provider class.
     */
    ScopedBindingBuilder toProvider(Class<? extends Provider<? extends T>> providerType);
}

