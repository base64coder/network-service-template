package com.dtc.framework.beans.impl;

import com.dtc.framework.beans.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DefaultBeanFactoryTest {

    private DefaultBeanFactory factory;

    @BeforeEach
    void setUp() {
        BeanContainer container = new DefaultBeanContainer();
        DependencyInjector injector = new DefaultDependencyInjector(container);
        factory = new DefaultBeanFactory(container, injector);
    }

    @Test
    void testGetBean() {
        DefaultBeanDefinition definition = new DefaultBeanDefinition("testBean", String.class);
        factory.registerBeanDefinition("testBean", definition);
        factory.registerSingleton("testBean", "testValue");
        
        Object bean = factory.getBean("testBean");
        assertEquals("testValue", bean);
    }

    @Test
    void testGetBeanByType() {
        DefaultBeanDefinition definition = new DefaultBeanDefinition("testBean", String.class);
        factory.registerBeanDefinition("testBean", definition);
        factory.registerSingleton("testBean", "testValue");
        
        String bean = factory.getBean(String.class);
        assertEquals("testValue", bean);
    }

    @Test
    void testGetBeanThrowsExceptionWhenNotFound() {
        assertThrows(Exception.class, () -> factory.getBean("nonExistent"));
    }
}

