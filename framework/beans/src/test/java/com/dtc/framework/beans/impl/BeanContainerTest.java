package com.dtc.framework.beans.impl;

import com.dtc.framework.beans.BeanDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BeanContainerTest {

    private DefaultBeanContainer container;

    @BeforeEach
    void setUp() {
        container = new DefaultBeanContainer();
    }

    @Test
    void testRegisterAndGetBean() {
        container.registerBean("testBean", "testValue");
        
        Object bean = container.getBean("testBean");
        assertNotNull(bean);
        assertEquals("testValue", bean);
    }

    @Test
    void testContainsBean() {
        container.registerBean("testBean", "testValue");
        
        assertTrue(container.containsBean("testBean"));
        assertFalse(container.containsBean("nonExistent"));
    }

    @Test
    void testRemoveBean() {
        container.registerBean("testBean", "testValue");
        container.removeBean("testBean");
        
        assertFalse(container.containsBean("testBean"));
    }

    @Test
    void testClear() {
        container.registerBean("bean1", "value1");
        container.registerBean("bean2", "value2");
        container.clear();
        
        assertFalse(container.containsBean("bean1"));
        assertFalse(container.containsBean("bean2"));
    }

    @Test
    void testGetBeanNames() {
        container.registerBean("bean1", "value1");
        container.registerBean("bean2", "value2");
        
        String[] names = container.getBeanNames();
        assertEquals(2, names.length);
    }
}

