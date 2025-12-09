package com.dtc.framework.beans;

import com.dtc.framework.beans.impl.DefaultBeanFactory;
import com.dtc.framework.beans.impl.DefaultBeanContainer;
import com.dtc.framework.beans.impl.DefaultDependencyInjector;
import com.dtc.framework.beans.impl.DefaultBeanDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BeanFactoryTest {

    private BeanFactory beanFactory;

    @BeforeEach
    void setUp() {
        BeanContainer container = new DefaultBeanContainer();
        DependencyInjector injector = new DefaultDependencyInjector(container);
        beanFactory = new DefaultBeanFactory(container, injector);
    }

    @Test
    void testRegisterAndGetBean() {
        DefaultBeanDefinition definition = new DefaultBeanDefinition("testBean", String.class);
        beanFactory.registerBeanDefinition("testBean", definition);
        beanFactory.registerSingleton("testBean", "testValue");

        Object bean = beanFactory.getBean("testBean");
        assertNotNull(bean);
        assertEquals("testValue", bean);
    }

    @Test
    void testGetBeanByType() {
        DefaultBeanDefinition definition = new DefaultBeanDefinition("testBean", String.class);
        beanFactory.registerBeanDefinition("testBean", definition);
        beanFactory.registerSingleton("testBean", "testValue");

        String bean = beanFactory.getBean(String.class);
        assertNotNull(bean);
        assertEquals("testValue", bean);
    }

    @Test
    void testContainsBean() {
        DefaultBeanDefinition definition = new DefaultBeanDefinition("testBean", String.class);
        beanFactory.registerBeanDefinition("testBean", definition);
        
        assertTrue(beanFactory.containsBean("testBean"));
        assertFalse(beanFactory.containsBean("nonExistent"));
    }

    @Test
    void testIsSingleton() {
        DefaultBeanDefinition definition = new DefaultBeanDefinition("testBean", String.class);
        definition.setScope(BeanScope.SINGLETON);
        beanFactory.registerBeanDefinition("testBean", definition);
        beanFactory.registerSingleton("testBean", "testValue");

        assertTrue(beanFactory.isSingleton("testBean"));
    }

    @Test
    void testGetType() {
        DefaultBeanDefinition definition = new DefaultBeanDefinition("testBean", String.class);
        beanFactory.registerBeanDefinition("testBean", definition);

        Class<?> type = beanFactory.getType("testBean");
        assertEquals(String.class, type);
    }
}

