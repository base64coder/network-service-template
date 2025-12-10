package com.dtc.ioc.core.compatibility;

import com.dtc.ioc.core.binder.Binder;
import com.dtc.ioc.core.impl.binder.DefaultBinder;
import com.dtc.ioc.core.NetApplicationContext;

/**
 * AbstractModule compatibility class for module-based configuration.
 * Provides a convenient base class for implementing network modules.
 */
public abstract class AbstractModule implements com.dtc.ioc.core.NetModule {

    protected Binder binder;

    @Override
    public void configure(NetApplicationContext context) {
        this.binder = new DefaultBinder(context);
        configure();
    }

    protected abstract void configure();

    protected <T> com.dtc.ioc.core.binder.LinkedBindingBuilder<T> bind(Class<T> type) {
        return binder.bind(type);
    }

    protected void install(com.dtc.ioc.core.NetModule module) {
        binder.install(module);
    }
    
    // Metadata methods default implementation
    @Override
    public String getModuleName() { return this.getClass().getSimpleName(); }
    
    @Override
    public String getModuleVersion() { return "1.0.0"; }
    
    @Override
    public String getModuleDescription() { return "Compatibility module"; }
    
    @Override
    public String[] getDependencies() { return new String[0]; }
}

