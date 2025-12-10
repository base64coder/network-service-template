package com.dtc.framework.beans.compatibility;

import com.dtc.framework.beans.binder.Binder;
import com.dtc.framework.beans.impl.binder.DefaultBinder;
import com.dtc.framework.beans.NetworkApplicationContext;

/**
 * AbstractModule compatibility class for module-based configuration.
 * Provides a convenient base class for implementing IoC modules.
 */
public abstract class AbstractModule implements com.dtc.framework.beans.IoCModule {

    protected Binder binder;

    @Override
    public void configure(NetworkApplicationContext context) {
        this.binder = new DefaultBinder(context);
        configure();
    }

    protected abstract void configure();

    protected <T> com.dtc.framework.beans.binder.LinkedBindingBuilder<T> bind(Class<T> type) {
        return binder.bind(type);
    }

    protected void install(com.dtc.framework.beans.IoCModule module) {
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

