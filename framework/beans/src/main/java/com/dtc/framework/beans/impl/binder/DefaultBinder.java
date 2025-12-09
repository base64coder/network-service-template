package com.dtc.framework.beans.impl.binder;

import com.dtc.framework.beans.BeanDefinition;
import com.dtc.framework.beans.BeanScope;
import com.dtc.framework.beans.NetworkApplicationContext;
import com.dtc.framework.beans.binder.Binder;
import com.dtc.framework.beans.binder.LinkedBindingBuilder;
import com.dtc.framework.beans.binder.ScopedBindingBuilder;
import com.dtc.framework.beans.impl.DefaultBeanDefinition;
import com.dtc.framework.beans.provider.Provider;

public class DefaultBinder implements Binder {

    private final NetworkApplicationContext context;

    public DefaultBinder(NetworkApplicationContext context) {
        this.context = context;
    }

    @Override
    public <T> LinkedBindingBuilder<T> bind(Class<T> type) {
        return new BindingBuilderImpl<>(type, context);
    }

    @Override
    public void install(com.dtc.framework.beans.IoCModule module) {
        module.configure(context);
    }

    private static class BindingBuilderImpl<T> implements LinkedBindingBuilder<T> {
        private final Class<T> type;
        private final NetworkApplicationContext context;
        private final DefaultBeanDefinition definition;

        public BindingBuilderImpl(Class<T> type, NetworkApplicationContext context) {
            this.type = type;
            this.context = context;
            this.definition = new DefaultBeanDefinition(type);
            // Default to singleton if not specified? Or prototype? 
            // Guice defaults to "no scope" (prototype-like), Spring defaults to Singleton.
            // Let's stick to our framework default (Singleton usually).
            this.definition.setScope(BeanScope.SINGLETON);
        }

        @Override
        public ScopedBindingBuilder to(Class<? extends T> implementation) {
            // In a real implementation, we would update the BeanDefinition to point to the impl class
            // but keep the bean name/key associated with the interface 'type'.
            // For now, simplistically assuming we register the impl.
            // context.registerBean(implementation); 
            // TODO: Enhanced registration handling mapping interface to impl.
            return this;
        }

        @Override
        public void toInstance(T instance) {
            context.registerSingleton(instance);
        }

        @Override
        public ScopedBindingBuilder toProvider(Provider<? extends T> provider) {
            // TODO: Implement provider-based registration
            return this;
        }

        @Override
        public ScopedBindingBuilder toProvider(Class<? extends Provider<? extends T>> providerType) {
            // TODO: Implement provider-class-based registration
            return this;
        }

        @Override
        public void in(BeanScope scope) {
            definition.setScope(scope);
        }

        @Override
        public void asEagerSingleton() {
            definition.setScope(BeanScope.SINGLETON);
            definition.setLazyInit(false);
        }
    }
}

