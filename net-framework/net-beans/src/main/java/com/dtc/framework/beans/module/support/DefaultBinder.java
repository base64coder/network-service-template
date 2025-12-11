package com.dtc.framework.beans.module.support;

import com.dtc.framework.beans.annotation.Scope;
import com.dtc.framework.beans.factory.config.BeanDefinition;
import com.dtc.framework.beans.factory.support.DefaultListableBeanFactory;
import com.dtc.framework.beans.module.AnnotatedBindingBuilder;
import com.dtc.framework.beans.module.Binder;
import com.dtc.framework.beans.module.Module;
import com.dtc.framework.beans.module.ScopedBindingBuilder;

import java.lang.annotation.Annotation;

public class DefaultBinder implements Binder {
    private final DefaultListableBeanFactory beanFactory;

    public DefaultBinder(DefaultListableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Override
    public <T> AnnotatedBindingBuilder<T> bind(Class<T> type) {
        return new DefaultBindingBuilder<>(type);
    }

    @Override
    public void install(Module module) {
        module.configure(this);
    }

    private class DefaultBindingBuilder<T> implements AnnotatedBindingBuilder<T> {
        private final Class<T> type;
        private Class<? extends T> implementation;
        private String scope = "singleton"; 
        private boolean eager = false;

        public DefaultBindingBuilder(Class<T> type) {
            this.type = type;
            this.implementation = type;
            registerBean(); // Register default binding (self)
        }

        @Override
        public ScopedBindingBuilder to(Class<? extends T> implementation) {
            this.implementation = implementation;
            registerBean(); // Update binding
            return this;
        }

        @Override
        public void toInstance(T instance) {
            // Inject dependencies into instance
            Object autowiredInstance = beanFactory.autowireBean(instance);
            // Register as singleton with the type name
            beanFactory.registerSingleton(type.getName(), autowiredInstance);
        }

        @Override
        public void asEagerSingleton() {
            this.scope = "singleton";
            this.eager = true;
            updateBeanDefinition();
        }

        @Override
        public void in(Class<? extends Annotation> scopeAnnotation) {
            // Simple mapping for standard scopes
            String name = scopeAnnotation.getSimpleName();
            if ("Singleton".equalsIgnoreCase(name)) {
                this.scope = "singleton";
            } else if ("Prototype".equalsIgnoreCase(name)) {
                this.scope = "prototype";
            }
            updateBeanDefinition();
        }

        @Override
        public void in(Scope scope) {
            this.scope = scope.value();
            updateBeanDefinition();
        }
        
        private void registerBean() {
            BeanDefinition bd = new BeanDefinition();
            bd.setBeanClass(implementation);
            bd.setScope(scope);
            bd.setLazyInit(!eager);
            // Use fully qualified name of the *Interface* (or Type) as the bean name
            // This ensures getBean(Interface.class) works via name lookup or type scan
            beanFactory.registerBeanDefinition(type.getName(), bd);
        }
        
        private void updateBeanDefinition() {
            BeanDefinition bd = beanFactory.getBeanDefinition(type.getName());
            if (bd == null) {
                registerBean();
                return;
            }
            bd.setScope(scope);
            bd.setLazyInit(!eager);
        }
    }
}

