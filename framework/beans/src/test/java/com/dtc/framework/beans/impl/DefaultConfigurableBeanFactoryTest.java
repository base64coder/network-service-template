package com.dtc.framework.beans.impl;

import com.dtc.framework.beans.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DefaultConfigurableBeanFactoryTest {

    private DefaultConfigurableBeanFactory factory;

    @BeforeEach
    void setUp() {
        factory = new DefaultConfigurableBeanFactory();
    }

    @Test
    void testRegisterBeanDefinition() {
        DefaultBeanDefinition definition = new DefaultBeanDefinition("testBean", String.class);
        factory.registerBeanDefinition("testBean", definition);
        
        assertTrue(factory.containsBean("testBean"));
    }

    @Test
    void testPreInstantiateSingletons() {
        DefaultBeanDefinition definition = new DefaultBeanDefinition("testBean", String.class);
        definition.setScope(BeanScope.SINGLETON);
        factory.registerBeanDefinition("testBean", definition);
        factory.registerSingleton("testBean", "testValue");
        
        assertDoesNotThrow(() -> factory.preInstantiateSingletons());
    }

    @Test
    void testDestroySingletons() {
        factory.registerSingleton("testBean", "testValue");
        factory.destroySingletons();
        
        // After destruction, bean should still exist in definition but instance cleared
        assertTrue(factory.containsBean("testBean"));
    }

    @Test
    void testAddBeanPostProcessor() {
        BeanPostProcessor processor = new BeanPostProcessor() {
            @Override
            public Object postProcessBeforeInitialization(Object bean, String beanName) {
                return bean;
            }

            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) {
                return bean;
            }
        };
        
        factory.addBeanPostProcessor(processor);
        // Should not throw exception
        assertDoesNotThrow(() -> factory.getBean("testBean"));
    }

    @Test
    void testSetBeanClassLoader() {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        factory.setBeanClassLoader(loader);
        
        assertEquals(loader, factory.getBeanClassLoader());
    }
}

