package com.dtc.framework.beans.context;

import com.dtc.framework.beans.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AnnotationConfigApplicationContextTest {

    @Test
    void testContextCreation() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        assertNotNull(context);
    }

    @Test
    void testRegisterAndGetBean() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.registerBean("testBean", String.class);
        context.refresh();

        Object bean = context.getBean("testBean");
        assertNotNull(bean);
        assertEquals(String.class, bean.getClass());
    }

    @Test
    void testGetBeanByType() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.registerBean("testBean", TestService.class);
        context.refresh();

        TestService service = context.getBean(TestService.class);
        assertNotNull(service);
    }

    @Test
    void testContextIsActive() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        assertFalse(context.isActive());
        
        context.registerBean("testBean", String.class);
        context.refresh();
        
        assertTrue(context.isActive());
    }

    @Test
    void testCloseContext() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.registerBean("testBean", String.class);
        context.refresh();
        context.close();

        assertFalse(context.isActive());
    }

    static class TestService {
        public void doSomething() {}
    }
}

