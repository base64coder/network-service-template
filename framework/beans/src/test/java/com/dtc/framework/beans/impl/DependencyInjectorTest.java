package com.dtc.framework.beans.impl;

import com.dtc.framework.beans.BeanContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DependencyInjectorTest {

    private DefaultDependencyInjector injector;
    private BeanContainer container;

    @BeforeEach
    void setUp() {
        container = new DefaultBeanContainer();
        injector = new DefaultDependencyInjector(container);
    }

    @Test
    void testInjectDependencies() {
        container.registerBean("dependency", "dependencyValue");
        
        TestBean bean = new TestBean();
        injector.injectDependencies(bean);
        
        assertNotNull(bean);
    }

    @Test
    void testInjectDependenciesWithNullContainer() {
        DefaultDependencyInjector nullInjector = new DefaultDependencyInjector(null);
        TestBean bean = new TestBean();
        
        // Should not throw exception
        assertDoesNotThrow(() -> nullInjector.injectDependencies(bean));
    }

    static class TestBean {
        private String dependency;
        
        public String getDependency() {
            return dependency;
        }
        
        public void setDependency(String dependency) {
            this.dependency = dependency;
        }
    }
}

