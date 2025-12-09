package com.dtc.framework.beans;

import com.dtc.framework.beans.impl.DefaultBeanDefinition;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BeanDefinitionTest {

    @Test
    void testDefaultBeanDefinition() {
        DefaultBeanDefinition definition = new DefaultBeanDefinition("testBean", String.class);
        
        assertEquals("testBean", definition.getBeanName());
        assertEquals(String.class, definition.getBeanClass());
        assertEquals(BeanScope.SINGLETON, definition.getScope());
        assertTrue(definition.isSingleton());
        assertFalse(definition.isPrototype());
    }

    @Test
    void testPrototypeScope() {
        DefaultBeanDefinition definition = new DefaultBeanDefinition("testBean", String.class);
        definition.setScope(BeanScope.PROTOTYPE);
        
        assertFalse(definition.isSingleton());
        assertTrue(definition.isPrototype());
    }

    @Test
    void testLazyInit() {
        DefaultBeanDefinition definition = new DefaultBeanDefinition("testBean", String.class);
        definition.setLazyInit(true);
        
        assertTrue(definition.isLazyInit());
    }

    @Test
    void testInitAndDestroyMethods() {
        DefaultBeanDefinition definition = new DefaultBeanDefinition("testBean", TestBean.class);
        definition.setInitMethodName("init");
        definition.setDestroyMethodName("destroy");
        
        assertEquals("init", definition.getInitMethodName());
        assertEquals("destroy", definition.getDestroyMethodName());
    }

    static class TestBean {
        void init() {}
        void destroy() {}
    }
}

