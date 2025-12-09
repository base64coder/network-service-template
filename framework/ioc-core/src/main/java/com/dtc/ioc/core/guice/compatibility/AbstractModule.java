package com.dtc.ioc.core.guice.compatibility;

import com.dtc.ioc.core.binder.Binder;
import com.dtc.ioc.core.impl.binder.DefaultBinder;
import com.dtc.ioc.core.NetworkApplicationContext;

/**
 * AbstractModule compatibility class to ease migration from Guice.
 * Users can extend this class instead of com.google.inject.AbstractModule.
 */
public abstract class AbstractModule implements com.dtc.ioc.core.IoCModule {

    protected Binder binder;

    @Override
    public void configure(NetworkApplicationContext context) {
        this.binder = new DefaultBinder(context);
        configure();
    }

    protected abstract void configure();

    protected <T> com.dtc.ioc.core.binder.LinkedBindingBuilder<T> bind(Class<T> type) {
        return binder.bind(type);
    }

    protected void install(com.dtc.ioc.core.IoCModule module) {
        binder.install(module);
    }
    
    // Metadata methods default implementation
    @Override
    public String getModuleName() { return this.getClass().getSimpleName(); }
    
    @Override
    public String getModuleVersion() { return "1.0.0"; }
    
    @Override
    public String getModuleDescription() { return "Guice compatibility module"; }
    
    @Override
    public String[] getDependencies() { return new String[0]; }
}

